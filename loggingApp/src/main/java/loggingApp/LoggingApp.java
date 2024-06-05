package loggingApp;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import loggingApp.config.Configuration;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoggingApp implements BackgroundFunction<PubSubMessage> {

    private static final Logger logger = Logger.getLogger(LoggingApp.class.getName());
    //private static final FirestoreDb fdb = initFirestoreDb();
    private static final Firestore db = initFirestore();

    /*private static FirestoreDb initFirestoreDb() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            return new FirestoreDb(credentials);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    private static Firestore initFirestore(){
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            FirestoreOptions options = FirestoreOptions
                    .newBuilder().setDatabaseId(Configuration.FIRESTORE_DATABASE_ID)
                    .setCredentials(credentials)
                    .build();
            return options.getService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void accept(PubSubMessage payload, Context context) throws Exception {
        if (db == null) {
            logger.info("Error connecting to Firestore. Exiting function.");
            //https://cloud.google.com/functions/docs/bestpractices/retries#why_event-driven_functions_fail_to_complete
            //https://cloud.google.com/functions/docs/bestpractices/retries#set_an_end_condition_to_avoid_infinite_retry_loops
            throw new RuntimeException("Error connecting to Firestore");
        }
        logger.info("Function triggered by message: " + payload.data);
        String data = new String(Base64.getDecoder().decode(payload.data));
        String requestId = getRequestId(data);
        logger.info(data);
        Map<String, Object> doc = prepareData(payload, context);
        CollectionReference coll = db.collection(Configuration.FIRESTORE_LOGS_COLLECTION_NAME);
        logger.info("Adding document to Firestore.");
        DocumentReference document = coll.document();
        logger.info("Adding document to Firestore with ID: " + document.getId());
        ApiFuture<WriteResult> result = document.set(doc);
        logger.info("Update time: " + result.get().getUpdateTime());
        logger.info("Event was written to Firestore.");
    }

    private Map<String, Object> prepareData(PubSubMessage payload, Context context) {
        HashMap<String, Object> parsedData = new HashMap<>();
        parsedData.put("message-data", payload.data);
        parsedData.put("message-attributes", payload.attributes);
        parsedData.put("message-id", context.eventId());
        parsedData.put("pub-time", context.timestamp());
        return parsedData;
    }

    private static String getRequestId(String input) {
        String pattern = "RequestID:(.*?)\\s";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(input);
        if (m.find()) {
            return m.group(1);
        } else {
            return "No RequestID found";
        }
    }
}
