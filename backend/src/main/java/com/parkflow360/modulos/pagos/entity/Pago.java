package com.parkflow360.modulos.pagos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehiculo_id")
    private Long vehiculoId;

    @Column(name = "espacio_id")
    private Long espacioId;

    @Column(name = "tarifa_id")
    private Long tarifaId;

    @Column(name = "ingreso_id")
    private Long ingresoId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Builder.Default
    @Column(name = "estado_pago", nullable = false, length = 20)
    private String estadoPago = "CONFIRMADO";

    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
        if (estadoPago == null) {
            estadoPago = "CONFIRMADO";
        }
    }
}
