package es.damdi.alberto.chatdam_cliente;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        // Asegúrate de que el nombre del archivo FXML coincide con el que vayas a crear
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 350, 300);
        stage.setTitle("Chat Corporativo - Login");
        stage.setScene(scene);
        stage.setResizable(false);
        try {
            // Forma absoluta y directa, mucho más segura en JavaFX
            Image icon = new Image(getClass().getResourceAsStream("/es/damdi/alberto/chatdam_cliente/twenti.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono. Revisa que el nombre y la ruta sean exactos.");
            e.printStackTrace();
        }
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}