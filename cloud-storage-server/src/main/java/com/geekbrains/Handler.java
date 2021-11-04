package com.geekbrains;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
public class Handler {

    private static int counter = 0;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DataInputStream is;
    private final DataOutputStream os;
    private final String name;

    private Path serverDir;

    public Handler(ExecutorService executor, Socket socket) throws IOException {
        serverDir = Paths.get("cloud-storage-server", "storage");
        if (!Files.exists(serverDir)) Files.createDirectory(serverDir);

        is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        name = "User#" + ++counter;
        log.debug("Set nick: {} for new client", name);

        for (String fileName : getFiles(serverDir)) {
            os.writeUTF("/file " + fileName);
        }
        os.flush();

        executor.execute(this::runHandler);
    }

    private String getDate() {
        return formatter.format(LocalDateTime.now());
    }

    private List<String> getFiles(Path path) throws IOException {
        return Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    public void runHandler() {
        try {
            while (true) {
                String msg = is.readUTF();
                log.debug("Received: {}", msg);

                if (msg.startsWith("/")) {
                    if (msg.startsWith("/file ")) {
                        String[] strings = msg.split(" ", 3);
                        int size = Integer.parseInt(strings[1]);
                        String fileName = strings[2];

                        Path filePath = Paths.get(serverDir.toString(), fileName);
                        log.debug(filePath.toString());
                        if (!Files.exists(filePath)) Files.createFile(filePath);
                        File file = new File(filePath.toString());

                        byte[] bytes = new byte[size];
                        try (BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                            is.read(bytes, 0, bytes.length);
                            fos.write(bytes, 0, bytes.length);
                            fos.flush();
                        }

                        os.writeUTF("/file " + fileName);
                    }
                }


                String response = String.format("%s %s: %s", getDate(), name, msg);
                log.debug("Message for response: {}", response);
                os.writeUTF(response);
                os.flush();
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
