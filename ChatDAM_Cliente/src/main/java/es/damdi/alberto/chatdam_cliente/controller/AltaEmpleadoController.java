package es.damdi.alberto.chatdam_cliente.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AltaEmpleadoController {

    @FXML private TextField txtNuevoUsuario;
    @FXML private PasswordField txtNuevaPassword;
    @FXML private ComboBox<String> cmbRol;
    @FXML private Label lblMensaje;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    public void initialize() {
        // Rellenamos el desplegable con los roles válidos definidos en el servidor
        cmbRol.setItems(FXCollections.observableArrayList("EMPLEADO", "ADMINISTRADOR"));
        cmbRol.setValue("EMPLEADO"); // Valor por defecto
    }

    @FXML
    void registrarEmpleado(ActionEvent event) {
        String username = txtNuevoUsuario.getText();
        String password = txtNuevaPassword.getText();
        String rol = cmbRol.getValue();

        if (username.isEmpty() || password.isEmpty() || rol == null) {
            lblMensaje.setStyle("-fx-text-fill: red;");
            lblMensaje.setText("Rellena todos los campos");
            return;
        }

        // Incluimos el rol dinámicamente en el JSON
        String jsonBody = String.format("{\"username\":\"%s\", \"password\":\"%s\", \"rol\":\"%s\"}", username, password, rol);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/registro"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> {
                            lblMensaje.setStyle("-fx-text-fill: green;");
                            lblMensaje.setText("Usuario " + rol.toLowerCase() + " registrado con éxito");
                            txtNuevoUsuario.clear();
                            txtNuevaPassword.clear();
                            cmbRol.setValue("EMPLEADO"); // Reiniciamos el desplegable
                        });
                    } else {
                        Platform.runLater(() -> {
                            lblMensaje.setStyle("-fx-text-fill: red;");
                            lblMensaje.setText("Error: El usuario ya existe");
                        });
                    }
                });
    }
}