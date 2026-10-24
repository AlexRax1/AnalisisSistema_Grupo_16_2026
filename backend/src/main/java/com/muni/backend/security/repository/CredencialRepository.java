package com.muni.backend.security.repository;

import com.muni.backend.security.model.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredencialRepository extends JpaRepository<Credencial, Integer> {

    Optional<Credencial> findByUsername(String username);
    Optional<Credencial> findByUsernameIgnoreCase(String username);
}