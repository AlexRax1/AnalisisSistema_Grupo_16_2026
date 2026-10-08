package com.muni.backend.quejas.repository;

import com.muni.backend.quejas.model.SubcategoriaQueja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubcategoriaQuejaRepository extends JpaRepository<SubcategoriaQueja, Integer> {
    List<SubcategoriaQueja> findByCategoria_CategoriaId(Integer categoriaId);
}
