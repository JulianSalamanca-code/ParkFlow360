package com.parkflow360.modulos.empresas.repository;

import com.parkflow360.modulos.empresas.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByIdentificador(String identificador);
    boolean existsByIdentificador(String identificador);
}
