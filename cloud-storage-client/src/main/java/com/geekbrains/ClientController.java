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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

@Slf4j
public class ClientController implements Initializable {

    public TreeView<String> clientTreeView;
    public ListView<String> clientListView;
    public MenuItem connect;
    public HBox treeViewBox;
    public HBox listViewBox;
    public SplitPane splitPane;

    private ClientNetty netty;
    private Path currentDir;
    private TreeItem<String> current;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Указание процентного соотношение допустимых минимальных размеров ширины при движении разделителя
        treeViewBox.minWidthProperty().bind(splitPane.widthProperty().multiply(0.15));
        listViewBox.minWidthProperty().bind(splitPane.widthProperty().multiply(0.6));
    }

    public void connect(ActionEvent actionEvent) {
        disconnect();

        netty = new ClientNetty(this::action).connect("localhost", 8189);
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
                case LIST_DIRECTORIES:
                    createTreeDirectories((ListDirectory) msg.getMessage()); // Как правильно вытащить нужный экземпляр без каста?
                    break;
                case LIST_FILES:
                    addListFiles((ListFile) msg);
                    break;
            }
        });
    }

    private void createTreeDirectories(ListDirectory msg) {
        currentDir = Paths.get(msg.getCurrentDir());

        clientTreeView.setRoot(null);

        current = new TreeItem<>("MyCloud");
        clientTreeView.setRoot(current);

        for (String path : msg.getList()) {
            TreeItem<String> node = current;
            for (String s : path.replace(currentDir.toString() + "\\", "").split("\\\\")) {
                node = addTreeNode(node, s);
            }
        }

        current.setExpanded(true);
    }

    private TreeItem<String> addTreeNode(TreeItem<String> parent, String value) {
        for (TreeItem<String> child : parent.getChildren()) {
            if (value.equals(child.getValue())) {
                return child;
            }
        }
        TreeItem<String> newChild = new TreeItem<>(value);
        parent.getChildren().add(newChild);
        return newChild;
    }

    private void addListFiles(ListFile msg) {
        clientListView.getItems().clear();
        clientListView.getItems().addAll(msg.getList());
    }

    public void createDirectory(ActionEvent actionEvent) {

    }

    public void openDirectory(MouseEvent mouseEvent) {
        Platform.runLater(() -> {
            if (clientTreeView.getSelectionModel().getSelectedItem() != null) {
                current = clientTreeView.getSelectionModel().getSelectedItem();
//                current.getValue()

            }
//        currentDir.resolve(current.)
//        netty.sendMessage(new ListFile(currentDir.toString()));
        });
    }
}
