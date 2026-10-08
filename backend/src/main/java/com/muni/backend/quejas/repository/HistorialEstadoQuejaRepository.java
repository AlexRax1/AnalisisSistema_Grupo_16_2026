package com.muni.backend.quejas.repository;

import com.muni.backend.quejas.model.HistorialEstadoQueja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.muni.backend.quejas.model.Queja;
import java.util.List;

@Repository
public interface HistorialEstadoQuejaRepository extends JpaRepository<HistorialEstadoQueja, Integer> {
    List<HistorialEstadoQueja> findByQuejaOrderByFechaCambioAsc(Queja queja);
}
