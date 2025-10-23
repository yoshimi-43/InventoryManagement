package com.example.InventoryManagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.InventoryManagement.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}