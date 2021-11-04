package com.geekbrains;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class MainController implements Initializable {

    public ListView<String> clientView;
    public ListView<String> serverView;
    public TextField input;

    private DataInputStream is;
    private DataOutputStream os;

    private Path clientdDir;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            clientdDir = Paths.get("cloud-storage-client", "storage");
            if (!Files.exists(clientdDir)) Files.createDirectory(clientdDir);
            addListFiles();

            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());

            Thread readThread = new Thread(this::read);
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addListFiles() throws IOException {
        clientView.getItems().clear();
        clientView.getItems().addAll(getFiles(clientdDir));
    }

    private List<String> getFiles(Path path) throws IOException {
        return Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    private void read() {
        try {
            while (true) {
                String msg = is.readUTF();
                log.debug("Received: {}", msg);
                Platform.runLater(() -> clientView.getItems().add(msg));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(ActionEvent actionEvent) throws IOException {
        String text = input.getText();
        os.writeUTF(text);
        os.flush();
        input.clear();
    }

    public void clientSelectedItem(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            String item = clientView.getSelectionModel().getSelectedItem();
            input.setText(item);
        }
    }
}
