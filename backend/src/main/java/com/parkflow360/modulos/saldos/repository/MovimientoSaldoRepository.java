package com.parkflow360.modulos.saldos.repository;

import com.parkflow360.modulos.saldos.entity.MovimientoSaldo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoSaldoRepository extends JpaRepository<MovimientoSaldo, Long> {
    List<MovimientoSaldo> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
