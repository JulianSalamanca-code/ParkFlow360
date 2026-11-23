package com.parkflow360.modulos.pagos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "facturas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingreso_id", nullable = false)
    private Long ingresoId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "fecha_emision", nullable = false, updatable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @PrePersist
    protected void onCreate() {
        if (fechaEmision == null) {
            fechaEmision = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }
}
