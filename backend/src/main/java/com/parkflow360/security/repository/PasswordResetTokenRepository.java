package com.parkflow360.security.repository;

import com.parkflow360.security.entity.PasswordResetToken;
import com.parkflow360.security.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    void deleteByUsuario(Usuario usuario);
}
