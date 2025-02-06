package ir.ac.kntu.Meowter.util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

public class RabbitMQListener {

    private static final String QUEUE_NAME = "chat_messages";

    public static void startListening() {
        try {
            Channel channel = RabbitMQUtil.getChannel();

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("\n[New Message] " + message);
                System.out.print(">> ");
            };

            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to listen to RabbitMQ messages", e);
        }
    }
}
