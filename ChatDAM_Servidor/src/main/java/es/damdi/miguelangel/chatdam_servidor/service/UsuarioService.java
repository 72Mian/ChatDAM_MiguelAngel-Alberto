package es.damdi.miguelangel.chatdam_servidor.service;

import es.damdi.miguelangel.chatdam_servidor.model.Usuario;
import es.damdi.miguelangel.chatdam_servidor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Método para validar el login
    public Optional<Usuario> validarLogin(String username, String passwordPlana) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Comparamos la contraseña plana que llega de JavaFX con la cifrada de la BD
            if (passwordEncoder.matches(passwordPlana, usuario.getPassword())) {
                return Optional.of(usuario);
            }
        }
        return Optional.empty(); // Login incorrecto
    }

    // Método para que el Administrador registre nuevos empleados (cifrando la clave)
    public Usuario registrarUsuario(Usuario nuevoUsuario) {
        // Ciframos la contraseña antes de guardarla
        String passwordCifrada = passwordEncoder.encode(nuevoUsuario.getPassword());
        nuevoUsuario.setPassword(passwordCifrada);

        return usuarioRepository.save(nuevoUsuario);
    }
}
