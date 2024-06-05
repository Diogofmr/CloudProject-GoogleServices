package grpcServer.grpcServerApp.stubs;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import grpcServer.grpcServerApp.cloudStorage.CloudStorageOperations;
import grpcServer.grpcServerApp.pubSub.PubSub;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import sfservicestubs.ID;
import sfservicestubs.Image;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static java.util.UUID.randomUUID;

public class ImageStreamObserver implements StreamObserver<Image> {
    private final StreamObserver<ID> responseObserver;
    private final CloudStorageOperations cloudStorageOperations;
    private final PubSub pubSub;
    private final String bucketName;
    private final String topicName;
    private final String labelsSubscriptionName;

    private String blobName;
    private String requestId;
    private String imageName;
    private String language = null;
    private WriteChannel writer = null;

    public ImageStreamObserver(StreamObserver<ID> responseObserver, CloudStorageOperations cloudStorageOperations, String bucketName, PubSub pubSub, String topicName, String labelsSubscriptionName) {
        this.responseObserver = responseObserver;
        this.cloudStorageOperations = cloudStorageOperations;
        this.bucketName = bucketName;
        this.pubSub = pubSub;
        this.topicName = topicName;
        this.labelsSubscriptionName = labelsSubscriptionName;
    }

    @Override
    public void onNext(Image value) {
        language = value.getLanguage();
        if (imageName == null) {
            // For macOS and Linux
            String[] path = value.getName().split("/");
            // For Windows
            if (path.length == 1) {
                path = value.getName().split("\\\\");
            }
            imageName = path[path.length - 1];

            // Split the imageName into name and extension
            int lastDotIndex = imageName.lastIndexOf('.');
            String name = imageName.substring(0, lastDotIndex);

            // Append the UUID to the name, then append the extension
            requestId = name + "-" + randomUUID();
            blobName = requestId;
        }

        if (!cloudStorageOperations.isBucketAvailable(bucketName)) {
            try {
                cloudStorageOperations.createBucket(bucketName);
            } catch (Exception e) {
                System.err.println("    -> ❌ :: Error creating bucket: " + e.getMessage());
            }
        }

        if (writer == null) {
            BlobId blobId = BlobId.of(bucketName, blobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/jpeg").build();
            writer = cloudStorageOperations.storage.writer(blobInfo);
        }

        byte[] buffer = new byte[1024];
        try (InputStream input = new ByteArrayInputStream(value.getContent().toByteArray())) {
            int limit;
            while ((limit = input.read(buffer)) >= 0) {
                try {
                    writer.write(ByteBuffer.wrap(buffer, 0, limit));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("    -> ❌ :: Error uploading file to blob - " + blobName + " - : " + e.getMessage());
        }
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("    -> ❌ Error: " + t.getMessage());
        responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT.withDescription("Error: " + t.getMessage())));
    }

    @Override
    public void onCompleted() {
        System.out.println("    -> \uD83D\uDE80 :: Image [" + imageName + "] uploaded successfully: ");

        responseObserver.onNext(ID.newBuilder().setId(requestId).build());
        responseObserver.onCompleted();

        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e) {
                System.err.println("    -> ❌ :: Error closing the writer: " + e.getMessage());
            }
        }

        // Send the message to the PubSub topic "image-requests"
        try {
            if (!pubSub.topicExists(topicName)) {
                pubSub.createNewTopic(topicName);
            }
            if (!pubSub.existsSubscription(topicName, labelsSubscriptionName)) {
                pubSub.createSubscription(topicName, labelsSubscriptionName);
            }
            pubSub.publishMessage(topicName, requestId, bucketName, blobName, language);
        } catch (Exception e) {
            System.out.println("    -> ❌ :: Error sending the message to the PubSub topic: " + e.getMessage());
        }
    }
}
