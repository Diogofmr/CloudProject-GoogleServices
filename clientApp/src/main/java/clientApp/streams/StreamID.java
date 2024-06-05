package clientApp.streams;

import io.grpc.stub.StreamObserver;
import sfservicestubs.ID;

public class StreamID implements StreamObserver<ID> {
    public String imagePath;
    public String targetLanguage;

    public StreamID(String imagePath, String targetLanguage) {
        this.imagePath = imagePath;
        this.targetLanguage = targetLanguage;
    }

    @Override
    public void onNext(ID value) {
        System.out.println(" -> [✓] Image ID: " + value.getId());
    }

    @Override
    public void onError(Throwable t) {
        System.err.println(" ❌ :: Server is not available. Try again Later!");
    }

    @Override
    public void onCompleted() {
        System.out.println(" -> [✓] Image in path ->" + imagePath + " uploaded!");
    }
}
