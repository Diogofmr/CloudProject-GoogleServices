package grpcServer.grpcServerApp.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.StorageOptions;
import grpcServer.config.Configuration;
import grpcServer.grpcServerApp.cloudStorage.CloudStorageOperations;
import grpcServer.grpcServerApp.firestore.FirestoreDb;
import grpcServer.grpcServerApp.pubSub.PubSub;
import grpcServer.grpcServerApp.stubs.ImageStreamObserver;
import io.grpc.stub.StreamObserver;
import sfservicestubs.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.Stream;

public class SfService extends SfServiceGrpc.SfServiceImplBase {

    public SfService(int svcPort) throws IOException {
        System.out.println("\uD83D\uDE80 :: Service started on port: " + svcPort);
    }

    String keyFilePath = Configuration.GOOGLE_APPLICATION_CREDENTIALS;
    CloudStorageOperations cloudStorageOperations = new CloudStorageOperations(
            StorageOptions
            .getDefaultInstance()
            .toBuilder()
            .setCredentials(ServiceAccountCredentials
                    .fromStream(new FileInputStream(keyFilePath)))
            .build()
            .getService());
    FirestoreDb fdb = new FirestoreDb(GoogleCredentials.getApplicationDefault());
    PubSub pubSub = new PubSub(Configuration.PROJECT_ID);


    @Override
    public StreamObserver<Image> submitImage(StreamObserver<ID> responseObserver) {
        System.out.println("♻️ :: submitImage called!");
        return new ImageStreamObserver(
                responseObserver,
                cloudStorageOperations,
                Configuration.BUCKET_NAME,
                pubSub,
                Configuration.TOPIC_NAME,
                Configuration.LABELS_SUBSCRIPTION_ID
        );
    }

    @Override
    public void getImageInfo(ID request, StreamObserver<ImageInfo> responseObserver) {
        System.out.println("♻️ :: getImageInfo called!");
        ImageInfo imageInfo = fdb.getImageInfo(request.getId());
        System.out.println("    -> ♻️ :: Image Info Retrieved with Success!");
        responseObserver.onNext(imageInfo);
        responseObserver.onCompleted();
    }

    @Override
    public void getImagesNames(ImagesRequest request, StreamObserver<ImagesNames> responseObserver) {
        System.out.println("♻️ :: getImagesNames called!");
        Stream<String> names = fdb.getImagesNames(request.getIDate(), request.getFDate(), request.getCharacteristic());
        ImagesNames.Builder imagesNamesBuilder = ImagesNames.newBuilder();
        names.forEach(imagesNamesBuilder::addNames);
        responseObserver.onNext(imagesNamesBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void downloadImage(ID request, StreamObserver<Image> responseObserver) {
        System.out.println("♻️ :: downloadImage called!");
        try {
            cloudStorageOperations.downloadFile(Configuration.BUCKET_NAME, request.getId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        responseObserver.onCompleted();
    }

}
