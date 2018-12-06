package backend;

import com.sun.tools.corba.se.idl.IncludeGen;
import utils.MapData;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.locks.*;

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

    // start every thing that's used for path coordination
    public boolean finishLastRound = true;

    public final Lock inLock = new ReentrantLock(true);
    public final Lock outLock = new ReentrantLock(true);

    public int currentTerm = 0;
    public int releaseTerm = -1;

    public boolean allOut = true;

    public Condition releaseCond = outLock.newCondition();
    public Condition allOutCond = outLock.newCondition();

    public HashMap<Integer, HashMap<Integer, String>> inbox = new HashMap<>();
    public HashMap<Integer, HashMap<Integer, String>> outbox = new HashMap<>();

    private int threadId = 0;
    private int intervalPeriod = 5;
    // end every thing that's used for path coordination

    public void Start() {
        try {
            ServerSocket backend = new ServerSocket(this.port);
            System.out.println("Backend server starts listening on port " + this.port);

            Timer timer = new Timer();
            timer.schedule(new PathCoordinator(this), 0, intervalPeriod * 1000);

            while (true) {
                Socket clientSocket = backend.accept();
                new Thread(new BackendWorker(clientSocket, this, threadId++)).start();
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
