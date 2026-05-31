package es.damdi.alberto.chatdam_cliente;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Asegúrate de que el nombre del archivo FXML coincide con el que vayas a crear
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 350, 300);
        stage.setTitle("Chat Corporativo - Login");
        stage.setScene(scene);
        stage.setResizable(false); // Para que no deformen la ventana de login
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}