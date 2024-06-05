package labelsApp.pubSub;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.PubsubMessage;
import labelsApp.api.cloudTranslation.TranslateWord;
import labelsApp.api.cloudVision.DetectLabels;
import labelsApp.firestore.FirestoreDb;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MessageReceiveHandler implements MessageReceiver {
    private static final String DEFAULT_LANGUAGE = "en";
    DetectLabels detectLabels = new DetectLabels();
    TranslateWord translateWord = new TranslateWord();
    public String blobName;
    public String bucketName;
    public String targetLanguage;

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        System.out.println("✅ :: Message " + message.getMessageId() + " received!");
        Map<String, String> atribs = message.getAttributesMap();
        for (String key : atribs.keySet())
            System.out.println("    -> 🔑 :: Attribute:(" + key + " => " + atribs.get(key) + ")");
        blobName = atribs.get("blob");
        bucketName = atribs.get("bucket");
        targetLanguage = atribs.get("language");
        consumer.ack();

        try {
            Map<String, Float> labels = detectLabels.detect(bucketName, blobName);
            // Map of labels translated to the target language
            Map<String, Float> translatedLabels = new HashMap<>();
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            FirestoreDb firestoreDb = new FirestoreDb(credentials);
            for (String label : labels.keySet()) {
                if (Objects.equals(targetLanguage, DEFAULT_LANGUAGE)) {
                    translatedLabels.put(label, labels.get(label));
                } else {
                    String translatedLabel = translateWord.detectLangAndTranslateTo(label, this.targetLanguage);
                    translatedLabels.put(translatedLabel, labels.get(label));
                }
            }
            firestoreDb.insertDocument(this.blobName, translatedLabels, this.targetLanguage);
        } catch (IOException | ExecutionException | InterruptedException e) {
            if (e instanceof IOException) {
                System.err.println("❗ :: Error detecting labels: " + e.getMessage());
            } else {
                System.err.println("❗ :: Error inserting document: " + e.getMessage());
            }
        }
    }

}
