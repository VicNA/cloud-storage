package com.geekbrains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CloudStorageClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("client.fxml"));
        primaryStage.setScene(new Scene(parent));
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }
}
