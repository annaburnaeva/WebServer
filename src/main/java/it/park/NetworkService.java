package it.park;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NetworkService {
    public static void main(String[] args) {
        try (
                ServerSocket serverSocket = new ServerSocket(9999);
        ) {
            while (true) {
                try (final Socket socket = serverSocket.accept();) {
                    final OutputStream out = socket.getOutputStream();
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final String request = reader.readLine();
                    final String[] parts = request.split(" ");
                    final String path = parts[1];

                    try {
                        final Path requestedPath = Paths.get("public", path);
                        if (!Files.exists(requestedPath) || !Files.isRegularFile(requestedPath)) {
                            throw new NoSuchFileException(path);
                        }
                        final long size = Files.size(requestedPath);

                        out.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Length: " + size + "\r\n" +
                                        "Content-Type: " + Files.probeContentType(requestedPath) + "\r\n" +
                                        "Connection: close\r\n\r\n"
                        ).getBytes());


                        Files.copy(requestedPath, out);
                    } catch (NoSuchFileException e) {
                        final Path requestedPath = Paths.get("public", "404.html");
                        final long size = Files.size(requestedPath);

                        out.write((
                                "HTTP/1.1 404 Not Found\r\n" +
                                        "Content-Length: " + size + "\r\n" +
                                        "Content-Type: " + Files.probeContentType(requestedPath) + "\r\n" +
                                        "Connection: close\r\n\r\n"
                        ).getBytes());

                        Files.copy(requestedPath, out);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// http://localhost:9876/java.png
// http://localhost:9876/index.html
// http://localhost:9876/catalog.html
