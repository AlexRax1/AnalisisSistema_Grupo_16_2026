package com.muni.backend.quejas.repository;

import com.muni.backend.quejas.model.CategoriaQueja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaQuejaRepository extends JpaRepository<CategoriaQueja, Integer> {
}
