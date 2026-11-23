package com.parkflow360.modulos.creditos.entity;

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
@Table(name = "creditos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "factura_id", nullable = false)
    private Long facturaId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "monto_pendiente", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoPendiente;

    @Column(name = "fecha_otorgamiento", nullable = false, updatable = false)
    private LocalDateTime fechaOtorgamiento;

    @Column(name = "fecha_limite", nullable = false)
    private LocalDateTime fechaLimite;

    @Column(nullable = false, length = 20)
    private String estado;

    @PrePersist
    protected void onCreate() {
        if (fechaOtorgamiento == null) {
            fechaOtorgamiento = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }
}
