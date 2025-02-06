package ir.ac.kntu.Meowter.repository;

import ir.ac.kntu.Meowter.model.Comment;
import ir.ac.kntu.Meowter.model.Message;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class MessageRepository {

    public void save(Message message) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.saveOrUpdate(message);
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

    public List<Message> findBySender(User sender) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Message> messages = null;

        try {
            String hql = "FROM Message m WHERE m.sender = :sender";
            messages = session.createQuery(hql, Message.class)
                    .setParameter("sender", sender)
                    .getResultList();
        } finally {
            session.close();
        }

        return messages;
    }

    public List<Message> findByRecipient(User recipient) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Message> messages = null;

        try {
            String hql = "FROM Message m WHERE m.recipient = :recipient";
            messages = session.createQuery(hql, Message.class)
                    .setParameter("recipient", recipient)
                    .getResultList();
        } finally {
            session.close();
        }

        return messages;
    }

    public List<Message> findConversation(User user1, User user2) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Message> conversation = null;

        try {
            String hql = "FROM Message m WHERE (m.sender = :user1 AND m.recipient = :user2) " +
                    "OR (m.sender = :user2 AND m.recipient = :user1) ORDER BY m.timestamp";
            conversation = session.createQuery(hql, Message.class)
                    .setParameter("user1", user1)
                    .setParameter("user2", user2)
                    .getResultList();
        } finally {
            session.close();
        }

        return conversation;
    }

    public void markAsRead(Long messageId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            Message message = session.get(Message.class, messageId);
            if (message != null) {
                message.setRead(true);
                session.update(message);
            }
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

    public List<User> findSendersByRecipient(User recipient) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<User> senders = null;

        try {
            String hql = "SELECT DISTINCT m.sender FROM Message m WHERE m.recipient = :recipient";
            senders = session.createQuery(hql, User.class)
                    .setParameter("recipient", recipient)
                    .getResultList();
        } finally {
            session.close();
        }

        return senders;
    }

    public List<Message> findAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Message> messages = null;

        try {
            messages = session.createQuery("FROM Message ", Message.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return messages;
    }

    public List<User> findRecipientsBySender(User sender) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<User> recipients = null;

        try {
            String hql = "SELECT DISTINCT m.recipient FROM Message m WHERE m.sender = :sender";
            recipients = session.createQuery(hql, User.class)
                    .setParameter("sender", sender)
                    .getResultList();
        } finally {
            session.close();
        }

        return recipients;
    }
}

