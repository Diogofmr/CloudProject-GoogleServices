package labelsApp.firestore;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteResult;
import labelsApp.config.Configuration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirestoreDb {

    private final Firestore db;

    public FirestoreDb(GoogleCredentials credentials) {
        FirestoreOptions options = FirestoreOptions
                .newBuilder().setDatabaseId(Configuration.FIRESTORE_DATABASE_ID).setCredentials(credentials)
                .build();
        db = options.getService();
    }

    /**
     * Insert the document in the Firestore collection
     *
     * @param id       the document ID
     * @param labels   labels the labels of the image
     * @param language the language of the labels
     */
    public void insertDocument(String id, Map<String, Float> labels, String language) throws ExecutionException, InterruptedException {
        Map<String, Object> docData = new HashMap<>();
        Map<String, Object> characteristicData = new HashMap<>();
        docData.put("ID", id);
        docData.put("date", new Date());
        characteristicData.put("labels", labels);
        characteristicData.put("language", language);
        docData.put("characteristics", characteristicData);
        ApiFuture<WriteResult> addedDocRef = db.collection(Configuration.FIRESTORE_LABELS_COLLECTION_NAME).document(id).set(docData);
        System.out.println("Added document with ID: " + id + " at: " + addedDocRef.get().getUpdateTime());
    }
}
