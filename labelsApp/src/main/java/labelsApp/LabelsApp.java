package labelsApp;

import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.pubsub.v1.ProjectSubscriptionName;
import labelsApp.config.Configuration;
import labelsApp.pubSub.MessageReceiveHandler;

/**
 * Labels App that is responsible for managing labels from received messages from PubSub
 * <p>
 * It is a simple app that will receive messages from PubSub and will interact with Google Vision API
 * to extract labels from the images and Translation API to translate the labels to a specific language.
 * <p>
 * Then Store the labels in Google Firestore.
 */
public class LabelsApp {

    // Change to an environment variable
    private static final String PROJECT_ID = Configuration.PROJECT_ID;
    private static final String subscriptionID = Configuration.LABELS_SUBSCRIPTION_ID;

    public static void main(String[] args) {
        Subscriber subscriber = null;
        try {
            System.out.println("🚀 :: Labels App Started");

            System.out.println("env: " + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));

            while (!subscriptionExists(PROJECT_ID, subscriptionID)) {
                System.out.println("🔍 :: Checking Subscription...");
                Thread.sleep(3000);
            }

            subscriber = subscribeMessages(PROJECT_ID, subscriptionID);

            while (true) {
                Thread.sleep(1000);
                if (!subscriber.isRunning()) {
                    System.out.println("⚠️ :: Subscriber is not running, restarting...");
                }
            }

        } catch (Exception e) {
            System.out.println("❗ :: Error Launching Labels App: " + e.getMessage());
        } finally {
            System.out.println("♻️ :: Labels App Finished");
            if (subscriber != null) {
                subscriber.startAsync();
                subscriber.awaitTerminated();
            }
        }
    }

    private static boolean subscriptionExists(String projectId, String subscriptionID) {
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionID);
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
            return subscriptionAdminClient.getSubscription(subscriptionName) != null;
        } catch (Exception e) {
            System.err.println("❗ :: Error checking subscription: " + e.getMessage());
            return false;
        }
    }

    /**
     * Create a subscriber to receive messages from PubSub
     *
     * @param projectID      the project ID
     * @param subscriptionID the subscription ID
     * @return the subscriber
     */
    public static Subscriber subscribeMessages(String projectID, String subscriptionID) {
        ProjectSubscriptionName projSubscriptionName = ProjectSubscriptionName.of(
                projectID, subscriptionID);
        ExecutorProvider executorProvider = InstantiatingExecutorProvider
                .newBuilder()
                .setExecutorThreadCount(1) // um só thread no handler
                .build();
        Subscriber subscriber =
                Subscriber.newBuilder(projSubscriptionName, new MessageReceiveHandler())
                        .setExecutorProvider(executorProvider)
                        .build();
        subscriber.startAsync().awaitRunning();
        return subscriber;
    }

}
