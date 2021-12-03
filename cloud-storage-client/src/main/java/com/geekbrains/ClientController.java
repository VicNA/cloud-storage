package com.geekbrains;

import com.geekgrains.common.ListDirectories;
import com.geekgrains.common.ListFiles;
import com.geekgrains.common.Message;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
    private String clientRootDir;
    private String serverSeparator;
    private String currentClientDir;
    private TreeItem<String> current;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Указание процентного соотношение допустимых минимальных размеров ширины при движении разделителя
        treeViewBox.minWidthProperty().bind(splitPane.widthProperty().multiply(0.15));
        listViewBox.minWidthProperty().bind(splitPane.widthProperty().multiply(0.6));

        clientTreeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<String>> changed, TreeItem<String> oldValue, TreeItem<String> newValue) {
                if (newValue != null) {
                    current = newValue;
                    if (current.getParent() != null) {
                        StringBuilder sb = new StringBuilder(current.getValue());
                        TreeItem<String> parent = current.getParent();
                        while (parent.getParent() != null) {
                            sb.insert(0, serverSeparator);
                            sb.insert(0, parent.getValue());
                            parent = parent.getParent();
                        }

                        currentClientDir = sb.insert(0, serverSeparator).insert(0, clientRootDir).toString();
                    } else {
                        currentClientDir = clientRootDir;
                    }
                    netty.sendMessage(new ListFiles(currentClientDir));
                }
            }
        });
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
                    createTreeDirectories((ListDirectories) msg.getMessage()); // Как правильно вытащить нужный экземпляр без каста?
                    break;
                case LIST_FILES:
                    addListFiles((ListFiles) msg);
                    break;
            }
        });
    }

    private void createTreeDirectories(ListDirectories msg) {
        clientRootDir = msg.getClientRootDir();
        serverSeparator = msg.getSeparator();

        clientTreeView.setRoot(null);

        current = new TreeItem<>("MyCloud");
        clientTreeView.setRoot(current);

        for (String path : msg.getList()) {
            TreeItem<String> node = current;
            StringBuilder sb = new StringBuilder(path);
            sb.delete(0, clientRootDir.length()).delete(0, 1);
            String regexp = serverSeparator.equals("\\") ? "\\\\" : "/";
            for (String s : sb.toString().split(regexp)) {
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

    private void addListFiles(ListFiles msg) {
        clientListView.getItems().clear();
        clientListView.getItems().addAll(msg.getList());
    }

    public void createDirectory(ActionEvent actionEvent) {

    }

    public void reload() {
        netty.sendMessage(new ListDirectories());
        netty.sendMessage(new ListFiles(currentClientDir));
    }
}
