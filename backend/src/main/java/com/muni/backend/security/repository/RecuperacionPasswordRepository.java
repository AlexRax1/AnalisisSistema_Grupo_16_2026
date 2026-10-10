package com.muni.backend.security.repository;

import com.muni.backend.security.model.RecuperacionPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecuperacionPasswordRepository extends JpaRepository<RecuperacionPassword, Integer> {

    Optional<RecuperacionPassword> findTopByCredencial_UserIdAndCodigoVerificacionAndUtilizadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
            Integer userId, String codigoVerificacion, LocalDateTime ahora);

    Optional<RecuperacionPassword> findTopByCredencial_UserIdAndUtilizadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
            Integer userId, LocalDateTime ahora);

    List<RecuperacionPassword> findByCredencial_UserIdAndUtilizadoFalse(Integer userId);
}
