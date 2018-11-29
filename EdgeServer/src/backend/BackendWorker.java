package backend;

import utils.CommandList;
import utils.Communicator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class BackendWorker implements Runnable {
    private final Socket clientSocket;
    private final BackendServer backendServer;

    private final int id;

    private BufferedReader reader;
    private PrintWriter writer;

    private Communicator communicator;

    public BackendWorker(Socket clientSocket, BackendServer backendServer, int id) {
        this.clientSocket = clientSocket;
        this.backendServer = backendServer;
        this.id = id;

        backendServer.GetConnectedClientSockets().add(clientSocket);
    }

    @Override
    // NOTE: backend server won't keep the socket forever, it only processes one request at a time
    public void run() {
        try {
            System.out.println("Connection from client " + clientSocket.getInetAddress());

            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new PrintWriter(clientSocket.getOutputStream());

            this.communicator = new Communicator(writer);

            String line = reader.readLine();
            // filter all non numeric value
            line = line.replaceAll("[^0-9]", "9");
            // if the input is more than three digits, then it is invalid.
            if (line.toCharArray().length <= 3) {
                int commandCode = Integer.parseInt(line);
                Execute(commandCode);
            } else {
                communicator.SendMessageBack(CommandList.Command.NULL, "Command Input incorrect...");
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
                communicator.SendMessageBack(CommandList.Command.TEST, "Message from backend server...");
                break;
            case 101:
                UploadMapHandler();
                break;
            case 103:
                CheckMapHandler();
                break;
            case 106:
                CoordinateHandler();
            default:
                communicator.SendMessageBack(CommandList.Command.NULL, "Cannot recognize command...");
        }
    }

    private void CoordinateHandler() throws IOException {
        String path = this.reader.readLine();

        // inbox
        backendServer.inLock.lock();
        int myTerm = backendServer.currentTerm;
        if (!backendServer.inbox.containsKey(myTerm)) {
            backendServer.inbox.put(myTerm, new HashMap<>());
        }
        backendServer.inbox.get(myTerm).put(id, path);
        backendServer.inLock.unlock();

        // outbox
        backendServer.outLock.lock();
        while (myTerm > backendServer.releaseTerm) {
            backendServer.releaseCond.awaitUninterruptibly();
        }

        if (!backendServer.outbox.containsKey(myTerm)) {
            System.out.println("No result of the same term from outbox");
            communicator.SendMessageBack(CommandList.Command.NULL, "Unable to find valid result");
            backendServer.outLock.unlock();
            return;
        }

        HashMap<Integer, String> currentTable = backendServer.outbox.get(myTerm);
        if (!currentTable.containsKey(id)) {
            System.out.println("Unable to find result from outbox");
            communicator.SendMessageBack(CommandList.Command.NULL, "Unable to find valid result");
            backendServer.outLock.unlock();
            return;
        }

        path = currentTable.get(id);
        currentTable.remove(id);
        if (currentTable.size() == 0) {
            backendServer.outbox.remove(myTerm);
            backendServer.allOut = true;
            backendServer.allOutCond.signalAll();
        } else {
            backendServer.outbox.replace(myTerm, currentTable);
        }
        // send message back
        communicator.SendMessageBack(CommandList.Command.COORDINATE_PATH, path);
        backendServer.outLock.unlock();
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
            communicator.SendMessageBack(CommandList.Command.UPLOAD_MAP, response);
        } else {
            response = "Failed to upload mao information.";
            communicator.SendMessageBack(CommandList.Command.NULL, response);
        }
    }

    private void CheckMapHandler() throws IOException {
        // reply with the string builder if it is not updated, otherwise just reply null
        String checkAgainstString = this.reader.readLine();

        backendServer.GetLock().readLock().lock();

        String currentBuildString = backendServer.GetMap().getBuildString();
        if (currentBuildString.equals(checkAgainstString)) {
            System.out.println("Check map succeeded...");
            communicator.SendMessageBack(CommandList.Command.NULL, "Check succeeded...");
        } else {
            System.out.println("Check map failed, send most recent to edge...");
            communicator.SendMessageBack(CommandList.Command.CHECK_MAP, currentBuildString);
        }

        backendServer.GetLock().readLock().unlock();
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
