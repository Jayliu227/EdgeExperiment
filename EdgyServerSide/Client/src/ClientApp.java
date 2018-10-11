import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) {
        String hostName = "localhost";
        int port = 8000;
        try {
            Socket client = new Socket(hostName, port);

            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter writer = new PrintWriter(client.getOutputStream());

            Scanner scanner = new Scanner(System.in);

            String input;
            String msg;
            while (!(input = scanner.next()).equalsIgnoreCase("exit")) {
                writer.println(input);
                writer.flush();
                msg = reader.readLine();
                System.out.println(msg);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
