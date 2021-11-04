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
//        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
//        os = new DataOutputStream(socket.getOutputStream());
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
                        String fileName = msg.split(" ", 2)[1];
                        Path filePath = Paths.get(serverDir.toString(), fileName);
                        log.debug(filePath.toString());
                        if (!Files.exists(filePath)) Files.createFile(filePath);
                        File file = new File(filePath.toString());

                        byte[] bytes = new byte[1024];
                        int in;
                        int pos = 0;
                        try (BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
//                            count = is.read(bytes);
//                            fos.write(bytes, 0, count);
//                            do {
//                                pos += count;
//                                count = is.read(bytes);
//                                fos.write(bytes, pos - 1, count);
//                            } while (count != -1);
                            while ((in = is.read(bytes)) != -1) {
                                log.debug(String.valueOf(in));
                                fos.write(bytes, 0, in);
//                                fos.flush();
                            }
                            fos.flush();
                        }
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
