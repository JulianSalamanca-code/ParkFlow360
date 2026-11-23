package com.parkflow360.modulos.parqueaderos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "parqueaderos_tipos_vehiculo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParqueaderoTipoVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parqueadero_id", nullable = false)
    private Long parqueaderoId;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(name = "cantidad_fija", nullable = false)
    private boolean cantidadFija;

    @Column
    private Integer cantidad;
}
