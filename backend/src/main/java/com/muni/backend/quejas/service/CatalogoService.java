package com.muni.backend.quejas.service;

import com.muni.backend.quejas.model.CategoriaQueja;
import com.muni.backend.quejas.model.SubcategoriaQueja;
import com.muni.backend.quejas.repository.CategoriaQuejaRepository;
import com.muni.backend.quejas.repository.SubcategoriaQuejaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final CategoriaQuejaRepository categoriaRepository;
    private final SubcategoriaQuejaRepository subcategoriaRepository;

    public String obtenerNombreCategoria(Integer categoriaId) {
        if (categoriaId == null) return null;
        return categoriaRepository.findById(categoriaId)
                .map(CategoriaQueja::getNombreCategoria)
                .orElse("Categoría " + categoriaId);
    }

    public String obtenerNombreSubcategoria(Integer subcategoriaId) {
        if (subcategoriaId == null) return null;
        return subcategoriaRepository.findById(subcategoriaId)
                .map(SubcategoriaQueja::getNombreSubcategoria)
                .orElse("Subcategoría " + subcategoriaId);
    }
}
