package ir.ac.kntu.Meowter.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisUtil {
    private final Jedis jedis;

    public RedisUtil() {
        jedis = new Jedis("localhost", 6379);
    }

    public void publish(String channel, String message) {
        jedis.publish(channel, message);
    }

    public void subscribe(String channel, JedisPubSub listener) {
        new Thread(() -> {
            jedis.subscribe(listener, channel);
        }).start();
    }
}
