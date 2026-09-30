package com.parkflow360.modulos.pagos.repository;

import com.parkflow360.modulos.pagos.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByVehiculoId(Long vehiculoId);
    List<Pago> findByEspacioId(Long espacioId);
    List<Pago> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Pago> findByMetodoPago(String metodoPago);
}
