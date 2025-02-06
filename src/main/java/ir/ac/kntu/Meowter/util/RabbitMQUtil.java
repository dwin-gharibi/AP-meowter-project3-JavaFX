package ir.ac.kntu.Meowter.util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQUtil {

    private static final String QUEUE_NAME = "messagesQueue";
    private static final String EXCHANGE_NAME = "messagesExchange";

    private static ConnectionFactory factory;
    private static Connection connection;

    static {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        try {
            connection = factory.newConnection();
        } catch (Exception e) {
            throw new RuntimeException("Failed to establish RabbitMQ connection", e);
        }
    }

    public static Connection getConnection() throws Exception {
        return connection;
    }

    public static Channel getChannel() {
        try {
            return connection.createChannel();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create RabbitMQ channel", e);
        }
    }

    public static void sendMessage(String message) {
        try (Channel channel = getChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout", true);
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
            System.out.println("Message sent to RabbitMQ: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initializeQueue() {
        try (Channel channel = getChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout", true);
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
