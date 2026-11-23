package com.parkflow360.modulos.ingresos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ingresos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String folio;

    @Column(nullable = false, length = 20)
    private String placa;

    @Column(nullable = false, length = 20)
    private String categoria;

    @Column(name = "parqueadero_id")
    private Long parqueaderoId;

    @Column(name = "vehiculo_id")
    private Long vehiculoId;

    @Column(name = "espacio_id")
    private Long espacioId;

    @Column(name = "espacio_numero", length = 20)
    private String espacioNumero;

    @Column(name = "tarifa_id")
    private Long tarifaId;

    @Column(name = "tarifa_valor", precision = 10, scale = 2)
    private BigDecimal tarifaValor;

    @Column(length = 30)
    private String modalidad;

    @Column(length = 100)
    private String operador;

    @Column(length = 200)
    private String novedad;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "fecha_entrada", updatable = false)
    private LocalDateTime fechaEntrada;

    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida;

    @Column(name = "fecha_reserva", updatable = false)
    private LocalDateTime fechaReserva;

    @Column(name = "fecha_limite_llegada", updatable = false)
    private LocalDateTime fechaLimiteLlegada;

    @Column(name = "fecha_llegada")
    private LocalDateTime fechaLlegada;

    @PrePersist
    protected void onCreate() {
        LocalDateTime ahora = LocalDateTime.now();
        if (fechaEntrada == null) {
            fechaEntrada = ahora;
        }
        if (fechaReserva == null) {
            fechaReserva = ahora;
        }
        if (estado == null) {
            estado = "ACTIVO";
        }
    }
}
