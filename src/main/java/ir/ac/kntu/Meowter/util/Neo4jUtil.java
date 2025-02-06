package ir.ac.kntu.Meowter.util;

import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Neo4jUtil {

    private final Driver driver;

    public Neo4jUtil(String uri, String user, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public void saveNotificationToGraph(Long notifierId, Long notifiableId, String type, String content) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                String query = "MERGE (notifier:User {id: $notifierId}) " +
                        "MERGE (notifiable:User {id: $notifiableId}) " +
                        "CREATE (notifier)-[:NOTIFIES {type: $type, content: $content, timestamp: datetime()}]->(notifiable)";
                tx.run(query, Map.of(
                        "notifierId", notifierId,
                        "notifiableId", notifiableId,
                        "type", type,
                        "content", content
                ));
                return null;
            });
        }
    }

    public List<Map<String, Object>> loadNotificationsForUser(Long userId) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                String query = "MATCH (notifiable:User {id: $userId})<-[:NOTIFIES]-(notifier:User) " +
                        "RETURN notifier.id AS notifierId, notifier.username AS notifierUsername, " +
                        "type, content, timestamp ORDER BY timestamp DESC";
                var result = tx.run(query, Map.of("userId", userId));
                List<Map<String, Object>> notifications = new ArrayList<>();
                while (result.hasNext()) {
                    notifications.add(result.next().asMap());
                }
                return notifications;
            });
        }
    }

    public void close() {
        driver.close();
    }
}

