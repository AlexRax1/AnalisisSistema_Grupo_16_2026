package com.muni.backend.quejas.repository;

import com.muni.backend.quejas.model.Dependencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DependenciaRepository extends JpaRepository<Dependencia, Integer> {
}
