package com.parkflow360.modulos.espacios.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "espacios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Espacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column
    private Integer piso;

    // ---- Ubicación dentro de un plano ----

    @Column(name = "plano_id")
    private Long planoId;

    @Column
    private Integer fila;

    @Column
    private Integer columna;

    @Column(name = "pos_x")
    private Double posX;

    @Column(name = "pos_y")
    private Double posY;

    @Column
    private Double ancho;

    @Column
    private Double alto;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
