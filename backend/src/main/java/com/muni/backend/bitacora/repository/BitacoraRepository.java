package com.muni.backend.bitacora.repository;

import com.muni.backend.bitacora.model.Bitacora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraRepository extends JpaRepository<Bitacora, Integer> {
}
