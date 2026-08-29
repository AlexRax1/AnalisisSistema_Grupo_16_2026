package com.muni.backend.quejas.repository;

import com.muni.backend.quejas.model.EvidenciaDigital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenciaDigitalRepository extends JpaRepository<EvidenciaDigital, Integer> {
    List<EvidenciaDigital> findByQueja_QuejaId(Integer quejaId);
}