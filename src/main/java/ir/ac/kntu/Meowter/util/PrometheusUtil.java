package ir.ac.kntu.Meowter.util;


import io.prometheus.metrics.core.metrics.Counter;

public class PrometheusUtil {

    public static final Counter POST_CREATED = Counter.builder()
            .name("post_added_total")
            .help("Total number of posts created.")
            .labelNames("user")
            .register();

    public static final Counter COMMENT_ADDED = Counter.builder()
            .name("comment_added_total")
            .help("Total number of comments added.")
            .labelNames("user")
            .register();

    public static final Counter LIKE_ADDED = Counter.builder()
            .name("like_added_total")
            .help("Total number of likes added.")
            .labelNames("user")
            .register();

    public static final Counter LIKE_REMOVED = Counter.builder()
            .name("like_removed_total")
            .help("Total number of likes removed.")
            .labelNames("user")
            .register();

    public static final Counter POST_LIKE_TOTAL = Counter.builder()
            .name("post_like_total")
            .help("Total number of likes on posts.")
            .labelNames("username", "post_id")
            .register();

    public static final Counter POST_COMMENT_TOTAL = Counter.builder()
            .name("post_comment_total")
            .help("Total number of comments on posts.")
            .labelNames("username", "post_id")
            .register();

    public static final Counter USER_ACTIVE_TIME = Counter.builder()
            .name("user_active_time_total")
            .help("Total time a user spends actively.")
            .labelNames("username", "state")
            .register();

}
