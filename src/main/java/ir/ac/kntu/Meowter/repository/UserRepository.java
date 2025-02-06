package ir.ac.kntu.Meowter.repository;

import ir.ac.kntu.Meowter.model.FollowRequest;
import ir.ac.kntu.Meowter.model.Notification;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.model.FollowRequestStatus;
import ir.ac.kntu.Meowter.util.HibernateUtil;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class UserRepository {

    public void save(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.saveOrUpdate(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public void update(User user) {
        save(user);
    }

    public List<User> findAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<User> users = session.createQuery("FROM User", User.class).getResultList();
        session.close();
        return users;
    }

    public long count() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        long count = (long) session.createQuery("SELECT COUNT(u) FROM User u").getSingleResult();
        session.close();
        return count;
    }

    public boolean existsByEmail(String email) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        boolean exists = false;

        try {
            String hql = "SELECT COUNT(u) FROM User u WHERE u.email = :email";
            Long count = (Long) session.createQuery(hql)
                    .setParameter("email", email)
                    .uniqueResult();
            exists = count > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return exists;
    }

    public User findByUsername(String username) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = null;
        try {
            user = session.createQuery("FROM User WHERE username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();

            if (user != null) {
                Hibernate.initialize(user.getFollowers());
                Hibernate.initialize(user.getFollowing());
            }

        } finally {
            session.close();
        }
        return user;
    }

    public User findById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = null;
        try {
            user = session.createQuery("FROM User WHERE id = :id", User.class)
                    .setParameter("id", id)
                    .uniqueResult();
        } finally {
            session.close();
        }
        return user;
    }

    public User findByEmail(String email) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = null;

        try {
            user = session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResult();
        } finally {
            session.close();
        }

        return user;
    }

    public FollowRequest findFollowRequest(User requester, User recipient) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        FollowRequest followRequest = null;

        try {
            String hql = "FROM FollowRequest fr WHERE fr.requester = :requester AND fr.recipient = :recipient";
            followRequest = session.createQuery(hql, FollowRequest.class)
                    .setParameter("requester", requester)
                    .setParameter("recipient", recipient)
                    .uniqueResult();
        } finally {
            session.close();
        }

        return followRequest;
    }

    public void saveFollowRequest(FollowRequest followRequest) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.saveOrUpdate(followRequest);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public List<FollowRequest> getFollowRequests(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<FollowRequest> requests = null;

        try {
            String hql = "FROM FollowRequest fr WHERE fr.recipient = :user OR fr.requester = :user";
            requests = session.createQuery(hql, FollowRequest.class)
                    .setParameter("user", user)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return requests;
    }


    public void updateFollowRequest(FollowRequest followRequest) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.merge(followRequest);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public void saveNotification(Notification notification) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.saveOrUpdate(notification);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public List<Notification> getNotifications(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Notification> notifications = null;

        try {
            String hql = "FROM Notification nt WHERE nt.notifiee = :user";
            notifications = session.createQuery(hql, Notification.class)
                    .setParameter("user", user)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return notifications;
    }

    public List<Notification> getAllNotifications() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Notification> notifications = null;

        try {
            String hql = "FROM Notification";
            notifications = session.createQuery(hql, Notification.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return notifications;
    }

    public Notification getNotificationById(int id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Notification> notifications = null;

        try {
            String hql = "FROM Notification nt WHERE nt.id = :id";
            notifications = session.createQuery(hql, Notification.class)
                    .setParameter("id", (long) id)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return notifications != null && !notifications.isEmpty() ? notifications.get(0) : null;
    }



}
