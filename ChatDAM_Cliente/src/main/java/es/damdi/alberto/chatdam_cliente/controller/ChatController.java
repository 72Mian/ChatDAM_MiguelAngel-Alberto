package es.damdi.alberto.chatdam_cliente.controller;

import es.damdi.alberto.chatdam_cliente.model.Mensaje;
import es.damdi.alberto.chatdam_cliente.model.Usuario;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;

public class ChatController {

    @FXML
    private TextArea areaMensajes;
    @FXML
    private TextField txtNuevoMensaje;

    private Usuario usuarioActual;
    private StompSession stompSession;

    // URL de tu WebSocket configurada en el servidor
    private static final String WS_URL = "ws://localhost:8080/ws-chat";

    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioActual = usuario;
        areaMensajes.appendText("¡Bienvenido al chat corporativo, " + usuario.getUsername() + "!\n");
        conectarWebSocket();
    }

    private void conectarWebSocket() {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);

        // Configuramos Jackson para que entienda las fechas (LocalDate y LocalTime)
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(mapper);
        stompClient.setMessageConverter(converter);

        try {
            areaMensajes.appendText("Conectando con el servidor...\n");

            // Iniciamos la conexión
            stompSession = stompClient.connectAsync(WS_URL, new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    Platform.runLater(() -> areaMensajes.appendText("Conectado exitosamente.\n\n"));

                    // Nos suscribimos al canal público de mensajes
                    session.subscribe("/topic/mensajes", new StompSessionHandlerAdapter() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                            return Mensaje.class; // Esperamos recibir objetos Mensaje
                        }

                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                            Mensaje mensajeRecibido = (Mensaje) payload;
                            mostrarMensajeEnPantalla(mensajeRecibido);
                        }
                    });
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    Platform.runLater(() -> areaMensajes.appendText("[Error de conexión con el servidor]\n"));
                }
            }).get(); // El .get() bloquea hasta que se conecte

        } catch (InterruptedException | ExecutionException e) {
            areaMensajes.appendText("Error crítico al intentar conectar: " + e.getMessage() + "\n");
        }
    }

    private void mostrarMensajeEnPantalla(Mensaje mensaje) {
        // Platform.runLater es OBLIGATORIO cuando un hilo de fondo (WebSocket) actualiza la interfaz (JavaFX)
        Platform.runLater(() -> {
            String formato = String.format("[%s] %s: %s\n",
                    mensaje.getHora().toString().substring(0, 5), // Muestra solo HH:mm
                    mensaje.getAutor().getUsername(),
                    mensaje.getContenido());
            areaMensajes.appendText(formato);
        });
    }

    @FXML
    void enviarMensaje(ActionEvent event) {
        String texto = txtNuevoMensaje.getText();
        if (!texto.isEmpty() && stompSession != null && stompSession.isConnected()) {

            // Preparamos el mensaje a enviar
            Mensaje nuevoMensaje = new Mensaje(texto, usuarioActual);

            // Lo enviamos a la ruta mapeada en tu servidor
            stompSession.send("/app/enviar-mensaje", nuevoMensaje);

            txtNuevoMensaje.clear();
        }
    }

    @FXML
    void salirApp(ActionEvent event) {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
        Platform.exit();
        System.exit(0);
    }
}