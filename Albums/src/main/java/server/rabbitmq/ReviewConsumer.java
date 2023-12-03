package server.rabbitmq;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import org.apache.commons.pool2.impl.GenericObjectPool;
import server.database.DatabaseConnection;
import server.database.ReviewDao;
import service_interface.Review;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ReviewConsumer implements Runnable {
    private GenericObjectPool<Channel> channelPool;
    private final ReviewDao reviewDao = new ReviewDao(DatabaseConnection.getDataSource());
    private final ExecutorService executorService;

    public ReviewConsumer(int numThreads) {
        this.executorService = Executors.newFixedThreadPool(numThreads);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection connection;
        try {
            connection = factory.newConnection();
            channelPool = new GenericObjectPool<>(new RMQChannelFactory(connection));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Channel channel = channelPool.borrowObject();
            String exchange = "reviewsExchange";
            channel.exchangeDeclare(exchange, "fanout");

            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, exchange, "");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                executorService.execute(() -> processReviewMessage(channel, message, delivery.getEnvelope().getDeliveryTag()));
            };

            channel.basicConsume(queue, true, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processReviewMessage(Channel channel, String message, long delivery) {
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
        System.out.println("consumer shutdown");
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}