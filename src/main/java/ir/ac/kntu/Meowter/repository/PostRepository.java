package ir.ac.kntu.Meowter.repository;

import ir.ac.kntu.Meowter.model.Like;
import ir.ac.kntu.Meowter.model.Post;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.model.Comment;
import ir.ac.kntu.Meowter.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class PostRepository {

    public void save(Post post) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            post.extractHashtags();
            session.save(post);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
        } finally {
            session.close();
        }
    }

    public void update(Post post) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.update(post);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
        } finally {
            session.close();
        }
    }

    public void delete(Long postId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            Post post = session.get(Post.class, postId);
            if (post != null) {
                session.delete(post);
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

    public List<Post> findByHashtag(String hashtag) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Post> posts = null;

        try {
            String hql = "SELECT p FROM Post p JOIN p.hashtags h WHERE h = :hashtag";
            posts = session.createQuery(hql, Post.class)
                    .setParameter("hashtag", hashtag)
                    .getResultList();
        } finally {
            session.close();
        }
        return posts;
    }

    public Post findById(Long postId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Post post = null;

        try {
            post = session.get(Post.class, postId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return post;
    }

    public Comment findByIdComment(Long commentId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Comment comment = null;

        try {
            comment = session.get(Comment.class, commentId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return comment;
    }

    public List<Post> findAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Post> posts = null;

        try {
            posts = session.createQuery("FROM Post", Post.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return posts;
    }

    public List<Comment> findAllComments() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Comment> comments = null;

        try {
            comments = session.createQuery("FROM Comment ", Comment.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return comments;
    }

    public List<Post> findByUser(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Post> posts = null;

        try {
            String hql = "FROM Post p WHERE p.user = :user";
            posts = session.createQuery(hql, Post.class)
                    .setParameter("user", user)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return posts;
    }

    public long count() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        long count = (long) session.createQuery("SELECT COUNT(p) FROM Post p").getSingleResult();
        session.close();
        return count;
    }

    public List<Post> findAllPaginated(int page, int size) {
        return null;
    }

    public void addLike(User user, Post post) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            if (post.getLikes().stream().anyMatch(like -> like.getUser().equals(user))) {
                System.out.println("User already liked this post.");
                return;
            }
            Like like = new Like(user, post);
            session.save(like);
            post.addLike(like);
            session.update(post);
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

    public void addComment(User user, Post post, String content) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            Comment comment = new Comment(content, post, user);
            session.save(comment);
            post.addComment(comment);
            session.update(post);
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

    public void addComment(User user, Comment comment, String content) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            Post post = comment.getPost();
            Comment new_comment = new Comment(content, post, user, comment);
            session.save(new_comment);
            post.addComment(new_comment);
            session.update(post);
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

    public void removeComment(User user, Post post, long commentId) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            Post managedPost = session.get(Post.class, post.getId());

            if (managedPost != null) {
                Comment commentToRemove = managedPost.getComments().stream()
                        .filter(comment -> comment.getId() == commentId && comment.getUser().equals(user))
                        .findFirst()
                        .orElse(null);

                if (commentToRemove != null) {
                    managedPost.getComments().remove(commentToRemove);
                    session.delete(commentToRemove);
                    transaction.commit();
                } else {
                    System.out.println("Comment not found or user is not authorized to delete.");
                }
            } else {
                System.out.println("Post not found.");
            }

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }


    public void removeLike(User user, Post post) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            Like like = post.getLikes().stream()
                    .filter(l -> l.getUser().equals(user))
                    .findFirst()
                    .orElse(null);
            if (like != null) {
                post.removeLike(like);
                session.delete(like);
                session.update(post);
                transaction.commit();
            } else {
                System.out.println("Like not found.");
            }
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

