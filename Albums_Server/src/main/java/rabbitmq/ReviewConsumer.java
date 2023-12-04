package rabbitmq;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import org.apache.commons.pool2.impl.GenericObjectPool;
import database.DatabaseConnection;
import database.ReviewDao;
import data_models.Review;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ReviewConsumer implements Runnable {
    private static final String EXCHANGE_NAME = "reviewsExchange";
    private GenericObjectPool<Channel> channelPool;
    private final ReviewDao reviewDao = new ReviewDao(DatabaseConnection.getDataSource());
    private final ExecutorService executorService;

    public ReviewConsumer(int numThreads) {
        this.executorService = Executors.newFixedThreadPool(numThreads);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("ec2-54-212-83-112.us-west-2.compute.amazonaws.com");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection connection;
        try {
//            System.out.println("[review consumer] new connection...");

            connection = factory.newConnection();
            channelPool = new GenericObjectPool<>(new RMQChannelFactory(connection));

//            System.out.println("[review consumer] channelPool created...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Channel channel = channelPool.borrowObject();
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, EXCHANGE_NAME, "");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                executorService.execute(() -> processReviewMessage(message));
            };

            channel.basicConsume(queue, true, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processReviewMessage(String message) {
        try {
            Gson gson = new Gson();
            Review review = gson.fromJson(message, Review.class);
            if (review != null) {
                reviewDao.createReview(review.getAlbumID(), review.getLikeOrNot());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}