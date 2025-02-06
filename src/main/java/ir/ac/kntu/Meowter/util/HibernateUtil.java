package ir.ac.kntu.Meowter.util;

import ir.ac.kntu.Meowter.model.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(Post.class)
                    .addAnnotatedClass(Like.class)
                    .addAnnotatedClass(Comment.class)
                    .addAnnotatedClass(Notification.class)
                    .addAnnotatedClass(Message.class)
                    .addAnnotatedClass(Ticket.class)
                    .addAnnotatedClass(FollowRequest.class)
                    .buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
