package es.damdi.alberto.chatdam_cliente.controller;

import es.damdi.alberto.chatdam_cliente.model.Mensaje;
import es.damdi.alberto.chatdam_cliente.model.Usuario;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality; // IMPORTANTE: Este es el que faltaba
import javafx.stage.Stage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class ChatController {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox chatContainer;
    @FXML private TextField txtNuevoMensaje;
    @FXML private Menu menuGestion;

    private Usuario usuarioLogueado;
    private org.springframework.messaging.simp.stomp.StompSession stompSession;

    private static final String HISTORIAL_URL = "http://54.242.82.149:8080/api/mensajes/historial";
    private static final String WS_URL = "ws://54.242.82.149:8080/ws-chat/websocket";
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @FXML
    public void initialize() {
        txtNuevoMensaje.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) procesarEnvio();
        });

        chatContainer.heightProperty().addListener((observable, oldValue, newValue) ->
                scrollPane.setVvalue(1.0)
        );
    }

    private HBox crearBurbuja(Mensaje msg) {
        HBox contenedor = new HBox();
        VBox burbuja = new VBox();
        burbuja.setSpacing(2);

        Label lblAutor = new Label(msg.getAutor() != null ? msg.getAutor().getUsername() : "Desconocido");
        Label lblContenido = new Label(msg.getContenido());
        lblContenido.setWrapText(true);
        Label lblHora = new Label(msg.getHora() != null ? msg.getHora().toString().substring(0, 5) : "");

        burbuja.getChildren().addAll(lblAutor, lblContenido, lblHora);
        contenedor.getChildren().add(burbuja);

        boolean esMio = esMio(msg);
        contenedor.setAlignment(esMio ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        burbuja.setStyle("-fx-padding: 10px; -fx-background-radius: 12px; " +
                (esMio ? "-fx-background-color: #2188ff;" : "-fx-background-color: #2d333b;"));

        lblContenido.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        lblAutor.setStyle("-fx-text-fill: #adbac7; -fx-font-size: 10px; -fx-font-weight: bold;");
        lblHora.setStyle("-fx-text-fill: #adbac7; -fx-font-size: 9px;");

        return contenedor;
    }

    private void cargarHistorial() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(HISTORIAL_URL)).GET().build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper om = new ObjectMapper();
                            om.registerModule(new JavaTimeModule());
                            List<Mensaje> mensajes = om.readValue(response.body(), new TypeReference<List<Mensaje>>(){});
                            mensajes.sort(java.util.Comparator.comparing(Mensaje::getFecha)
                                    .thenComparing(Mensaje::getHora));

                            Platform.runLater(() -> {
                                chatContainer.getChildren().clear();
                                for(Mensaje m : mensajes) {
                                    chatContainer.getChildren().add(crearBurbuja(m));
                                }
                                scrollPane.setVvalue(1.0);
                            });
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                });
    }

    private boolean esMio(Mensaje mensaje) {
        return usuarioLogueado != null &&
                mensaje.getAutor() != null &&
                mensaje.getAutor().getUsername().equals(usuarioLogueado.getUsername());
    }

    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        if (usuario.getRol() == null || !String.valueOf(usuario.getRol()).equals("ADMINISTRADOR")) {
            menuGestion.setVisible(false);
        }
        cargarHistorial();
        conectarWebSocket();
    }

    @FXML public void enviarMensaje(ActionEvent event) { procesarEnvio(); }

    private void procesarEnvio() {
        String texto = txtNuevoMensaje.getText();
        if (!texto.trim().isEmpty() && stompSession != null && stompSession.isConnected()) {
            Mensaje nuevoMensaje = new Mensaje(texto, usuarioLogueado);
            nuevoMensaje.setFecha(java.time.LocalDate.now());
            nuevoMensaje.setHora(java.time.LocalTime.now());
            stompSession.send("/app/enviar-mensaje", nuevoMensaje);
            txtNuevoMensaje.clear();
        }
    }

    @FXML
    public void cerrarSesion(ActionEvent event) {
        if (stompSession != null && stompSession.isConnected()) stompSession.disconnect();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/damdi/alberto/chatdam_cliente/Login.fxml"));
            Stage stage = (Stage) txtNuevoMensaje.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void salirApp(ActionEvent event) {
        if (stompSession != null && stompSession.isConnected()) stompSession.disconnect();
        Platform.exit();
        System.exit(0);
    }

    @FXML
    void abrirAltaEmpleado(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/damdi/alberto/chatdam_cliente/AltaEmpleado.fxml"));
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(loader.load()));
            modalStage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    void abrirBajaEmpleado(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/es/damdi/alberto/chatdam_cliente/BajaEmpleado.fxml"));
            Scene scene = new Scene(loader.load());

            // 1. Obtienes el controlador de la ventana que acabas de cargar
            BajaEmpleadoController controller = loader.getController();

            // 2. LLAMAS AL MÉTODO QUE ESTABA "SIN USO"
            // Esto dispara la petición al servidor y llena el ComboBox
            if (usuarioLogueado != null) {
                controller.cargarUsuarios(usuarioLogueado.getUsername());
            }

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Baja de Empleado");
            modalStage.setScene(scene);
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void conectarWebSocket() {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);

        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(om);

        stompClient.setMessageConverter(converter);

        stompClient.connectAsync(WS_URL, new org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(org.springframework.messaging.simp.stomp.StompSession session, org.springframework.messaging.simp.stomp.StompHeaders connectedHeaders) {
                stompSession = session;
                session.subscribe("/topic/mensajes", new org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter() {
                    @Override
                    public java.lang.reflect.Type getPayloadType(org.springframework.messaging.simp.stomp.StompHeaders headers) {
                        return Mensaje.class;
                    }

                    @Override
                    public void handleFrame(org.springframework.messaging.simp.stomp.StompHeaders headers, Object payload) {
                        Mensaje msg = (Mensaje) payload;
                        // AQUÍ ESTÁ LA CLAVE:
                        Platform.runLater(() -> {
                            chatContainer.getChildren().add(crearBurbuja(msg));
                            scrollPane.setVvalue(1.0); // Scroll automático
                        });
                    }
                });
            }
        });
    }


    private void mostrarMensajeEnPantalla(Mensaje mensaje) {
        Platform.runLater(() -> {
            chatContainer.getChildren().add(crearBurbuja(mensaje));
        });
    }
}