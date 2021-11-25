package com.geekbrains;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
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

    public ListView<String> clientView;
    public ListView<String> serverView;
    public TextField input;
    public MenuItem connect;

    private DataInputStream is;
    private DataOutputStream os;

    private BufferedInputStream bis;
    private BufferedOutputStream bos;

    private ClientNetty netty;

    private Path clientdDir;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        try {
////            clientdDir = Paths.get("cloud-storage-client", "storage");
////            if (!Files.exists(clientdDir)) Files.createDirectory(clientdDir);
////            addListFiles();
////
////            Socket socket = new Socket("localhost", 8189);
////            is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
////            os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
////
////            Thread readThread = new Thread(this::read);
////            readThread.setDaemon(true);
////            readThread.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void addListFiles() throws IOException {
        clientView.getItems().clear();
        clientView.getItems().addAll(getFiles(clientdDir));
        clientView.getSelectionModel().selectFirst();
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

                if (msg.startsWith("/")) {
                    if (msg.startsWith("/file ")) {
                        String fileName = msg.split(" ", 3)[1];
                        Platform.runLater(() -> {
                            if (!serverView.getItems().contains(fileName)) {
                                serverView.getItems().add(fileName);
                            }
                        });
                    }
                }
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

    public void addFile(ActionEvent actionEvent) {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        Path filePath = Paths.get(clientdDir.toString(), fileName);
        File file = new File(filePath.toString());

        try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file))) {
            log.debug("/file " + file.length() + " " + fileName);
            os.writeUTF("/file " + file.length() + " " + fileName);

            byte[] bytes = new byte[(int) file.length()];
            fis.read(bytes, 0, bytes.length);
            os.write(bytes, 0, bytes.length);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(ActionEvent actionEvent) {
    }

    public void connect(ActionEvent actionEvent) {
        netty = new ClientNetty("localhost", 8189);
        connect.setDisable(true);
    }

    public void disconnect() {
        netty.close();
    }

    public void exit() {
        disconnect();
        Platform.exit();
    }
}
