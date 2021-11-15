package com.geekbrains;

public class ServerApp {

    static final int PORT = 8189;

    public static void main(String[] args) {
        int port = PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        new StorageServer(port);
    }
}
