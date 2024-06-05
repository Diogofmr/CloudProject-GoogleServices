package grpcServer.grpcServerApp.firestore;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import grpcServer.config.Configuration;
import grpcServer.grpcServerApp.firestore.Util.DateConverter;
import jdk.jshell.spi.ExecutionControl;
import sfservicestubs.*;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class FirestoreDb {

    private final Firestore db;
    static String databaseId = Configuration.FIRESTORE_DATABASE_ID;
    static String collectionName = Configuration.FIRESTORE_LABELS_COLLECTION_NAME;


    public FirestoreDb(GoogleCredentials credentials) {
        FirestoreOptions options = FirestoreOptions
                .newBuilder().setDatabaseId(databaseId).setCredentials(credentials)
                .build();
        db = options.getService();
    }

    /**
     * Create a new collection in Firestore and populate it with the data from the CSV file
     * Each line of the CSV file is a new document in the collection
     */
    private boolean isCollectionEmpty() {
        try {
            ApiFuture<QuerySnapshot> querySnapshot = db.collection(collectionName).get();
            return querySnapshot.get().isEmpty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the names of the images that match the request and returns them
     *
     * @param dti            initial date
     * @param dtf            final date
     * @param characteristic characteristic of the image
     */
    public Stream<String> getImagesNames(String dti, String dtf, String characteristic) {
        Timestamp dtiTimestamp = DateConverter.stringToTimestamp(dti);
        Timestamp dtfTimestamp = DateConverter.stringToTimestamp(dtf);
        FieldPath labelsPath = FieldPath.of("characteristics", "labels", characteristic);
        try {
            Query query = db.collection(collectionName)
                    .whereGreaterThanOrEqualTo("date", dtiTimestamp)
                    .whereLessThanOrEqualTo("date", dtfTimestamp);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            return querySnapshot.get().getDocuments().stream()
                    .filter(document -> document.contains(labelsPath)).map(DocumentSnapshot::getId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Obtain the information of an image given its ID
     *
     * @param id the ID of the request
     * @return the information of the image (characteristic, date, language)
     */
    public ImageInfo getImageInfo(String id) {
        try {
            CollectionReference docRef = db.collection(collectionName);
            ApiFuture<DocumentSnapshot> future = docRef.document(id).get();
            DocumentSnapshot document = future.get();
            FieldPath labelsPath = FieldPath.of("characteristics", "labels");
            FieldPath languagePath = FieldPath.of("characteristics", "language");
            if (document.exists() && !isCollectionEmpty()) {
                // Get the labels map from the document
                Object labelsField = document.get(labelsPath);

                // Create the ImageInfo builder
                ImageInfo.Builder imageInfoBuilder = ImageInfo.newBuilder();

                // Set the date and language fields of the ImageInfo object
                imageInfoBuilder.setDate(Objects.requireNonNull(document.getDate("date")).toString());
                imageInfoBuilder.setLanguage(document.getString(String.valueOf(languagePath)));

                // If the labels field is a map of maps, process it as such
                if (labelsField instanceof Map) {
                    Map<String, Double> labelsData = (Map<String, Double>) labelsField;

                    // For each label data map, create a Label object and add it to the ImageInfo object
                    for (Map.Entry<String, Double> entry : labelsData.entrySet()) {
                        String name = entry.getKey();
                        // Get the score from the label data map without casting it
                        float score = entry.getValue().floatValue();
                        Label label = Label.newBuilder()
                                .setName(name)
                                .setValue(score)
                                .build();

                        imageInfoBuilder.addLabels(label);
                    }
                }

                return imageInfoBuilder.build();
            } else {
                throw new ExecutionControl.NotImplementedException("Document or Collection not found");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
