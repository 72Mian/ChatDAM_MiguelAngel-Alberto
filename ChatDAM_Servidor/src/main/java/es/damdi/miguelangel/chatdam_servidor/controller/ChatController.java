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

    @MessageMapping("/enviar-mensaje")
    @SendTo("/topic/mensajes")
    public Mensaje recibirYDifundirMensaje(@Payload Mensaje mensajeRecibido) {

        Optional<Usuario> autor = usuarioRepository.findById(mensajeRecibido.getAutor().getId());

        if (autor.isPresent()) {
            mensajeRecibido.setAutor(autor.get());
            mensajeRecibido.setFecha(LocalDate.now());
            mensajeRecibido.setHora(LocalTime.now());

            mensajeRepository.save(mensajeRecibido);

            return mensajeRecibido;
        }

        throw new RuntimeException("Usuario no encontrado");
    }
}