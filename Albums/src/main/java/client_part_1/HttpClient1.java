package client_part_1;

import client.AlbumClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpClient1 {
    private static final AtomicInteger totalRequests = new AtomicInteger(0);
    private static final int NUM_OF_ARGS = 4;
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final int INITIAL_THREAD_SIZE = 10;
    private static final int INITIAL_REQUEST_LOOP = 100;
    private static final int ADDITIONAL_REQUEST_LOOP = 1000;

    public static void main(String[] args) {
        if (args.length != NUM_OF_ARGS) {
            System.out.println("Invalid args! " +
                    "Usage: java HttpClientRequestLoads <threadGroupSize> <numThreadGroups> <delay> <IPAddr>");
            System.exit(1);
        }

        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setDefaultMaxPerRoute(50);
        manager.setMaxTotal(50);
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(manager).build();

        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        int delay = Integer.parseInt(args[2]);
        String IPAddr = args[3];

        ExecutorService executor = Executors.newFixedThreadPool(INITIAL_THREAD_SIZE);
        AlbumClient albumClient = new AlbumClient(httpClient, IPAddr);
        File imageFile = new File("images/nmtb.png");

        // initial 10 threads
        for (int i = 0; i < INITIAL_THREAD_SIZE; i++) {
            executor.execute(() -> {
                for (int j = 0; j < INITIAL_REQUEST_LOOP; j++) {
                    performRequests("POST", albumClient, imageFile);
                    performRequests("GET", albumClient, imageFile);
                }
            });
        }
        waitForCompleted(executor);

        // additional threads groups
        long startTime = System.currentTimeMillis();
        executor = Executors.newFixedThreadPool(threadGroupSize * numThreadGroups);
        for (int group = 0; group < numThreadGroups; group++) {
            for (int i = 0; i < threadGroupSize; i++) {
                executor.execute(() -> {
                    for (int j = 0; j < ADDITIONAL_REQUEST_LOOP; j++) {
                        totalRequests.addAndGet(performRequests("POST", albumClient, imageFile));
                        totalRequests.addAndGet(performRequests("GET", albumClient, imageFile));
                    }
                });
            }
            if (group < numThreadGroups - 1) {
                try {
                    Thread.sleep(delay * 1000L);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        waitForCompleted(executor);

        long endTime = System.currentTimeMillis();
        long wallTime = (endTime - startTime) / 1000;
        double throughput = (double) totalRequests.get() / wallTime;

        System.out.println("\nWall Time: " + wallTime + " seconds");
        System.out.println("Throughput: " + throughput + " requests per second");
    }

    public static int performRequests(String requestType, AlbumClient albumClient, File imageFile) {
        boolean requestSuccessful = false;
        int retryCount = 1;
        while (!requestSuccessful && retryCount < MAX_RETRY_ATTEMPTS) {
            try {
                if ("GET".equals(requestType)) {
                    albumClient.getAlbum("1");
                } else if ("POST".equals(requestType)) {
                    albumClient.postAlbum("Sex Pistols", "Never Mind The Bollocks!", "1977", imageFile);
                }

                requestSuccessful = true;
            } catch (Exception e) {
                e.printStackTrace();
                retryCount++;
            }
        }
        return retryCount;
    }

    private static void waitForCompleted(ExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
