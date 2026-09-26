package com.muni.backend.quejas.repository;

import com.muni.backend.quejas.model.HistorialEstadoQueja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialEstadoQuejaRepository extends JpaRepository<HistorialEstadoQueja, Integer> {
}
