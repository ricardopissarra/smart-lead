package com.rpissarra.smartleadqualification.lead;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rpissarra.smartleadqualification.message.Message;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;


@Builder
@Entity
@Table(name = "leads")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lead_id")
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Enumerated(EnumType.STRING)
    private UrgencyLevel urgencyLevel;

    private String description;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "message_id")
    private Message message;

    @JsonIgnore
    private LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime updatedAt;

    public Lead() {
    }

    public Lead(Long id, String title, Type type, UrgencyLevel urgencyLevel, String description, Message message, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.urgencyLevel = urgencyLevel;
        this.description = description;
        this.message = message;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public UrgencyLevel getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(UrgencyLevel urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
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

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Lead lead = (Lead) o;
        return Objects.equals(id, lead.id) && Objects.equals(title, lead.title) && type == lead.type && urgencyLevel == lead.urgencyLevel && Objects.equals(description, lead.description) && Objects.equals(message, lead.message) && Objects.equals(createdAt, lead.createdAt) && Objects.equals(updatedAt, lead.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, type, urgencyLevel, description, message, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Lead{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", urgencyLevel=" + urgencyLevel +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
