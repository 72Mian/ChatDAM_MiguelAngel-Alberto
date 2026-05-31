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

    public Optional<Usuario> validarLogin(String username, String passwordPlana) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (passwordEncoder.matches(passwordPlana, usuario.getPassword())) {
                return Optional.of(usuario);
            }
        }
        return Optional.empty();
    }

    public Usuario registrarUsuario(Usuario nuevoUsuario) {

        String passwordCifrada = passwordEncoder.encode(nuevoUsuario.getPassword());
        nuevoUsuario.setPassword(passwordCifrada);

        return usuarioRepository.save(nuevoUsuario);
    }

    public boolean eliminarUsuarioPorUsername(String username) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isPresent()) {
            usuarioRepository.delete(usuarioOpt.get());
            return true;
        }
        return false;
    }

    public java.util.List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }
}
