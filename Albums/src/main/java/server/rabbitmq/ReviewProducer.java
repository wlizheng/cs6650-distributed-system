package server.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class ReviewProducer {
    private static final String EXCHANGE_NAME = "reviewsExchange";
    private GenericObjectPool<Channel> channelPool;

    public void init() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("ec2-35-85-59-79.us-west-2.compute.amazonaws.com");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection connection;
        try {
            System.out.println("[review producer] new connection...");

            connection = factory.newConnection();
            channelPool = new GenericObjectPool<>(new RMQChannelFactory(connection));

            System.out.println("[review producer] channelPool created...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publishReview(String reviewJson) {
        Channel channel = null;

        try {
            channel = channelPool.borrowObject();
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

            channel.basicPublish(EXCHANGE_NAME, "", MessageProperties.PERSISTENT_TEXT_PLAIN, reviewJson.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                try {
                    channelPool.returnObject(channel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void close() {
        try {
            channelPool.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}