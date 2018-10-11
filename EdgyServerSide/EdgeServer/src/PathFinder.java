import javafx.util.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathFinder {
    public PathFinder() {
    }

    public List<Pair<Integer, Integer>> DFSPathFind(Pair<Integer, Integer> start, Pair<Integer, Integer> end, MapData map) {
        List<Pair<Integer, Integer>> result = new ArrayList<>();

        Map<Pair<Integer, Integer>, Pair<Integer, Integer>> pathPointer = new HashMap<>();
        boolean[][] visited = new boolean[map.getWidth()][map.getHeight()];
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                visited[i][j] = false;
            }
        }

        int steps = DFSPathFindHelper(start, end, visited, map, pathPointer);

        if (steps != Integer.MAX_VALUE) {
            Pair<Integer, Integer> current = start;
            while (!current.equals(end)) {
                current = pathPointer.get(current);
                result.add(current);
            }
        }

        return result;
    }

    private int DFSPathFindHelper(Pair<Integer, Integer> current, Pair<Integer, Integer> end, boolean[][] visited, MapData map, Map<Pair<Integer, Integer>, Pair<Integer, Integer>> pathPointer) {
        int x = current.getKey();
        int y = current.getValue();
        int destX = end.getKey();
        int destY = end.getValue();

        if (x == destX && y == destY) {
            return 0;
        }

        int width = map.getWidth();
        int height = map.getHeight();

        visited[x][y] = true;
        if (map.getMap().get(x).get(y) == 1) {
            return Integer.MAX_VALUE;
        }

        int shortestDist = Integer.MAX_VALUE;
        int nextX = 0;
        int nextY = 0;

        if (x + 1 < width && !visited[x + 1][y]) {
            int steps = DFSPathFindHelper(new Pair<>(x + 1, y), end, visited, map, pathPointer);
            if (steps < shortestDist) {
                shortestDist = steps;
                nextX = x + 1;
                nextY = y;
            }
        }

        if (x - 1 >= 0 && !visited[x - 1][y]) {
            int steps = DFSPathFindHelper(new Pair<>(x - 1, y), end, visited, map, pathPointer);
            if (steps < shortestDist) {
                shortestDist = steps;
                nextX = x - 1;
                nextY = y;
            }
        }

        if (y + 1 < height && !visited[x][y + 1]) {
            int steps = DFSPathFindHelper(new Pair<>(x, y + 1), end, visited, map, pathPointer);
            if (steps < shortestDist) {
                shortestDist = steps;
                nextX = x;
                nextY = y + 1;
            }
        }

        if (y - 1 >= 0 && !visited[x][y - 1]) {
            int steps = DFSPathFindHelper(new Pair<>(x, y - 1), end, visited, map, pathPointer);
            if (steps < shortestDist) {
                shortestDist = steps;
                nextX = x;
                nextY = y - 1;
            }
        }

        pathPointer.put(current, new Pair<>(nextX, nextY));

        if (shortestDist == Integer.MAX_VALUE) {
            return shortestDist;
        } else {
            return shortestDist + 1;
        }
    }

    public List<Pair<Integer, Integer>> AStarPathFind(Pair<Integer, Integer> start, Pair<Integer, Integer> end, MapData map) {
        throw new NotImplementedException();
    }

    public List<Pair<Integer, Integer>> DijkstraPathFind(Pair<Integer, Integer> start, Pair<Integer, Integer> end, MapData map) {

        throw new NotImplementedException();
    }
}
