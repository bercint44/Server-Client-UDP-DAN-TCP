import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class FileClientTCP {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 12345;

        try {
            Socket socket = new Socket(serverAddress, serverPort);

            // Buat output stream untuk mengirim file
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            while (true) {
                // Dapatkan path file dari pengguna
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Masukkan path file (atau ketik '/q' untuk keluar): ");
                String filePath = br.readLine();

                // Kirim path file ke server
                dos.writeUTF(filePath);
                dos.flush();

                if ("/q".equals(filePath)) {
                    // Klien ingin keluar, tutup koneksi
                    dos.close();
                    socket.close();
                    break;
                }

                // Dapatkan nama file dari path
                String fileName = new File(filePath).getName();

                // Kirim nama dan ukuran file ke server
                dos.writeUTF(fileName);
                dos.writeLong(new File(filePath).length());
                dos.flush();

                // Buat input stream untuk membaca file
                FileInputStream fis = new FileInputStream(filePath);
                BufferedInputStream bis = new BufferedInputStream(fis);

                // Kirim data file
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = bis.read(buffer, 0, buffer.length)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }

                dos.flush();

                bis.close();

                System.out.println("File berhasil dikirim: " + fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
