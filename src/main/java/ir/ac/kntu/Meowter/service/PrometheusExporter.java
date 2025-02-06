package ir.ac.kntu.Meowter.service;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import ir.ac.kntu.Meowter.repository.PostRepository;
import ir.ac.kntu.Meowter.util.PrometheusUtil;

import java.io.IOException;

public class PrometheusExporter {

    private PostRepository postRepository;

    public PrometheusExporter() {
        this.postRepository = new PostRepository();
    }

    public void startExporter() throws IOException {
        HTTPServer server = HTTPServer.builder()
                .port(9091)
                .buildAndStart();

        Counter postCreatedCounter = PrometheusUtil.POST_CREATED;
        Counter commentAddedCounter = PrometheusUtil.COMMENT_ADDED;
        Counter likeAddedCounter = PrometheusUtil.LIKE_ADDED;
        Counter likeRemovedCounter = PrometheusUtil.LIKE_REMOVED;

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
