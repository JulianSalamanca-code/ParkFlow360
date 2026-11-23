package com.parkflow360.modulos.parqueaderos.entity;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "parqueadero_usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParqueaderoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parqueadero_id", nullable = false)
    private Long parqueaderoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "rol_asignado", nullable = false, length = 30)
    private String rolAsignado;

    @Column(name = "fecha_asignacion", updatable = false)
    private LocalDateTime fechaAsignacion;

    @PrePersist
    protected void onCreate() {
        if (fechaAsignacion == null) {
            fechaAsignacion = LocalDateTime.now();
        }
    }
}
