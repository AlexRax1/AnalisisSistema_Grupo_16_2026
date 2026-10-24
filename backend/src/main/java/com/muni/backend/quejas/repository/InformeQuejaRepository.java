package com.muni.backend.quejas.repository;

import com.muni.backend.quejas.model.InformeQueja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.muni.backend.quejas.model.Queja;
import java.util.List;

@Repository
public interface InformeQuejaRepository extends JpaRepository<InformeQueja, Integer> {
    List<InformeQueja> findByQuejaOrderByFechaRegistroAsc(Queja queja);
}
