package ir.ac.kntu.Meowter.repository;

import ir.ac.kntu.Meowter.model.Post;
import ir.ac.kntu.Meowter.model.Ticket;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import ir.ac.kntu.Meowter.util.HibernateUtil;

import java.util.List;

public class TicketRepository {

    public void save(Ticket ticket) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.save(ticket);
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

    public List<Ticket> findByUsername(String username) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Ticket> tickets = null;
        try {
            Query<Ticket> query = session.createQuery("FROM Ticket WHERE username = :username", Ticket.class);
            query.setParameter("username", username);
            tickets = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return tickets;
    }

    public Ticket findById(long ticketId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Ticket ticket = null;
        try {
            ticket = session.get(Ticket.class, ticketId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return ticket;
    }

    public List<Ticket> findAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Ticket> tickets = null;

        try {
            tickets = session.createQuery("FROM Ticket ", Ticket.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return tickets;
    }

    public void update(Ticket ticket) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.update(ticket);
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
}
