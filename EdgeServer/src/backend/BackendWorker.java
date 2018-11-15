package backend;

import utils.CommandList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BackendWorker implements Runnable {
    private final Socket clientSocket;
    private final BackendServer backendServer;

    private BufferedReader reader;
    private PrintWriter writer;

    public BackendWorker(Socket clientSocket, BackendServer backendServer) {
        this.clientSocket = clientSocket;
        this.backendServer = backendServer;

        backendServer.GetConnectedClientSockets().add(clientSocket);
    }

    @Override
    // NOTE: backend server won't keep the socket forever, it only processes one request at a time
    public void run() {
        try {
            System.out.println("Connection from client " + clientSocket.getInetAddress());

            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new PrintWriter(clientSocket.getOutputStream());

            String line = reader.readLine();
            // filter all non numeric value
            line = line.replaceAll("[^0-9]", "9");
            // if the input is more than three digits, then it is invalid.
            if (line.toCharArray().length <= 3) {
                int commandCode = Integer.parseInt(line);
                Execute(commandCode);
            } else {
                SendMessageBack(CommandList.Command.NULL, "Command Input incorrect...");
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
         *           103 - Check map
         *           105 - Find Path
         *       Debug Command:
         *           201 - Print Edge Info
         *           202 - Task Info
         *       Special Command:
         *           301
         * */
        switch (commandCode) {
            case 111:
                SendMessageBack(CommandList.Command.TEST, "Message from backend server...");
                break;
            case 101:
                UploadMapHandler();
                break;
            case 102:
                UpdateMapHandler();
            case 103:
                CheckMapHandler();
            default:
                SendMessageBack(CommandList.Command.NULL, "Cannot recognize command...");
        }
    }

    private void UploadMapHandler() throws IOException {
        String builderString = this.reader.readLine();

        backendServer.GetLock().writeLock().lock();
        backendServer.GetMap().BuildMap(builderString);
        backendServer.GetLock().writeLock().unlock();

        String response;
        if (backendServer.GetMap().getIsValid()) {
            response = "Successfully uploaded map information.";
            backendServer.GetMap().PrintMap();
            SendMessageBack(CommandList.Command.UPLOAD_MAP, response);
        } else {
            response = "Failed to upload mao information.";
            SendMessageBack(CommandList.Command.NULL, "Unable to successfully upload map...");
        }
    }

    private void UpdateMapHandler() {

    }

    private void CheckMapHandler() throws IOException {
        // reply with the string builder if it is not updated, otherwise just reply null
        String checkAgainstString = this.reader.readLine();

        backendServer.GetLock().readLock().lock();

        String currentBuildString = backendServer.GetMap().getBuildString();
        if (currentBuildString.equals(checkAgainstString)) {
            System.out.println("Check map succeeded...");
            SendMessageBack(CommandList.Command.NULL, "Check succeeded...");
        } else {
            System.out.println("Check map failed, send most recent to edge...");
            SendMessageBack(CommandList.Command.CHECK_MAP, currentBuildString);
        }

        backendServer.GetLock().readLock().unlock();
    }

    private void SendMessageBack(CommandList.Command command, String msg) {
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
        backendServer.GetConnectedClientSockets().remove(clientSocket);
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
