package es.damdi.alberto.chatdam_cliente.controller;

import es.damdi.alberto.chatdam_cliente.model.Usuario;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

public class BajaEmpleadoController {

    @FXML private ComboBox<String> cmbUsuarios;
    @FXML private Label lblMensaje;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private String usuarioLogueado; // Aquí guardaremos tu nombre

    // Este método lo llama el ChatController antes de mostrar la ventana
    public void cargarUsuarios(String usernameActual) {
        this.usuarioLogueado = usernameActual;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://54.242.82.149:8080/api/auth/usuarios"))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            List<Usuario> listaUsuarios = mapper.readValue(response.body(), new TypeReference<List<Usuario>>() {});

                            // Extraemos los nombres y FILTRAMOS el nuestro
                            List<String> nombres = listaUsuarios.stream()
                                    .map(Usuario::getUsername)
                                    .filter(nombre -> !nombre.equals(this.usuarioLogueado)) // ¡AQUÍ ESTÁ LA MAGIA!
                                    .collect(Collectors.toList());

                            Platform.runLater(() -> {
                                cmbUsuarios.getItems().addAll(nombres);
                                cmbUsuarios.setPromptText("Selecciona un empleado");
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.runLater(() -> {
                                lblMensaje.setStyle("-fx-text-fill: red;");
                                lblMensaje.setText("Error procesando la lista de usuarios.");
                            });
                        }
                    } else {
                        Platform.runLater(() -> {
                            lblMensaje.setStyle("-fx-text-fill: red;");
                            lblMensaje.setText("Error del servidor al cargar usuarios.");
                        });
                    }
                });
    }

    @FXML
    void eliminarSeleccionado(ActionEvent event) {
        String username = cmbUsuarios.getValue();

        if (username == null || username.isEmpty()) {
            lblMensaje.setStyle("-fx-text-fill: red;");
            lblMensaje.setText("Selecciona un usuario de la lista.");
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://54.242.82.149:8080/api/auth/eliminar/" + username))
                .DELETE()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            lblMensaje.setStyle("-fx-text-fill: green;");
                            lblMensaje.setText("Usuario eliminado con éxito.");
                            cmbUsuarios.getItems().remove(username); // Lo quitamos de la lista
                            cmbUsuarios.setValue(null);
                        } else {
                            lblMensaje.setStyle("-fx-text-fill: red;");
                            lblMensaje.setText(response.body());
                        }
                    });
                });
    }
}