package clientApp;

import clientApp.streams.StreamID;
import clientApp.util.ChooseLanguage;
import clientApp.util.Menu;
import com.google.protobuf.ByteString;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import sfservicestubs.*;
import sgservicestubs.InstanceRequest;
import sgservicestubs.InstancesResponse;
import sgservicestubs.SgServiceGrpc;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Client {
    // generic ClientApp for Calling a grpc Service
//    private static final String svcIP = "34.147.168.187";
    private static final String IP_LOOKUP_URL = "https://europe-west1-cn2324-t1-g18.cloudfunctions.net/funcIPLookup?instance-group=group-server";
    private static final int SVC_PORT = 7500;
    private static ManagedChannel channel;
    private static SfServiceGrpc.SfServiceBlockingStub blockingStub;
    private static SfServiceGrpc.SfServiceStub noBlockStub;
    private static SgServiceGrpc.SgServiceBlockingStub blockingStubSg;
    private static String targetLanguage = null;

    public static void main(String[] args) {
        try {
            // Get the IP on the instance group using the ipLookup function in the cloud
            // If there is no IP, will try a retry policy
            connectToServer();
            // Create a blocking stub
            blockingStub = SfServiceGrpc.newBlockingStub(channel);

            // Create a non-blocking stub
            noBlockStub = SfServiceGrpc.newStub(channel);

            // Create a blocking stub for the SgService
            blockingStubSg = SgServiceGrpc.newBlockingStub(channel);

            boolean end = false;
            while (!end) {
                try {
                    int option = Menu.make();
                    switch (option) {
                        case 1:
                            targetLanguage = ChooseLanguage.chooseLanguage();
                            submitImageCall();
                            break;
                        case 2:
                            String id = String.valueOf(read("Enter the image ID: ", new Scanner(System.in)));
                            getImageInfoCall(id);
                            break;
                        case 3:
                            String iDate = String.valueOf(read("\uD83D\uDC49 ♦ Enter the initial date (YYYY/MM/DD): ", new Scanner(System.in)));
                            String fDate = String.valueOf(read("\uD83D\uDC49 ♦ Enter the final date (YYYY/MM/DD): ", new Scanner(System.in)));
                            String characteristic = String.valueOf(read("\uD83D\uDC49 ♦ Enter the characteristic: ", new Scanner(System.in)));
                            getImagesNamesCall(iDate, fDate, characteristic);
                            break;
                        case 4:
                            String idDownload = String.valueOf(read("Enter the image ID: ", new Scanner(System.in)));
                            downloadImageCall(idDownload);
                            break;
                        case 5:
                            serverInstancesCall();
                            break;
                        case 6:
                            labelsInstancesCall();
                            break;
                        case 99:
                            // Call the service operation
                            System.exit(0);
                    }
                } catch (Exception ex) {
                    System.out.println("Error: " + ex.getMessage());
                }
            }
            read("prima enter to end", new Scanner(System.in));
        } catch (Exception ex) {
            System.out.println("Unhandled exception");
            ex.printStackTrace();
        }
    }

    private static void submitImageCall() {
        String imagePath = String.valueOf(read("Enter the image path: ", new Scanner(System.in)));
        // block size to read the image
        int blockSize = 1024;

        // Call the service operation
        StreamObserver<ID> streamId = new StreamID(imagePath, targetLanguage);
        StreamObserver<Image> streamImage = noBlockStub.submitImage(streamId);

        // Create a FileInputStream to read the image file
        try (FileInputStream fis = new FileInputStream(imagePath)) {
            byte[] block = new byte[blockSize];
            int bytesRead;

            // Loop over the FileInputStream and read each block
            while ((bytesRead = fis.read(block)) != -1) {
                // Create an Image object for the current block
                Image image = Image.newBuilder()
                        .setName(imagePath)
                        .setContent(ByteString.copyFrom(block, 0, bytesRead))
                        .setLanguage(targetLanguage)
                        .build();

                // Send the block
                streamImage.onNext(image);
            }
        } catch (IOException e) {
            // Error reading the image file
            System.err.println("❗ :: Error reading the image file. Please ensure the file path is correct and the file is accessible ::");
            return;
        }
        // Notify the server that the image has been sent
        streamImage.onCompleted();
    }

    private static void getImageInfoCall(String id) {
        try {
            ImageInfo imageInfo = blockingStub.getImageInfo(ID.newBuilder().setId(id).build());
            System.out.println("\uD83D\uDCC4 :: Image Info ::");
            System.out.println("    |       |");
            System.out.println("    \uD83D\uDC49 \uD83D\uDCC5 :: Date: " + imageInfo.getDate());
            System.out.println("    \uD83D\uDC49 \uD83D\uDCCD :: Language: " + imageInfo.getLanguage());
            System.out.println("    \uD83D\uDC49 \uD83D\uDCDD :: Labels List: ");
            System.out.println("        |       |");
            for (Label label : imageInfo.getLabelsList()) {
                System.out.println("        \uD83D\uDC49 ✅ :: Label: " + label.getName() + " - Score: " + label.getValue());
            }
        } catch (Exception e) {
            System.err.println("❗ :: Error getting the image info. Please ensure the image ID is correct. ::");
        }
    }

    private static void getImagesNamesCall(String iDate, String fDate, String characteristic) {
        if (!isFormatCorrect(iDate) || !isFormatCorrect(fDate)) {
            System.err.println("❗ :: Invalid date format. Please use the format YYYY/MM/DD ::");
            return;
        }
        try {
            ImagesRequest request = ImagesRequest.newBuilder()
                    .setIDate(iDate)
                    .setFDate(fDate)
                    .setCharacteristic(characteristic)
                    .build();
            ImagesNames ImagesNames = blockingStub.getImagesNames(request);
            System.out.println("\uD83D\uDCC4 :: Images Names ::");
            System.out.println("    |       |");
            if (ImagesNames.getNamesCount() == 0) {
                System.out.println("    \uD83D\uDC49 ❌ :: No images found with the specified parameters ::");
            } else {
                ImagesNames.getNamesList().forEach(name -> System.out.println("    \uD83D\uDC49 ✅ :: Name: " + name));
            }

        } catch (Exception e) {
            System.err.println("❗ :: Error getting the images Name ::");
        }
    }

    /**
     * Check if the date format is correct, according to (YYYY/MM/DD)
     *
     * @param date The date to be checked
     * @return True if the date format is correct, false otherwise
     */
    private static boolean isFormatCorrect(String date) {
        return date.matches("\\d{4}/\\d{2}/\\d{2}");
    }

    private static void downloadImageCall(String id) {
        try {
            Image image = blockingStub.downloadImage(ID.newBuilder().setId(id).build()).next();
            System.out.println(" ⚙ :: Image " + image.getName() + "was downloaded with success! ::");
        } catch (Exception e) {
            System.err.println("❗ :: Error downloading the image. Please ensure the image ID is correct. ::");
            System.err.println("❗ :: Reason: " + e.getMessage() +"::");
        }
    }

    private static void serverInstancesCall() {
        try {
            int numberOfInstances = Integer.parseInt(read("Enter the number of instances: ", new Scanner(System.in)));
            InstancesResponse response = blockingStubSg.serverInstances(InstanceRequest.newBuilder().setNumberOfInstances(numberOfInstances).build());
            System.out.println(" ⚙ :: Instance group resized with success! :: " + response.getMessage());
        } catch (Exception e) {
            System.err.println("❗ :: Error resizing instance group! ::");
        }
    }

    private static void labelsInstancesCall() {
        try {
            int numberOfInstances = Integer.parseInt(read("Enter the number of instances: ", new Scanner(System.in)));
            InstancesResponse response = blockingStubSg.labelsInstances(InstanceRequest.newBuilder().setNumberOfInstances(numberOfInstances).build());
            System.out.println(" ⚙ :: Instance group resized with success! :: " + response.getMessage());
        } catch (Exception e) {
            System.err.println("❗ :: Error resizing instance group! ::");
        }
    }

    /**
     * Connects to a server using the IP lookup.
     * Retries if the connection fails.
     */
    private static void connectToServer() {
        boolean connected = false;

        System.out.println(" ✨ :: Connecting to the server...");

        while (!connected) {
            String svcIp = lookupSvcIp();
            if (svcIp == null) {
                System.out.println(" ❌ :: Error looking up service IP address.");
                continue;
            }

            channel = ManagedChannelBuilder.forAddress(svcIp, SVC_PORT)
                    .usePlaintext()
                    .build();

            System.out.println(" ✨ :: Connecting to service of IP " + svcIp + "...");

            boolean connectionReady = false;
            while (!connectionReady) {
                ConnectivityState state = channel.getState(true);

                if (state == ConnectivityState.READY) {
                    System.out.println("\uD83D\uDE80 :: Client launched on IP [" + svcIp + "] ::");
                    connectionReady = true;
                    connected = true;
                } else if (state == ConnectivityState.TRANSIENT_FAILURE || state == ConnectivityState.SHUTDOWN) {
                    System.out.println("\uD83D\uDC49 :: Service IP: " + svcIp);
                    System.out.println(" ❌ :: Connection failed. Retrying...");
                    break;
                }
            }
        }
    }

    /**
     * Looks up the service IP address.
     *
     * @return the service IP address
     */
    private static String lookupSvcIp() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(IP_LOOKUP_URL))
                    .GET()
                    .build();

            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            String[] ips = response.split(","); // IPs separated by comma
            System.out.println("    ✅ :: Service IPs found :: ");

            if (response.isBlank() || ips.length == 0) {
                System.out.println("❗ :: No service IP found ::");
                return null;
            }

            Scanner scanner = new Scanner(System.in);
            System.out.println(" \uD83D\uDC49 :: Choose the IP to connect to: ");
            for (int i = 0; i < ips.length; i++) {
                System.out.println("    \uD83D\uDC49 :: [" + i + "] :: " + ips[i]);
            }
            int choice = Integer.parseInt(read("    \uD83D\uDC49 :: Enter the number of the IP to connect to: ", scanner));
            return ips[choice];
        } catch (IOException | InterruptedException e) {
            System.out.println("❗ :: Error looking up the service IP " + e.getMessage() + " ::");
            return null;
        }
    }

    private static String read(String message, Scanner scanner) {
        System.out.println(message);
        return scanner.nextLine();
    }
}
