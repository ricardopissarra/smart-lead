package com.rpissarra.smartleadqualification.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.status = ?1")
    List<Message> findMessagesByStatus(Status status);

    @Query("SELECT m FROM Message m WHERE m.status = ?1 AND m.createdAt >= ?2")
    List<Message> findMessagesByStatusAndCreatedAt(Status status, LocalDateTime time);
}
