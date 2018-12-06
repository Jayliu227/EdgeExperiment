package backend;

import utils.Communicator;
import utils.PathFinder;
import utils.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

public class PathCoordinator extends TimerTask {
    private BackendServer backend;

    public PathCoordinator(BackendServer backend) {
        this.backend = backend;
    }

    @Override
    public void run() {
        if (!backend.finishLastRound) {
            return;
        }
        // since it is spawned every three seconds for now, no lock is required
        backend.finishLastRound = false;

        System.out.println("Coordinating...");

        backend.inLock.lock();
        System.out.println("Grabbed inlock...");
        int myTerm = backend.currentTerm;
        HashMap<Integer, String> idToPathStrings = backend.inbox.get(myTerm);
        if (idToPathStrings == null || idToPathStrings.size() == 0) {
            backend.inLock.unlock();
            backend.finishLastRound = true;
            System.out.println("No path from the backend worker...");
            return;
        }
        backend.inbox.remove(myTerm);
        backend.currentTerm++;
        backend.inLock.unlock();

        System.out.println("We're dealing with " + idToPathStrings.size() + " paths.");

        HashMap<Integer, String> resultMap = new HashMap<>();

        HashMap<Integer, List<Point<Integer>>> idToPaths = new HashMap<>();
        for (HashMap.Entry<Integer, String> idToPathString : idToPathStrings.entrySet()) {
            int id = idToPathString.getKey();
            String pathString = idToPathString.getValue();
            List<Point<Integer>> path = Communicator.DecodePath(pathString);
            idToPaths.put(id, path);
        }

        List<Point<Integer>> startPoints = new ArrayList<>();
        List<Point<Integer>> endPoints = new ArrayList<>();

        List<List<Point<Integer>>> pathsToCoordinate = new ArrayList<>();

        HashMap<Integer, Integer> positionToId = new HashMap<>();
        for (HashMap.Entry<Integer, List<Point<Integer>>> idToPath : idToPaths.entrySet()) {
            int id = idToPath.getKey();
            List<Point<Integer>> path = idToPath.getValue();

            if (path.size() < 2) {
                resultMap.put(id, Communicator.EncodePath(null));
                continue;
            }

            Point<Integer> start = path.get(0);
            Point<Integer> end = path.get(path.size() - 1);
            if (startPoints.contains(start) || endPoints.contains(end)) {
                resultMap.put(id, Communicator.EncodePath(null));
            } else {
                pathsToCoordinate.add(path);
                positionToId.put(pathsToCoordinate.size() - 1, id);

                startPoints.add(start);
                endPoints.add(end);
            }
        }

        // we compute the path
        pathsToCoordinate = new PathFinder().CoordinatePaths(pathsToCoordinate);

        for (int i = 0; i < pathsToCoordinate.size(); i++) {
            int id = positionToId.get(i);
            String newPathString = Communicator.EncodePath(pathsToCoordinate.get(i));

            resultMap.put(id, newPathString);
        }


        backend.outLock.lock();
        System.out.println("Grabbed outlock...");
        while (!backend.allOut) {
            backend.allOutCond.awaitUninterruptibly();
        }
        backend.outbox.put(myTerm, resultMap);
        backend.releaseTerm++;
        backend.allOut = false;
        backend.releaseCond.signalAll();
        backend.outLock.unlock();

        backend.finishLastRound = true;
    }
}
