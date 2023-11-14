import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServerTCP {
    public static void main(String[] args) {
        int port = 12345;

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server mendengarkan pada port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Klien terhubung: " + clientSocket.getInetAddress().getHostAddress());

                // Tangani klien dalam thread terpisah
                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            // Create input stream to receive file
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

            while (true) {
                try {
                    // Read file name and size
                    String message = dis.readUTF();
                    if ("/q".equals(message)) {
                        // Client wants to quit, close the connection
                        System.out.println("Klien terputus: " + clientSocket.getInetAddress().getHostAddress());
                        dis.close();
                        clientSocket.close();
                        return;
                    } else {
                        // Process file
                        String fileName = message;
                        long fileSize = dis.readLong();

                        // Check if the file already exists
                        String directoryPath = "received";
                        File directory = new File(directoryPath);
                        if (!directory.exists()) {
                            directory.mkdirs();  // Create the directory if it doesn't exist
                        }

                        File serverFile = new File(directoryPath, fileName);
                        int postfix = 1;
                        while (serverFile.exists()) {
                            String[] nameParts = fileName.split("\\.");
                            String baseName = nameParts[0];
                            String extension = nameParts.length > 1 ? "." + nameParts[1] : "";
                            fileName = baseName + "_" + postfix + extension;
                            serverFile = new File(directoryPath, fileName);
                            postfix++;
                        }

                        System.out.println("Menerima file: " + fileName);

                        // Create output stream to write file
                        FileOutputStream fos = new FileOutputStream(serverFile);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);

                        // Receive and write file data
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = dis.read(buffer, 0, buffer.length)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }

                        bos.close();

                        System.out.println("File diterima dengan sukses: " + fileName);
                    }
                } catch (EOFException e) {
                    System.out.println("Klien terputus: " + clientSocket.getInetAddress().getHostAddress());
                    dis.close();
                    clientSocket.close();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
