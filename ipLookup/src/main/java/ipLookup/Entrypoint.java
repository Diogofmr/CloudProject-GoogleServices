package ipLookup;

import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.stream.StreamSupport;

public class Entrypoint implements HttpFunction {

    private static final String PROJECT_ID = "cn2324-t1-g18";
    private static final String ZONE = "europe-west2-b";
    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        String instanceGroup = request.getFirstQueryParameter("instance-group").orElseThrow();

        String[] ips = getIpInstancesFromGroup(instanceGroup);

        BufferedWriter writer = response.getWriter();
        writer.write(String.join(",", ips)); // return the list of IPs separated by commas
        writer.close();
    }

    static String[] getIpInstancesFromGroup(String instanceGroupName) throws IOException {
        System.out.println("Instances of instance group: " + instanceGroupName);
        try (InstancesClient client = InstancesClient.create()) {
            return StreamSupport.stream(client.list(PROJECT_ID, ZONE).iterateAll().spliterator(), false)
                    .filter(instance -> instance.getName().contains(instanceGroupName))
                    .map(instance -> instance.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP())
                    .toArray(String[]::new);
        }
    }
}
