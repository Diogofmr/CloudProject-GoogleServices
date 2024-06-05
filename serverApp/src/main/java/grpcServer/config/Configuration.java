package grpcServer.config;

public class Configuration {
    public static final int ACK_DEAD_LINE = 0;
    public static final String PROJECT_ID = "cn2324-t1-g18";
    public static final String BUCKET_NAME = "images-bucket-project-cn18";
    public static final String TOPIC_NAME = "image-requests";
    public static final String LABELS_SUBSCRIPTION_ID = "labels-app";
    public static final String GOOGLE_APPLICATION_CREDENTIALS = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
    public static final String FIRESTORE_LABELS_COLLECTION_NAME = "images";
    public static final String FIRESTORE_LOGS_COLLECTION_NAME = "logs";
    public static final String FIRESTORE_DATABASE_ID = "lab4-database";
    public static final String DEFAULT_LOCATION = "europe-west1";

    // Configurations for the Instance Group Manager
    public static final String ZONE = "europe-west2-b";
    public static final String SERVER_INSTANCE_GROUP_NAME = "group-server";
    public static final String LABELS_INSTANCE_GROUP_NAME = "group-labels";
}
