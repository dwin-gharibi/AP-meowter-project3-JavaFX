package ir.ac.kntu.Meowter.repository;

import org.neo4j.driver.*;

public class Neo4jSession {
    private Driver driver;
    private Session session;

    public Neo4jSession(Driver driver) {
        this.driver = driver;
        this.session = driver.session();
    }

    public Session getSession() {
        return session;
    }

    public Transaction beginTransaction() {
        return session.beginTransaction();
    }

    public void close() {
        if (session != null) {
            session.close();
        }
    }
}
