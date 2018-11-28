package edge;

import utils.MapData;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EdgeServer {
    // config of edge server
    private final int port;
    private List<Socket> connectedClientSockets;

    // public data
    private MapData mapData;

    // sync primitive
    private ReadWriteLock mapDataLock;

    // constructor
    public EdgeServer(int port) {
        this.port = port;
        this.connectedClientSockets = new ArrayList<>();
        this.mapData = new MapData();
        this.mapDataLock = new ReentrantReadWriteLock();
    }

    public void Start() {
        try {
            ServerSocket edge = new ServerSocket(this.port);
            System.out.println("Edge server starts listening on port " + this.port);
            while (true) {
                Socket clientSocket = edge.accept();
                new Thread(new EdgeWorker(clientSocket, this)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EdgeServer edge = new EdgeServer(8000);
        edge.Start();
    }

    public List<Socket> GetConnectedClientSockets() {
        return connectedClientSockets;
    }

    public MapData GetMap() {
        return mapData;
    }

    public ReadWriteLock GetLock() { return mapDataLock; }
}
