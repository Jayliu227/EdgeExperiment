package backend;

import utils.MapData;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BackendServer {
    private final int port;
    private List<Socket> connectedClientSockets;

    private MapData mapData;

    // sync primitives
    private ReadWriteLock readWriteLock;

    public BackendServer(int port) {
        this.port = port;
        this.connectedClientSockets = new ArrayList<>();
        this.mapData = new MapData();
        this.readWriteLock = new ReentrantReadWriteLock();
    }

    public void Start() {
        try {
            ServerSocket backend = new ServerSocket(this.port);
            System.out.println("Backend server starts listening on port " + this.port);

            // Timer timer = new Timer();
            // timer.schedule();

            while (true) {
                Socket clientSocket = backend.accept();
                new Thread(new BackendWorker(clientSocket, this)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        BackendServer backend = new BackendServer(8001);
        backend.Start();
    }


    public List<Socket> GetConnectedClientSockets() {
        return connectedClientSockets;
    }

    public MapData GetMap() {
        return mapData;
    }

    public ReadWriteLock GetLock() {
        return readWriteLock;
    }
}
