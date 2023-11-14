import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ChatClientUDP {
    private static final int SERVER_PORT = 9876;

    public static void main(String[] args) {
        new ChatClientUDP().runClient();
    }

    public void runClient() {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName("localhost");

            // Input dari pengguna
            Scanner scanner = new Scanner(System.in);
            System.out.print("Masukkan nama pengguna Anda: ");
            String username = scanner.nextLine();

            // Thread untuk menerima pesan dari server
            Thread receiveThread = new Thread(() -> {
                try {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    while (true) {
                        clientSocket.receive(receivePacket);
                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        System.out.println(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            receiveThread.start();

            // Mengirim pesan ke server
            while (true) {
                System.out.print("[" + username + "] Masukkan pesan: ");
                String message = scanner.nextLine();

                byte[] sendData = (username + ": " + message).getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        serverAddress, SERVER_PORT);
                clientSocket.send(sendPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
