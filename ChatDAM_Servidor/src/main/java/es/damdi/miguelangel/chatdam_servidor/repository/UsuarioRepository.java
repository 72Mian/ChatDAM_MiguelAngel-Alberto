package es.damdi.miguelangel.chatdam_servidor.repository;

import es.damdi.miguelangel.chatdam_servidor.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Spring Boot escribirá la consulta SQL automáticamente para buscar por nombre
    Optional<Usuario> findByUsername(String username);
}
