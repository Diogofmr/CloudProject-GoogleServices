package grpcServer.grpcServerApp.pubSub;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.TopicName;
import grpcServer.config.Configuration;

import java.io.IOException;
import java.util.Scanner;

public class PubSub {
    private final String PROJECT_ID;

    public PubSub(String projectID) {
        this.PROJECT_ID = projectID;

    }

    private static String read(String msg, Scanner input) {
        System.out.println(msg);
        return input.nextLine();
    }

    public boolean topicExists(String topicID) {
        try (TopicAdminClient topicAdmin = TopicAdminClient.create()) {
            TopicName projTopName = TopicName.ofProjectTopicName(PROJECT_ID, topicID);
            topicAdmin.getTopic(projTopName);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void createNewTopic(String topicID) throws IOException {
        TopicAdminClient topicAdmin = TopicAdminClient.create();
        TopicName projTopName = TopicName.ofProjectTopicName(PROJECT_ID, topicID);
        topicAdmin.createTopic(projTopName);
        topicAdmin.close();
    }

    public void publishMessage(String topicID, String requestID, String bucket, String blob, String language) {
        try {
            TopicName topicName = TopicName.ofProjectTopicName(PROJECT_ID, topicID);
            Publisher publisher = Publisher.newBuilder(topicName).build();

            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8("RequestID:" + requestID + " Bucket:" + bucket + " Blob:" + blob))
                    .putAttributes("requestID", requestID)
                    .putAttributes("bucket", bucket)
                    .putAttributes("blob", blob)
                    .putAttributes("language", language)
                    .build();

            ApiFuture<String> future = publisher.publish(pubsubMessage);
            String msgID = future.get();
            System.out.println("    -> 🚀 :: Message [" + msgID + "] Published");
            System.out.println("    -> \uD83D\uDD11 :: Message: " + pubsubMessage.getData().toStringUtf8());
            publisher.shutdown();
        } catch (Exception e) {
            System.out.println("    -> ❌ :: Error publishing message: " + e.getMessage());
        }
    }

    public void createSubscription(String topicName, String subscriptionId) {
        ProjectSubscriptionName projSubscriptionName = ProjectSubscriptionName.of(PROJECT_ID, subscriptionId);
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
            TopicName projTopicName = TopicName.ofProjectTopicName(PROJECT_ID, topicName);
            // Create pull grpcServer.config for the subscription
            PushConfig pushConfig = PushConfig.getDefaultInstance();
            subscriptionAdminClient.createSubscription(projSubscriptionName, projTopicName, pushConfig, Configuration.ACK_DEAD_LINE);
        } catch (Exception e) {
            System.out.println("    -> ❌ Error creating subscription: " + e.getMessage());
        }
    }

    public boolean existsSubscription(String topicName, String subscriptionId) {
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
            ProjectSubscriptionName projSubscriptionName = ProjectSubscriptionName.of(PROJECT_ID, subscriptionId);
            subscriptionAdminClient.getSubscription(projSubscriptionName);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
