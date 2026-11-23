package com.parkflow360.modulos.espacios.repository;

import com.parkflow360.modulos.espacios.entity.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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
    List<Espacio> findByParqueaderoId(Long parqueaderoId);
    List<Espacio> findByParqueaderoIdIn(Collection<Long> parqueaderoIds);
    boolean existsByParqueaderoIdAndNumero(Long parqueaderoId, String numero);
    boolean existsByParqueaderoIdAndNumeroIgnoreCase(Long parqueaderoId, String numero);
    long countByParqueaderoIdAndTipo(Long parqueaderoId, String tipo);

    @Query("SELECT e FROM Espacio e WHERE UPPER(e.numero) IN :numeros")
    List<Espacio> findAllByNumeroIgnoreCaseIn(@Param("numeros") Collection<String> numeros);
}
