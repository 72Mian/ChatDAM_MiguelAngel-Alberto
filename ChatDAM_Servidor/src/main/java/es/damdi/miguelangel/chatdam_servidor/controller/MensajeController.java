package es.damdi.miguelangel.chatdam_servidor.controller;


import es.damdi.miguelangel.chatdam_servidor.model.Mensaje;
import es.damdi.miguelangel.chatdam_servidor.model.Usuario;
import es.damdi.miguelangel.chatdam_servidor.repository.MensajeRepository;
import es.damdi.miguelangel.chatdam_servidor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método GET para recuperar los últimos 10 mensajes (Requisito obligatorio)
    @GetMapping("/historial")
    public ResponseEntity<List<Mensaje>> obtenerUltimosMensajes() {
        List<Mensaje> ultimosMensajes = mensajeRepository.findTop10ByOrderByFechaDescHoraDesc();
        return ResponseEntity.ok(ultimosMensajes);
    }

    // Método POST para que un empleado envíe un mensaje nuevo
    @PostMapping("/enviar")
    public ResponseEntity<?> enviarMensaje(@RequestParam Long idUsuario, @RequestBody String contenido) {

        Optional<Usuario> autorOpt = usuarioRepository.findById(idUsuario);

        if (autorOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El usuario no existe");
        }

        // Construimos el mensaje basado en el modelo relacional (AD03)
        Mensaje nuevoMensaje = new Mensaje();
        nuevoMensaje.setContenido(contenido);
        nuevoMensaje.setFecha(LocalDate.now());
        nuevoMensaje.setHora(LocalTime.now());
        nuevoMensaje.setAutor(autorOpt.get());

        // Lo guardamos en la base de datos
        mensajeRepository.save(nuevoMensaje);

        return ResponseEntity.ok(nuevoMensaje);
    }
}