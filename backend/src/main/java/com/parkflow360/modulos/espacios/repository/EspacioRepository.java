package com.parkflow360.modulos.espacios.repository;

import com.parkflow360.modulos.espacios.entity.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspacioRepository extends JpaRepository<Espacio, Long> {
    Optional<Espacio> findByNumero(String numero);
    boolean existsByNumero(String numero);
    List<Espacio> findByEstado(String estado);
    List<Espacio> findByPiso(Integer piso);
    List<Espacio> findByPlanoId(Long planoId);
    long countByPlanoId(Long planoId);
}
