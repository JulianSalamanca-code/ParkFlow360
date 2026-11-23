package com.parkflow360.modulos.parqueaderos.repository;

import com.parkflow360.modulos.parqueaderos.entity.ParqueaderoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParqueaderoUsuarioRepository extends JpaRepository<ParqueaderoUsuario, Long> {
    List<ParqueaderoUsuario> findByUsuarioId(Long usuarioId);
    List<ParqueaderoUsuario> findByParqueaderoId(Long parqueaderoId);
    boolean existsByParqueaderoIdAndUsuarioIdAndRolAsignado(
            Long parqueaderoId, Long usuarioId, String rolAsignado);
    boolean existsByUsuarioIdAndRolAsignado(Long usuarioId, String rolAsignado);
    Optional<ParqueaderoUsuario> findByUsuarioIdAndRolAsignado(Long usuarioId, String rolAsignado);
}
