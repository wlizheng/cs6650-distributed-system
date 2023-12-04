import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import data_models.Profile;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

public class HttpClient2 {
    private static AtomicInteger numOfSuccessfulRequests = new AtomicInteger(0);
    private static AtomicInteger numOfFailedRequests = new AtomicInteger(0);
    private static final List<RequestRecord> requestRecords = new ArrayList<>();
    private static final int NUM_OF_ARGS = 4;
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final int ADDITIONAL_REQUEST_LOOP = 100;

    public static void main(String[] args) throws IOException, TimeoutException {
        if (args.length != NUM_OF_ARGS) {
            System.out.println("Invalid args! " +
                    "Usage: java HttpClientRequestLoads <threadGroupSize> <numThreadGroups> <delay> <IPAddr>");
            System.exit(1);
        }

        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setDefaultMaxPerRoute(200);
        manager.setMaxTotal(200);
        manager.setValidateAfterInactivity(-1);

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(3 * 1000)
                .setConnectionRequestTimeout(3 * 1000)
                .setSocketTimeout(3 * 1000)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .disableAutomaticRetries()
                .setConnectionManager(manager)
                .disableRedirectHandling()
                .build();

        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        int delay = Integer.parseInt(args[2]);
        String IPAddr = args[3];

        AlbumClient albumClient = new AlbumClient(httpClient, IPAddr);
        Profile profile = new Profile("Sex Pistols", "Never Mind The Bollocks!", "1977");
        File imageFile = new File("images/nmtb.png");

        // additional threads groups
        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(threadGroupSize * numThreadGroups);
        for (int group = 0; group < numThreadGroups; group++) {
            for (int i = 0; i < threadGroupSize; i++) {
                executor.execute(() -> {
                    for (int j = 0; j < ADDITIONAL_REQUEST_LOOP; j++) {
                        String albumID = performPostAlbumRequest(albumClient, imageFile, profile);
                        performPostReviewRequest(albumClient, albumID, "like");
                        performPostReviewRequest(albumClient, albumID, "like");
                        performPostReviewRequest(albumClient, albumID, "dislike");
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
        double throughput = (double) numOfSuccessfulRequests.get() / wallTime;

        writeToCSV("records.csv");

        System.out.println("\nWall Time: " + wallTime + " seconds");
        System.out.println("Num of successful requests: " + numOfSuccessfulRequests);
        System.out.println("Num of failed requests: " + numOfFailedRequests);
        System.out.println("Throughput: " + throughput + " requests per second\n");

//        calculateAndDisplayStatistics("POST");
    }

    public static void performPostReviewRequest(AlbumClient albumClient, String albumID, String likeOrNot) {
        boolean requestSuccessful = false;
        int retryCount = 1;
        int statusCode = -1;

        while (!requestSuccessful && retryCount < MAX_RETRY_ATTEMPTS) {
            try {
                long start = System.currentTimeMillis();
                statusCode = albumClient.postReview(albumID, likeOrNot);
                requestSuccessful = true;
                numOfSuccessfulRequests.incrementAndGet();

                long end = System.currentTimeMillis();
                long latency = end - start;
                requestRecords.add(new RequestRecord(start, "POST", latency, statusCode));
            } catch (Exception e) {
                e.printStackTrace();
                numOfFailedRequests.incrementAndGet();
                retryCount++;
            }
        }
    }

    public static String performPostAlbumRequest(AlbumClient albumClient, File imageFile, Profile profile) {
        boolean requestSuccessful = false;
        int retryCount = 1;
        int statusCode = -1;
        String albumID = "";

        while (!requestSuccessful && retryCount < MAX_RETRY_ATTEMPTS) {
            try {
                long start = System.currentTimeMillis();
                String[] res = albumClient.postAlbum(profile, imageFile);

                if (res != null && res.length == 2) {
                    statusCode = Integer.parseInt(res[0]);
                    albumID = res[1];
                    requestSuccessful = true;
                    numOfSuccessfulRequests.incrementAndGet();

                    long end = System.currentTimeMillis();
                    long latency = end - start;
                    requestRecords.add(new RequestRecord(start, "POST", latency, statusCode));
                }
            } catch (Exception e) {
                e.printStackTrace();
                numOfFailedRequests.incrementAndGet();
                retryCount++;
            }
        }
        return albumID;
    }

    public static void writeToCSV(String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append("Start Time,Request Type,Latency (ms),Response Code\n");

        for (RequestRecord requestRecord : requestRecords) {
            sb.append(requestRecord.startTime).append(",")
                    .append(requestRecord.requestType).append(",")
                    .append(requestRecord.latency).append(",")
                    .append(requestRecord.responseCode).append("\n");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void waitForCompleted(ExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void calculateAndDisplayStatistics(String requestType) {
        long sum = 0;
        long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        List<Long> responseTimes = new ArrayList<>();

        for (RequestRecord requestRecord : requestRecords) {
            if (requestRecord.requestType.equals(requestType)) {
                responseTimes.add(requestRecord.latency);
            }
        }

        for (long time : responseTimes) {
            sum += time;
            min = Math.min(min, time);
            max = Math.max(max, time);
        }

        Collections.sort(responseTimes);
        long mean = sum / responseTimes.size();
        long median = calculateMedian(responseTimes);
        long p99 = calculateP99(responseTimes);

        System.out.println("Statistics for " + requestType + ": ");
        System.out.println("mean response time: " + mean + " millisecs");
        System.out.println("median response time: " + median + " millisecs");
        System.out.println("p99 response time: " + p99 + " millisecs");
        System.out.println("min response time: " + min + " millisecs");
        System.out.println("max response time: " + max + " millisecs");
    }

    private static long calculateMedian(List<Long> responseTimes) {
        int middle = responseTimes.size() / 2;
        if (responseTimes.size() % 2 == 1) {
            return responseTimes.get(middle);
        } else {
            long left = responseTimes.get(middle - 1);
            long right = responseTimes.get(middle);
            return (left + right) / 2;
        }
    }

    private static long calculateP99(List<Long> responseTimes) {
        int index = (int) Math.ceil(99 / 100.0 * responseTimes.size()) - 1;
        return responseTimes.get(index);
    }
}


