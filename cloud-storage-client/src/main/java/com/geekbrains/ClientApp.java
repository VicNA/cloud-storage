package com.geekbrains;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client.fxml"));
        Parent parent = loader.load();
        primaryStage.setScene(new Scene(parent));
        primaryStage.setTitle("Cloud storage");

        primaryStage.setOnCloseRequest(e -> {
            ClientController controller = loader.getController();
            controller.disconnect();
            Platform.exit();
        });

        primaryStage.show();
    }
}
