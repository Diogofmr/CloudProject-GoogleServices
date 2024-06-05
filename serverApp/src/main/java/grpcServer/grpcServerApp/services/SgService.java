package grpcServer.grpcServerApp.services;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.InstanceGroupManagersClient;
import com.google.cloud.compute.v1.Operation;
import grpcServer.config.Configuration;
import io.grpc.stub.StreamObserver;
import sgservicestubs.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class SgService extends SgServiceGrpc.SgServiceImplBase {

    public SgService(int svcPort) {
        System.out.println("\uD83D\uDE80 :: Service started on port: " + svcPort);
    }

    @Override
    public void serverInstances(InstanceRequest request, StreamObserver<InstancesResponse> responseObserver) {
        System.out.println(" ⚙ :: Resizing instance group ::");
        try {
            InstanceGroupManagersClient managersClient = InstanceGroupManagersClient.create();
            OperationFuture<Operation, Operation> result = managersClient.resizeAsync(
                    Configuration.PROJECT_ID,
                    Configuration.ZONE,
                    Configuration.SERVER_INSTANCE_GROUP_NAME,
                    request.getNumberOfInstances()
            );
            Operation oper = result.get();
            System.out.println("    [✔] :: Instance group resized with success!" + oper.getStatus());
            responseObserver.onNext(InstancesResponse.newBuilder().setMessage(oper.getStatus().toString()).build());
            responseObserver.onCompleted();
        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println("    [✔] :: Error resizing instance group!" + e.getMessage());
        }

    }

    @Override
    public void labelsInstances(InstanceRequest request, StreamObserver<InstancesResponse> responseObserver) {
        System.out.println(" ⚙ :: labelsInstances called!");
        try {
            InstanceGroupManagersClient managersClient = InstanceGroupManagersClient.create();
            OperationFuture<Operation, Operation> result = managersClient.resizeAsync(
                    Configuration.PROJECT_ID,
                    Configuration.ZONE,
                    Configuration.LABELS_INSTANCE_GROUP_NAME,
                    request.getNumberOfInstances()
            );
            Operation oper = result.get();
            responseObserver.onNext(InstancesResponse.newBuilder().setMessage(oper.getStatus().toString()).build());
            responseObserver.onCompleted();
            System.out.println("    [✔] :: Instance group resized with success!" + oper.getStatus());
        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println("    ❌ :: Error resizing instance group!" + e.getMessage());
        }

    }
}
