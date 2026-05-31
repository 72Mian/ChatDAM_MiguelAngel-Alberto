package es.damdi.alberto.chatdam_cliente.controller;

import es.damdi.alberto.chatdam_cliente.model.Mensaje;
import es.damdi.alberto.chatdam_cliente.model.Usuario;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Menu; // Importación nueva
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ChatController {

    @FXML
    private TextArea areaMensajes;
    @FXML
    private TextField txtNuevoMensaje;
    @FXML
    private Menu menuGestion; // Modificado: Inyectamos el menú completo, no solo el item
    @FXML
    private MenuItem menuAltaEmpleado;

    private Usuario usuarioActual;
    private StompSession stompSession;

    private static final String WS_URL = "ws://localhost:8080/ws-chat/websocket";
    private static final String HISTORIAL_URL = "http://localhost:8080/api/mensajes/historial";
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioActual = usuario;

        // CONTROL DE ACCESO: Si no es administrador, ocultamos la pestaña entera de Gestión
        if (usuario.getRol() == null || !usuario.getRol().equals("ADMINISTRADOR")) {
            menuGestion.setVisible(false); // Desaparece el menú por completo de la barra
        }

        areaMensajes.appendText("¡Bienvenido al chat corporativo, " + usuario.getUsername() + "!\n");
        cargarHistorial();
        conectarWebSocket();
    }

    @FXML
    void abrirAltaEmpleado(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/damdi/alberto/chatdam_cliente/AltaEmpleado.fxml"));
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Alta de Nuevo Empleado");
            modalStage.setScene(new Scene(loader.load()));
            modalStage.showAndWait();
        } catch (IOException e) {
            System.err.println("Error al cargar la ventana de Alta de Empleado.");
            e.printStackTrace();
        }
    }

    @FXML
    void abrirBajaEmpleado(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/damdi/alberto/chatdam_cliente/BajaEmpleado.fxml"));
            Scene scene = new Scene(loader.load()); // Cargamos la vista

            BajaEmpleadoController controller = loader.getController();
            controller.cargarUsuarios(usuarioActual.getUsername());

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Baja de Empleado");
            modalStage.setScene(scene);
            modalStage.showAndWait();
        } catch (IOException e) {
            System.err.println("Error al cargar la ventana de Baja de Empleado.");
            e.printStackTrace();
        }
    }

    @FXML
    void cerrarSesion(ActionEvent event) {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/damdi/alberto/chatdam_cliente/Login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) areaMensajes.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Inicio de Sesión - Chat Corporativo");
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Error crítico al volver a la pantalla de Login.");
            e.printStackTrace();
        }
    }

    private void cargarHistorial() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(HISTORIAL_URL)).GET().build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.registerModule(new JavaTimeModule());
                            mapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                            List<Mensaje> historial = mapper.readValue(response.body(), new TypeReference<List<Mensaje>>() {});
                            Collections.reverse(historial);
                            Platform.runLater(() -> {
                                areaMensajes.appendText("--- Últimos mensajes ---\n");
                                historial.forEach(this::mostrarMensajeEnPantalla);
                                areaMensajes.appendText("------------------------\n");
                            });
                        } catch (Exception e) { System.err.println("Error procesando historial: " + e.getMessage()); }
                    }
                });
    }

    private void conectarWebSocket() {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(mapper);
        stompClient.setMessageConverter(converter);

        try {
            stompSession = stompClient.connectAsync(WS_URL, new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    session.subscribe("/topic/mensajes", new StompSessionHandlerAdapter() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) { return Mensaje.class; }
                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) { mostrarMensajeEnPantalla((Mensaje) payload); }
                    });
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) { areaMensajes.appendText("Error: " + e.getMessage() + "\n"); }
    }

    private void mostrarMensajeEnPantalla(Mensaje mensaje) {
        Platform.runLater(() -> {
            String horaStr = (mensaje.getHora() != null) ? mensaje.getHora().toString().substring(0, 5) : "00:00";
            String autorStr = (mensaje.getAutor() != null) ? mensaje.getAutor().getUsername() : "Desconocido";
            areaMensajes.appendText(String.format("[%s] %s: %s\n", horaStr, autorStr, mensaje.getContenido()));
        });
    }

    @FXML
    void enviarMensaje(ActionEvent event) {
        String texto = txtNuevoMensaje.getText();
        if (!texto.isEmpty() && stompSession != null && stompSession.isConnected()) {
            Mensaje nuevoMensaje = new Mensaje(texto, usuarioActual);
            nuevoMensaje.setFecha(java.time.LocalDate.now());
            nuevoMensaje.setHora(java.time.LocalTime.now());
            stompSession.send("/app/enviar-mensaje", nuevoMensaje);
            txtNuevoMensaje.clear();
        }
    }

    @FXML
    void salirApp(ActionEvent event) {
        if (stompSession != null && stompSession.isConnected()) stompSession.disconnect();
        Platform.exit();
        System.exit(0);
    }
}