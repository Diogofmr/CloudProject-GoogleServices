package labelsApp.api.cloudVision;

import com.google.cloud.vision.v1.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectLabels {
    /**
     * Function that interacts with Google Cloud Vision API and return the labels of the image
     *
     * @param bucketName name of the bucket where the image is stored
     * @param blobName   Name of the blob that contains the image
     * @return Map of AnnotateImageResponse with the labels of the image
     */
    public Map<String, Float> detect(String bucketName, String blobName) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();
        String gcsPath = "gs://" + bucketName + "/" + blobName;

        ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
        Image img = Image.newBuilder().setSource(imgSource).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            Map<String, Float> labels = new HashMap<>();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                    return null;
                }

                // For full list of available annotations, see http://g.co/cloud/vision/docs
                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                    labels.put(annotation.getDescription(), annotation.getScore());
                }
            }
            return labels;
        }
    }
}
