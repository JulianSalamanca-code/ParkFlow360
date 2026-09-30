package com.parkflow360.modulos.tarifas.repository;

import com.parkflow360.modulos.tarifas.entity.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
    List<Tarifa> findByTipo(String tipo);
    List<Tarifa> findByNombreContaining(String nombre);
}
