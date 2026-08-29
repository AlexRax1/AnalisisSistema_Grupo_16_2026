package com.muni.backend.security.repository;

import com.muni.backend.security.model.RolUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolUserRepository extends JpaRepository<RolUser, Integer> {
    Optional<RolUser> findByNombreRol(String nombreRol);
}
