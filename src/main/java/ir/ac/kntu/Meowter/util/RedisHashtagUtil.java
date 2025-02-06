package ir.ac.kntu.Meowter.util;

import redis.clients.jedis.Jedis;

import java.util.concurrent.atomic.AtomicInteger;

public class RedisHashtagUtil {

    private static final String TOP_HASHTAGS_KEY = "top_hashtags";

    public static void incrementHashtag(String hashtag) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            jedis.zincrby(TOP_HASHTAGS_KEY, 1, hashtag);
        }
    }

    public static void displayTopHashtags(int n) {
        AtomicInteger cnt = new AtomicInteger();
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            jedis.zrevrangeWithScores(TOP_HASHTAGS_KEY, 0, n - 1).forEach(entry -> {
                System.out.printf(CliFormatter.boldRed("%s (%d times)\n"), entry.getElement(), Math.round(entry.getScore()));
                cnt.getAndIncrement();
            });
            if (cnt.get() == 0) {
                System.out.println(CliFormatter.boldPurple("- There is no top #hashtag."));
            }
        }

    }

    public static void resetTopHashtags() {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            jedis.del(TOP_HASHTAGS_KEY);
        }
    }
}

