package com.parkflow360.modulos.planos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "planos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column
    private Integer piso;

    @Column(nullable = false)
    private Integer filas;

    @Column(nullable = false)
    private Integer columnas;

    @Column
    private Integer ancho;

    @Column
    private Integer alto;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (filas == null) filas = 0;
        if (columnas == null) columnas = 0;
    }
}
