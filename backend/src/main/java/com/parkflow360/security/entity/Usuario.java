package com.parkflow360.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** Puede ser nulo para cuentas creadas con Google. */
    @Column(nullable = true)
    private String password;

    @Column(nullable = false, length = 50)
    private String rol;

    @Column(length = 100)
    private String nombre;

    @Column(length = 20)
    private String proveedor;

    @Column(name = "google_id", length = 100)
    private String googleId;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (proveedor == null) {
            proveedor = "LOCAL";
        }
    }
}
