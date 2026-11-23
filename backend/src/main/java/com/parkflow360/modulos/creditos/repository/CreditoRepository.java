package com.parkflow360.modulos.creditos.repository;

import com.parkflow360.modulos.creditos.entity.Credito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditoRepository extends JpaRepository<Credito, Long> {
    List<Credito> findByUsuarioId(Long usuarioId);
    Optional<Credito> findByFacturaId(Long facturaId);
    boolean existsByUsuarioIdAndEstado(Long usuarioId, String estado);
    List<Credito> findByUsuarioIdAndEstadoAndFechaLimiteBefore(
            Long usuarioId, String estado, LocalDateTime fecha);
}
