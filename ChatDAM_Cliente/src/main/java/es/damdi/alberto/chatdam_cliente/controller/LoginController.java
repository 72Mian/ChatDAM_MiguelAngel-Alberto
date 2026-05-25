package es.damdi.alberto.chatdam_cliente.controller;

import es.damdi.alberto.chatdam_cliente.model.LoginRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    private static final String SERVER_URL = "http://localhost:8080/api/auth/login";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final Gson gson = new Gson();

    @FXML
    void iniciarSesion(ActionEvent event) {
        String username = txtUsuario.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Error", "Por favor, rellena todos los campos.");
            return;
        }

        // Convertimos los datos a JSON usando la clase LoginRequest
        LoginRequest requestBody = new LoginRequest(username, password);
        String jsonBody = gson.toJson(requestBody);

        // Preparamos la petición al servidor Spring Boot
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Enviamos la petición de forma asíncrona para no congelar la interfaz
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> Platform.runLater(() -> procesarRespuesta(response)))
                .exceptionally(e -> {
                    Platform.runLater(() -> mostrarAlerta("Error de conexión", "No se pudo conectar con el servidor."));
                    return null;
                });
    }

    private void procesarRespuesta(HttpResponse<String> response) {
        if (response.statusCode() == 200) {
            System.out.println("Login exitoso. Respuesta del servidor: " + response.body());
            // TODO: Aquí añadiremos el código para cargar y mostrar la ventana del Chat Principal
        } else {
            mostrarAlerta("Acceso Denegado", "Credenciales incorrectas. Vuelve a intentarlo.");
        }
    }

    @FXML
    void salirApp(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}