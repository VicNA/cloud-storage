package com.geekbrains;

import com.geekgrains.common.ListDirectory;
import com.geekgrains.common.ListFile;
import com.geekgrains.common.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class ClientController implements Initializable {

    public TreeView<String> clientTreeView;
    public ListView<String> clientListView;
    public MenuItem connect;
    public HBox treeViewBox;
    public HBox listViewBox;
    public SplitPane splitPane;

    private ClientNetty netty;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Указание процентного соотношение допустимых минимальных размеров ширины при движении разделителя
        treeViewBox.minWidthProperty().bind(splitPane.widthProperty().multiply(0.15));
        listViewBox.minWidthProperty().bind(splitPane.widthProperty().multiply(0.6));
    }

    public void connect(ActionEvent actionEvent) {
        netty = new ClientNetty(this::action);
        netty.connect("localhost", 8189);
    }

    public void disconnect() {
        if (netty != null) netty.close();
    }

    public void exit() {
        disconnect();
        Platform.exit();
    }

    private void action(Message msg) throws IOException { // Как побороть сообщение: Raw use of parameterized class 'Message'?
        Platform.runLater(() -> {
            switch (msg.getCommand()) {
                case FILE_MESSAGE:
                    break;
                case FILE_REQUEST:
                    break;
                case LIST_DIRECTORY:
                    addListDirectories((ListDirectory) msg.getMessage()); // Как правильно вытащить нужный экземпляр без каста?
                    break;
                case LIST_FILE:
                    addListFiles((ListFile) msg);
                    break;
            }
        });
    }

    private void addListDirectories(ListDirectory msg) {
        if (clientTreeView.getRoot() == null) {
            clientTreeView.setRoot(new TreeItem<>("MyCloud"));
        }

        for (String item : msg.getList()) {
            clientTreeView.getRoot().getChildren().add(new TreeItem<>(item));
        }
    }

    private void addListFiles(ListFile msg){
        clientListView.getItems().clear();
        clientListView.getItems().addAll(msg.getList());
    }

    public void createDirectory(ActionEvent actionEvent) {

    }
}
