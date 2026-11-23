package com.parkflow360.modulos.planos.repository;

import com.parkflow360.modulos.planos.entity.Plano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PlanoRepository extends JpaRepository<Plano, Long> {
    List<Plano> findByParqueaderoId(Long parqueaderoId);
    List<Plano> findByParqueaderoIdIn(Collection<Long> parqueaderoIds);
}
