package ir.ac.kntu.Meowter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private boolean isWarned;

    @Enumerated(EnumType.STRING)
    private TicketSubject subject;

    private String username;

    private String response;

    private String reportUsername;

    private String reportWarning;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_department", nullable = true)
    @JsonIgnore
    private Set<Department> departments = new HashSet<>();

    public Ticket() {}

    public Ticket(String description, TicketSubject subject, String username) {
        this.description = description;
        this.subject = subject;
        this.username = username;
        this.status = TicketStatus.SUBMITTED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isWarned = false;
    }

    public boolean getIsWarned(){
        return this.isWarned;
    }

    public void setIsWarned(boolean isWarned){
        this.isWarned = isWarned;
    }

    public void setReportUsername(String reportUsername) {
        this.reportUsername = reportUsername;
    }

    public String getReportUsername() {
        return reportUsername;
    }

    public void setReportWarning(String reportWarning) {
        this.reportWarning = reportWarning;
    }

    public String getReportWarning() {
        return reportWarning;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketSubject getSubject() {
        return subject;
    }

    public void setSubject(TicketSubject subject) {
        this.subject = subject;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getResponse() {
        return response == null ? "There is no response yet." : response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(Set<Department> departments) {
        this.departments = departments;
    }
}
