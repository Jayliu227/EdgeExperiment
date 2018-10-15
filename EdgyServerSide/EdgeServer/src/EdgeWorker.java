import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class EdgeWorker implements Runnable {
    private final EdgeServer edgeServer;
    private Socket clientSocket;

    private BufferedReader reader;
    private PrintWriter writer;

    public EdgeWorker(Socket clientSocket, EdgeServer edgeServer) {
        this.edgeServer = edgeServer;
        this.clientSocket = clientSocket;

        edgeServer.GetConnectedClientSockets().add(clientSocket);
    }

    @Override
    public void run() {
        try {
            System.out.println("Connection from client " + clientSocket.getInetAddress());

            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new PrintWriter(clientSocket.getOutputStream());

            String line = reader.readLine();
            while (!line.equalsIgnoreCase("quit")) {
                // filter all non numeric value
                line = line.replaceAll("[^0-9]", "9");
                // if the input is more than three digits, then it is invalid.
                if (line.toCharArray().length > 3) {
                    break;
                }

                int commandCode = Integer.parseInt(line);
                Execute(commandCode);
                line = reader.readLine();
            }

            System.out.println("Disconnected from client " + clientSocket.getInetAddress());

            CleanUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Execute(int commandCode) throws IOException {
        /*
         *   Command list:
         *       Primitive Command:
         *           000 - Test
         *           100 - Echo
         *           101 - Upload Map
         *           102 - Update Map
         *           105 - Find Path
         *       Debug Command:
         *           201 - Print Edge Info
         *           202 - Task Info
         *       Special Command:
         *           301
         * */
        switch (commandCode) {
            case 0:
                SendMessageBack(CommandList.Command.TEST, "Response from edge server...");
                break;
            case 100:
                EchoHandler();
                break;
            case 101:
                UploadMapHandler();
                break;
            case 105:
                PathFindingHandler();
                break;
            default:
                SendMessageBack(CommandList.Command.NULL, "Cannot recognize command...");
        }
    }

    private void PathFindingHandler() throws IOException {
        PathFinder finder = new PathFinder();
        String coordinates = this.reader.readLine();

        // check if there is a valid map
        if (!edgeServer.GetMap().getIsValid()) {
            SendMessageBack(CommandList.Command.NULL, "There is no valid map.");
            return;
        }

        String[] elements = coordinates.split(" ");
        if (elements.length != 4) {
            // if the input if invalid, we return something to represent it;
            SendMessageBack(CommandList.Command.NULL, "Input for path finding is invalid.");
        } else {
            int sx = Integer.parseInt(elements[0]);
            int sy = Integer.parseInt(elements[1]);
            int dx = Integer.parseInt(elements[2]);
            int dy = Integer.parseInt(elements[3]);

            List<Pair<Integer, Integer>> path = finder.DFSPathFind(new Pair<>(sx, sy), new Pair<>(dx, dy), edgeServer.GetMap());

            if (path.size() == 0) {
                SendMessageBack(CommandList.Command.NULL, "Can't find such path.");
                return;
            }

            String response = "";
            response += path.size() + " ";

            for (Pair<Integer, Integer> step : path) {
                response += step.getKey() + " " + step.getValue() + " ";
            }

            System.out.println("Found path from (" + sx + "," + sy + ") - > (" + dx + "," + dy + ")");
            System.out.println("Message sent back: " + response);
            SendMessageBack(CommandList.Command.FIND_PATH, response);
        }
    }

    private void UploadMapHandler() throws IOException {
        String builderString = this.reader.readLine();

        edgeServer.GetMap().BuildMap(builderString);

        String response;
        if (edgeServer.GetMap().getIsValid()) {
            response = "Successfully uploaded map information.";
            edgeServer.GetMap().PrintMap();
        } else {
            response = "Failed to upload mao information.";
        }

        SendMessageBack(CommandList.Command.UPLOAD_MAP, response);
    }

    private void EchoHandler() throws IOException {
        String msg = null;
        msg = this.reader.readLine();

        if (msg != null) {
            SendMessageBack(CommandList.Command.ECHO, "Message from client: " + msg);
        }

    }

    private void SendMessageBack (CommandList.Command command, String msg) {
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

    private void CleanUp() {
        edgeServer.GetConnectedClientSockets().remove(edgeServer);
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
