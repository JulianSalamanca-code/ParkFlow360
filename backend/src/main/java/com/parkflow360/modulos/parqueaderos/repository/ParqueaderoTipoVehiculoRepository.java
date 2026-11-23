package com.parkflow360.modulos.parqueaderos.repository;

import com.parkflow360.modulos.parqueaderos.entity.ParqueaderoTipoVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParqueaderoTipoVehiculoRepository extends JpaRepository<ParqueaderoTipoVehiculo, Long> {
    List<ParqueaderoTipoVehiculo> findByParqueaderoId(Long parqueaderoId);
    boolean existsByParqueaderoIdAndTipo(Long parqueaderoId, String tipo);
}
