package es.damdi.miguelangel.chatdam_servidor.controller;


import es.damdi.miguelangel.chatdam_servidor.model.Mensaje;
import es.damdi.miguelangel.chatdam_servidor.model.Usuario;
import es.damdi.miguelangel.chatdam_servidor.repository.MensajeRepository;
import es.damdi.miguelangel.chatdam_servidor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Controller
public class ChatController {

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Cuando un empleado envíe algo a "/app/enviar-mensaje", Spring Boot ejecutará este método
    @MessageMapping("/enviar-mensaje")
    @SendTo("/topic/mensajes") // Y lo que devuelva este método, se enviará a TODOS los suscritos a este canal
    public Mensaje recibirYDifundirMensaje(@Payload Mensaje mensajeRecibido) {

        // 1. Buscamos al autor original en AWS para asegurarnos de que existe
        Optional<Usuario> autor = usuarioRepository.findById(mensajeRecibido.getAutor().getId());

        if (autor.isPresent()) {
            mensajeRecibido.setAutor(autor.get());
            mensajeRecibido.setFecha(LocalDate.now());
            mensajeRecibido.setHora(LocalTime.now());

            // 2. Guardamos el mensaje en la base de datos de AWS
            mensajeRepository.save(mensajeRecibido);

            // 3. Se lo "escupimos" a todos los usuarios conectados al instante
            return mensajeRecibido;
        }

        throw new RuntimeException("Usuario no encontrado");
    }
}