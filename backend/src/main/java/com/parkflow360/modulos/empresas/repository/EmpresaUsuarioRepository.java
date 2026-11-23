package com.parkflow360.modulos.empresas.repository;

import com.parkflow360.modulos.empresas.entity.EmpresaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpresaUsuarioRepository extends JpaRepository<EmpresaUsuario, Long> {
    List<EmpresaUsuario> findByUsuarioId(Long usuarioId);
    boolean existsByEmpresaIdAndUsuarioId(Long empresaId, Long usuarioId);
}
