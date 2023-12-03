package server.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class RMQChannelFactory extends BasePooledObjectFactory<Channel> {
    private final Connection connection;

    public RMQChannelFactory(Connection connection) {
        this.connection = connection;
    }

    @Override
    synchronized public Channel create() throws Exception {
        return connection.createChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<>(channel);
    }

    @Override
    public void destroyObject(PooledObject<Channel> p) throws Exception {
        p.getObject().close();
        super.destroyObject(p);
    }
}
