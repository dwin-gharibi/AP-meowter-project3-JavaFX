package ir.ac.kntu.Meowter.model;

import ir.ac.kntu.Meowter.util.RedisHashtagUtil;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Like> likes = new HashSet<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_hashtags", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "hashtag")
    private Set<String> hashtags = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "post_labels", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "label")
    private Set<PostLabel> labels = new HashSet<>();

    public Set<PostLabel> getLabels() {
        return labels;
    }

    public void setLabels(Set<PostLabel> labels) {
        this.labels = labels;
    }


    public Set<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(Set<String> hashtags) {
        this.hashtags = hashtags;
    }

    public void extractHashtags() {
        this.hashtags = Arrays.stream(content.split("\\s+"))
                .filter(word -> word.startsWith("#"))
                .map(word -> word.replaceAll("[^a-zA-Z0-9_#]", ""))
                .collect(Collectors.toSet());

        this.hashtags.forEach(RedisHashtagUtil::incrementHashtag);
    }


    public Post() {}

    public Post(String content, User user) {
        this.content = content;
        this.user = user;
        this.createdAt = new Date();
    }

    public Post(String content, User user, String labels) {
        this.content = content;
        this.user = user;
        this.createdAt = new Date();
        List<String> labelList = Arrays.asList(labels.split(","));
        for (String label : labelList) {
            this.addLabel(PostLabel.valueOf(label));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public Set<Like> getLikes() {
        return likes;
    }

    public void setLikes(Set<Like> likes) {
        this.likes = likes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Post post = (Post) obj;
        return id != null && id.equals(post.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        comment.setPost(null);
    }

    public void addLike(Like like) {
        this.likes.add(like);
        like.setPost(this);
    }

    public void removeLike(Like like) {
        this.likes.remove(like);
        like.setPost(null);
    }

    public void addLabel(PostLabel label) {
        this.labels.add(label);
    }

    public void removeLabel(PostLabel label) {
        this.labels.remove(label);
    }
}
