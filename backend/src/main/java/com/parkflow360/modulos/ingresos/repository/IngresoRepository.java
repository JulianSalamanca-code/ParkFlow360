package com.parkflow360.modulos.ingresos.repository;

import com.parkflow360.modulos.ingresos.entity.Ingreso;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IngresoRepository extends JpaRepository<Ingreso, Long> {
    Optional<Ingreso> findByFolio(String folio);
    List<Ingreso> findByEstado(String estado);
    List<Ingreso> findByPlaca(String placa);
    long countByEstado(String estado);
    List<Ingreso> findByUsuarioId(Long usuarioId);
    List<Ingreso> findByUsuarioIdAndEstado(Long usuarioId, String estado);
    List<Ingreso> findByUsuarioIdAndEstadoIn(Long usuarioId, Collection<String> estados);
    List<Ingreso> findByEspacioId(Long espacioId);
    Optional<Ingreso> findFirstByEspacioIdAndEstadoIn(Long espacioId, Collection<String> estados);
    boolean existsByEspacioIdAndEstadoIn(Long espacioId, Collection<String> estados);
    boolean existsByUsuarioIdAndEstadoIn(Long usuarioId, Collection<String> estados);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Ingreso i WHERE i.id = :id")
    Optional<Ingreso> findByIdForUpdate(@Param("id") Long id);
}
