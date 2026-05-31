package es.damdi.miguelangel.chatdam_servidor.controller;


import es.damdi.miguelangel.chatdam_servidor.dto.LoginRequest;
import es.damdi.miguelangel.chatdam_servidor.model.Usuario;
import es.damdi.miguelangel.chatdam_servidor.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Optional<Usuario> usuario = usuarioService.validarLogin(request.getUsername(), request.getPassword());

        if (usuario.isPresent()) {
            return ResponseEntity.ok(usuario.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }
    }
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario nuevoUsuario) {
        try {

            // Por seguridad, si el cliente no envía ningún rol, le asignamos EMPLEADO
            if (nuevoUsuario.getRol() == null) {
                nuevoUsuario.setRol(es.damdi.miguelangel.chatdam_servidor.model.Rol.EMPLEADO);
            }

            Usuario usuarioGuardado = usuarioService.registrarUsuario(nuevoUsuario);
            return ResponseEntity.ok(usuarioGuardado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al registrar: Probablemente el usuario ya existe");
        }
    }

    @DeleteMapping("/eliminar/{username}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable String username) {
        try {
            boolean eliminado = usuarioService.eliminarUsuarioPorUsername(username);
            if (eliminado) {
                return ResponseEntity.ok("Usuario eliminado correctamente");
            } else {
                return ResponseEntity.badRequest().body("El usuario no existe");
            }
        } catch (Exception e) {
            // Si el usuario ya ha enviado mensajes, la base de datos bloqueará el borrado
            // por la clave foránea para no dejar mensajes "huérfanos".
            return ResponseEntity.badRequest().body("Error: No se puede eliminar a un usuario que ya tiene mensajes en el chat.");
        }
    }

    @GetMapping("/usuarios")
    public ResponseEntity<java.util.List<Usuario>> obtenerUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodosLosUsuarios());
    }
}