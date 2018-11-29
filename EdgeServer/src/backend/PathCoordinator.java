package backend;

import java.util.HashMap;
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
        HashMap<Integer, String> idToPathStrings =  backend.inbox.get(myTerm);
        if (idToPathStrings == null || idToPathStrings.size() == 0) {
            backend.inLock.unlock();
            backend.finishLastRound = true;
            System.out.println("No path from the backend worker...");
            return;
        }
        backend.inbox.remove(myTerm);
        // TODO: we filter out some paths and store them into a list of paths

        backend.currentTerm++;
        backend.inLock.unlock();

        // TODO: we compute the path
        System.out.println("We're dealing with " + idToPathStrings.size() + " paths.");
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        backend.outLock.lock();
        System.out.println("Grabbed outlock...");
        while (!backend.allOut) {
            backend.allOutCond.awaitUninterruptibly();
        }
        backend.outbox.put(myTerm, idToPathStrings);
        backend.releaseTerm++;
        backend.allOut = false;
        backend.releaseCond.signalAll();
        backend.outLock.unlock();

        backend.finishLastRound = true;
    }
}
