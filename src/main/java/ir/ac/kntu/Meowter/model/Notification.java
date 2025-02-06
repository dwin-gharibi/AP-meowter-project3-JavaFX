package ir.ac.kntu.Meowter.model;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Instant timestamp;

    @ManyToOne
    @JoinColumn(name = "notifier_id", nullable = false)
    private User notifier;

    @ManyToOne
    @JoinColumn(name = "notifiee_id", nullable = false)
    private User notifiee;

    @Column(nullable = false)
    private Boolean checked;

    @Column(nullable = false)
    private Boolean active;

    public Notification() {}

    public Notification(String type, String content, User notifier, User notifiee) {
        this.type = type;
        this.content = content;
        this.timestamp = Instant.now();
        this.notifier = notifier;
        this.notifiee = notifiee;
        this.checked = false;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public User getNotifier() {
        return notifier;
    }

    public void setNotifier(User notifier) {
        this.notifier = notifier;
    }

    public User getNotifiee() {
        return notifiee;
    }

    public void setNotifiee(User notifiee) {
        this.notifiee = notifiee;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
