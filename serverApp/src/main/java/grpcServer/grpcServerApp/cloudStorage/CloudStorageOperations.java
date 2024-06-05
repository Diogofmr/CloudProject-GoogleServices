package grpcServer.grpcServerApp.cloudStorage;

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.*;
import grpcServer.config.Configuration;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudStorageOperations {

    public Storage storage;
    private final StorageClass defaultStorageClass = StorageClass.STANDARD;

    public CloudStorageOperations(Storage storage) {
        this.storage = storage;
    }

    /**
     * Create a bucket in the Google Cloud Storage with default storage class and location defined in the class
     *
     * @param bucketName name of the bucket to be created
     * @throws Exception if an error occurs while creating the bucket
     */
    public void createBucket(String bucketName) throws Exception {
        try {
            storage.create(
                    BucketInfo.newBuilder(bucketName)
                            // See here for possible values: http://g.co/cloud/storage/docs/storage-classes
                            .setStorageClass(defaultStorageClass)
                            // Possible values: http://g.co/cloud/storage/docs/bucket-locations#location-mr
                            .setLocation(Configuration.DEFAULT_LOCATION)
                            .build());
        } catch (Exception e) {
            throw new Exception("Error creating bucket: " + e.getMessage());
        }
    }


    /**
     * Check if a bucket with the given name exists in the Google Cloud Storage
     *
     * @param bucketName name of the bucket to be checked
     * @return true if the bucket name not exists, false otherwise
     */
    public boolean isBucketAvailable(String bucketName) {
        try {
            Bucket bucket = storage.get(bucketName, Storage.BucketGetOption.fields());
            return bucket != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Delete a bucket from the Google Cloud Storage
     *
     * @param bucketName name of the bucket to be deleted
     * @throws Exception if an error occurs while deleting the bucket
     */
    public void deleteBucket(String bucketName) throws Exception {
        try {
            Bucket bucket = storage.get(bucketName);
            if (bucket != null) {
                bucket.delete();
            } else {
                throw new Exception("Bucket " + bucketName + " does not exist");
            }
        } catch (Exception e) {
            throw new Exception("Error deleting bucket: " + e.getMessage());
        }
    }

    /**
     * Download a file from the Google Cloud Storage, i.e, download a blob from the bucket with the given name
     *
     * @param bucketName name of the bucket where the file will be downloaded
     * @param blobName   name of the blob to be downloaded
     * @throws Exception if an error occurs while downloading the file
     */
    public void downloadFile(String bucketName, String blobName) throws Exception {
        try {
            Path downloadTo = Paths.get(blobName + "_downloaded.png");
            System.out.println("download to: " + downloadTo);
            BlobId blobId = BlobId.of(bucketName, blobName);
            Blob blob = storage.get(blobId);
            if (blob == null) {
                System.out.println("No such Blob exists !");
                return;
            }
            PrintStream writeTo = new PrintStream(Files.newOutputStream(downloadTo));
            // Blob is small read all its content in one request
            if (blob.getSize() < 1_000_000) {
                byte[] content = blob.getContent();
                writeTo.write(content);
            } else {
                // Blob size is bigger than 1MB or unknown use the blob's channel reader.
                try (ReadChannel reader = blob.reader()) {
                    WritableByteChannel channel = Channels.newChannel(writeTo);
                    ByteBuffer bytes = ByteBuffer.allocate(64 * 1024);
                    while (reader.read(bytes) > 0) {
                        bytes.flip();
                        channel.write(bytes);
                        bytes.clear();
                    }
                }
            }
            writeTo.close();
            System.out.println("Blob " + blobName + " downloaded to " + downloadTo);
        } catch (Exception e) {
            throw new Exception("Error downloading file: " + e.getMessage());
        }
    }

}
