package com.parkflow360.modulos.parqueaderos.repository;

import com.parkflow360.modulos.parqueaderos.entity.Parqueadero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParqueaderoRepository extends JpaRepository<Parqueadero, Long> {
    Optional<Parqueadero> findByIdentificador(String identificador);
    boolean existsByIdentificador(String identificador);
    List<Parqueadero> findByEmpresaId(Long empresaId);
    List<Parqueadero> findByEmpresaIdIn(Collection<Long> empresaIds);
}
