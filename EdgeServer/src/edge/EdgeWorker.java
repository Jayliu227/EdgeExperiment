package edge;

import utils.CommandList;
import utils.Communicator;
import utils.PathFinder;
import utils.Point;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

public class EdgeWorker implements Runnable {
    private final EdgeServer edgeServer;
    private Socket clientSocket;

    private BufferedReader reader;
    private PrintWriter writer;

    private Communicator communicator;

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

            this.communicator = new Communicator(writer);

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
         *           103 - Check Map
         *           105 - Find Path
         *       Debug Command:
         *           201 - Print Edge Info
         *           202 - Task Info
         *       Special Command:
         *           301
         * */
        switch (commandCode) {
            case 0:
                communicator.SendMessageBack(CommandList.Command.TEST, "Response from edge server...");
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
                communicator.SendMessageBack(CommandList.Command.NULL, "Cannot recognize command...");
        }
    }

    private void PathFindingHandler() throws IOException {
        PathFinder finder = new PathFinder();
        String coordinates = this.reader.readLine();

        // check if there is a valid map
        edgeServer.GetLock().readLock().lock();
        if (!edgeServer.GetMap().getIsValid()) {
            communicator.SendMessageBack(CommandList.Command.NULL, "There is no valid map.");
            edgeServer.GetLock().readLock().unlock();
            return;
        }
        edgeServer.GetLock().readLock().unlock();

        // if it is a valid map, we need to sync with the backend
        String reply = communicator.SendAndReceiveFromBackend(CommandList.Command.CHECK_MAP, edgeServer.GetMap().getBuildString());
        // if the reply is not null, then it is not yet sync, we need to halt the execution
        if (reply != null && reply.equals("timeout")) {
            communicator.SendMessageBack(CommandList.Command.NULL, "Timeout uploading map to server");
            return;
        }

        if (reply != null && reply.equals("refuse")) {
            communicator.SendMessageBack(CommandList.Command.NULL, "Refuse connection from backend");
            return;
        }

        if (reply != null) {
            edgeServer.GetLock().writeLock().lock();

            edgeServer.GetMap().BuildMap(reply);

            edgeServer.GetLock().writeLock().unlock();
        }

        String[] elements = coordinates.split(" ");
        if (elements.length != 4) {
            // if the input if invalid, we return something to represent it;
            communicator.SendMessageBack(CommandList.Command.NULL, "Input for path finding is invalid.");
        } else {
            int sx = Integer.parseInt(elements[0]);
            int sy = Integer.parseInt(elements[1]);
            int dx = Integer.parseInt(elements[2]);
            int dy = Integer.parseInt(elements[3]);

            List<Point<Integer>> path = finder.DFSPathFind(new Point<>(sx, sy), new Point<>(dx, dy), edgeServer.GetMap());

            if (path.size() == 0) {
                communicator.SendMessageBack(CommandList.Command.NULL, "Can't find such path.");
                return;
            }

            // we remove the first one from the list
            path.remove(0);

            String response = Communicator.EncodePath(path);

            // TEST
            String fromBackend = communicator.SendAndReceiveFromBackend(CommandList.Command.COORDINATE_PATH, response);
            if (!response.equals(fromBackend)) {
                System.out.println("Error here: self->" + response + " theirs->" + fromBackend);
            }

            System.out.println("Found path from (" + sx + "," + sy + ") - > (" + dx + "," + dy + ")");
            System.out.println("Message sent back: " + response);
            communicator.SendMessageBack(CommandList.Command.FIND_PATH, response);
        }
    }

    private void UploadMapHandler() throws IOException {
        String builderString = this.reader.readLine();

        edgeServer.GetLock().readLock().lock();
        edgeServer.GetMap().BuildMap(builderString);

        String response;
        if (edgeServer.GetMap().getIsValid()) {
            response = "Successfully uploaded map information.";
            // edgeServer.GetMap().PrintMap();
        } else {
            response = "Failed to upload mao information.";
        }
        edgeServer.GetLock().readLock().unlock();

        // after uploading the map, we want to also upload the map to the backend
        String receivedMessage = communicator.SendAndReceiveFromBackend(CommandList.Command.UPLOAD_MAP, builderString);
        if (receivedMessage != null) {
            communicator.SendMessageBack(CommandList.Command.UPLOAD_MAP, response);
        } else {
            communicator.SendMessageBack(CommandList.Command.NULL, "Unable to upload map to backend...");
        }
    }

    private void EchoHandler() throws IOException {
        String msg = null;
        msg = this.reader.readLine();

        if (msg != null) {
            communicator.SendMessageBack(CommandList.Command.ECHO, "Message from client: " + msg);
        }

    }

    private void CleanUp() {
        edgeServer.GetConnectedClientSockets().remove(clientSocket);
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean TestBackend() {
        Socket socket = new Socket();
        int timeout = 3 * 1000;
        InetSocketAddress backendAddr = new InetSocketAddress("localhost", 8001);
        boolean successful = false;
        try {
            socket.connect(backendAddr, timeout);

            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.println("0");
            writer.flush();
            String commandCode = reader.readLine();
            String message = reader.readLine();
            System.out.println(message);

            successful = true;
        } catch (SocketTimeoutException e) {
            System.out.println("Timeout connecting backend");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return successful;
    }
}
