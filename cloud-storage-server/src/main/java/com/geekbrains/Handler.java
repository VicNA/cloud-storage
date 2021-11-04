package com.geekbrains;

import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
public class Handler implements Runnable {

    private static int counter = 0;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DataInputStream is;
    private final DataOutputStream os;
    private final String name;
    private boolean isRunning;

    public Handler (Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        name = "User#" + ++counter;
        log.debug("Set nick: {} for new client", name);
        isRunning = true;
    }

    private String getDate() {
        return formatter.format(LocalDateTime.now());
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                String msg = is.readUTF();
                log.debug("received: {}", msg);
                String response = String.format("%s %s: %s", getDate(), name, msg);
                log.debug("Message for response: {}", response);
                os.writeUTF(response);
                os.flush();
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
