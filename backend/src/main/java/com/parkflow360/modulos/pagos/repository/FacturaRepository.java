package com.parkflow360.modulos.pagos.repository;

import com.parkflow360.modulos.pagos.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    Optional<Factura> findByIngresoId(Long ingresoId);
    List<Factura> findByUsuarioId(Long usuarioId);
}
