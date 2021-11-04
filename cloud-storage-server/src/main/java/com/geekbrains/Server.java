package com.geekbrains;

import lombok.extern.slf4j.Slf4j;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Server {

    private static final int PORT = 8189;

    private ExecutorService executor;

    public Server() {
        try (ServerSocket server = new ServerSocket(PORT)) {
            log.debug("Server started...");

            executor = Executors.newCachedThreadPool();

            while (true) {
                Socket socket = server.accept();
                log.debug("Client accepted...");
                new Handler(executor, socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }

}
