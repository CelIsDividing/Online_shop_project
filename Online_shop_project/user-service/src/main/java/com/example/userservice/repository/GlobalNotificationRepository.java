package com.example.userservice.repository;

import com.example.userservice.model.GlobalNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GlobalNotificationRepository extends JpaRepository<GlobalNotification, Long> {

    List<GlobalNotification> findAllByOrderByCreatedAtDesc();
}
