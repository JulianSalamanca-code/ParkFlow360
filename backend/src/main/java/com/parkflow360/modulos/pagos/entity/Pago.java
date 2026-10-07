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

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    /** PENDIENTE | PAGADO | RECHAZADO | EFECTIVO */
    @Column(length = 20)
    private String estado;

    @Column(length = 100)
    private String referencia;

    @Column(name = "wompi_transaction_id", length = 100)
    private String wompiTransactionId;

    @Column(length = 10)
    private String moneda;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "ingreso_id")
    private Long ingresoId;

    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "PAGADO";
        }
        if (moneda == null) {
            moneda = "COP";
        }
    }
}
