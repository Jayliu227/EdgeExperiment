package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class Communicator {
    private PrintWriter writer;

    public Communicator(PrintWriter writer) {
        this.writer = writer;
    }

    public void SendMessageBack(CommandList.Command command, String msg) {
        if (this.writer == null) {
            System.out.println("Writer is not ready.");
            return;
        }

        // First send the what command this response is for.
        this.writer.println(CommandList.GetCommandCode(command));
        // Then send what this response is.
        this.writer.println(msg);
        this.writer.flush();
    }

    // this should not be called by backend, only available to edge
    public String SendAndReceiveFromBackend(CommandList.Command command, String msg) {
        // return null if there is an error
        Socket socket = new Socket();
        int timeout = 3 * 1000;

        InetSocketAddress backendAddr = new InetSocketAddress("backend-service", 8001);
        // InetSocketAddress backendAddr = new InetSocketAddress("localhost", 8001);

        try {
            socket.connect(backendAddr, timeout);

            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // send both the command and potential msg
            writer.println(CommandList.GetCommandCode(command));
            if (msg != null) {
                writer.println(msg);
            }
            writer.flush();

            int responseCommand = Integer.parseInt(reader.readLine());
            if (responseCommand != 999) {
                String response = reader.readLine();
                return response;
            }

        } catch (SocketTimeoutException e) {
            System.out.println("Timeout connecting backend");
            return "timeout";
        } catch (IOException e) {
            e.printStackTrace();
            return "refuse";
        }

        return null;
    }

    public static String EncodePath(List<Point<Integer>> path) {
        if (path == null) {
            return "-1";
        }

        String response = "";
        response += path.size() + " ";

        for (Point<Integer> step : path) {
            response += step.getX() + " " + step.getY() + " ";
        }

        return response;
    }

    public static List<Point<Integer>> DecodePath(String code) {
        List<Point<Integer>> result = new ArrayList<>();

        String[] elements = code.split(" ");
        if (elements.length < 2) {
            return null;
        }
        int length = Integer.parseInt(elements[0]);
        for (int i = 0; i < length; i++) {
            int x = Integer.parseInt(elements[2 * i + 1]);
            int y = Integer.parseInt(elements[2 * i + 2]);
            result.add(new Point<>(x, y));
        }

        return result;
    }
}
