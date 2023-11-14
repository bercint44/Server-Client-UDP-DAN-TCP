import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ChatServerUDP {
    private static final int PORT = 9876;
    private DatagramSocket serverSocket;
    private DatagramPacket receivePacket;
    private List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        new ChatServerUDP().runServer();
    }

    public void runServer() {
        try {
            serverSocket = new DatagramSocket(PORT);
            System.out.println("Server berjalan di port " + PORT);

            while (true) {
                byte[] receiveData = new byte[1024];
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Pesan dari " + receivePacket.getAddress().getHostAddress() + ": " + message);

                // Menangani pesan dari klien
                handleMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(String message) {
        // Mendapatkan informasi pengirim dari pesan
        String senderAddress = receivePacket.getAddress().getHostAddress();
        int senderPort = receivePacket.getPort();

        // Mengirim pesan ke semua klien yang terhubung, kecuali pengirimnya
        for (ClientHandler client : clients) {
            if (!client.getAddress().equals(senderAddress) || client.getPort() != senderPort) {
                client.sendMessage(client.getUsername() + ": " + message);
            }
        }

        // Menambahkan klien baru jika belum terdaftar
        if (!isClientRegistered(senderAddress, senderPort)) {
            clients.add(new ClientHandler(senderAddress, senderPort, message));
        }
    }

    private boolean isClientRegistered(String clientAddress, int clientPort) {
        for (ClientHandler client : clients) {
            if (client.getAddress().equals(clientAddress) && client.getPort() == clientPort) {
                return true;
            }
        }
        return false;
    }

    private class ClientHandler {
        private String clientAddress;
        private int clientPort;
        private String username;

        public ClientHandler(String clientAddress, int clientPort, String initialMessage) {
            this.clientAddress = clientAddress;
            this.clientPort = clientPort;

            // Menggunakan bagian pertama dari pesan awal sebagai username
            this.username = initialMessage.split(":")[0].trim();
        }

        public void sendMessage(String message) {
            try {
                byte[] sendData = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName(clientAddress), clientPort);
                serverSocket.send(sendPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String getAddress() {
            return clientAddress;
        }

        public int getPort() {
            return clientPort;
        }

        public String getUsername() {
            return username;
        }
    }
}
