package com.muni.backend.quejas.repository;

import com.muni.backend.quejas.model.InformeQueja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InformeQuejaRepository extends JpaRepository<InformeQueja, Integer> {
}
