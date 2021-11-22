package com.geekbrains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("layout.fxml"));
        primaryStage.setScene(new Scene(parent));
        primaryStage.setTitle("Cloud storage");
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }
}
