package grpcServer;

import grpcServer.grpcServerApp.services.SfService;
import grpcServer.grpcServerApp.services.SgService;
import grpcServer.grpcServerApp.ShutdownHook;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GrpcServer {

    private static int svcPort = 7500;

    public static void main(String[] args) {
        try {
            if (args.length > 0) svcPort = Integer.parseInt(args[0]);
            System.out.println("env:" + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
            Server svc = ServerBuilder.forPort(svcPort)
                    // Add one or more services.
                    // The Server can host many services in same TCP/IP port
                    .addService(new SfService(svcPort))
                    .addService(new SgService(svcPort))
                    .build().start();
            // Java virtual machine shutdown hook
            // to capture normal or abnormal exits
            Runtime.getRuntime().addShutdownHook(new ShutdownHook(svc));
            // Waits for the server to become terminated
            svc.awaitTermination();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
