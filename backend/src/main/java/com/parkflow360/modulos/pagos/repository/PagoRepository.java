package com.parkflow360.modulos.pagos.repository;

import com.parkflow360.modulos.pagos.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByVehiculoId(Long vehiculoId);
    List<Pago> findByEspacioId(Long espacioId);
    List<Pago> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Pago> findByMetodoPago(String metodoPago);
    List<Pago> findByIngresoId(Long ingresoId);
    List<Pago> findByUsuarioId(Long usuarioId);
    Optional<Pago> findByReferencia(String referencia);
    List<Pago> findByParqueaderoIdIn(Collection<Long> parqueaderoIds);
    boolean existsByIngresoId(Long ingresoId);
    boolean existsByIngresoIdAndEstadoPago(Long ingresoId, String estadoPago);
}
