package utils;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathFinder {
    public PathFinder() {
    }

    public List<Point<Integer>> DFSPathFind(Point<Integer> start, Point<Integer> end, MapData map) {
        List<Point<Integer>> result = new ArrayList<>();

        Map<Point<Integer>, Point<Integer>> pathPointer = new HashMap<>();
        boolean[][] visited = new boolean[map.getWidth()][map.getHeight()];
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                visited[i][j] = false;
            }
        }

        int steps = DFSPathFindHelper(start, end, visited, map, pathPointer);

        if (steps != Integer.MAX_VALUE) {
            Point<Integer> current = start;
            while (!current.isSame(end)) {
                current = pathPointer.get(current);
                result.add(current);
            }
        }

        return result;
    }

    private int DFSPathFindHelper(Point<Integer> current, Point<Integer> end, boolean[][] visited, MapData map, Map<Point<Integer>, Point<Integer>> pathPointer) {
        int x = current.getX();
        int y = current.getY();
        int destX = end.getX();
        int destY = end.getY();

        visited[x][y] = true;

        if (map.getMap().get(x).get(y) == 1) {
            return Integer.MAX_VALUE;
        }

        if (x == destX && y == destY) {
            visited[x][y] = false;
            return 0;
        }

        int width = map.getWidth();
        int height = map.getHeight();

        int shortestDist = Integer.MAX_VALUE;
        int nextX = -1;
        int nextY = -1;

        if (x + 1 < width && !visited[x + 1][y]) {
            int steps = DFSPathFindHelper(new Point<>(x + 1, y), end, visited, map, pathPointer);
            if (steps < shortestDist) {
                shortestDist = steps;
                nextX = x + 1;
                nextY = y;
            }
        }

        if (x - 1 >= 0 && !visited[x - 1][y]) {
            int steps = DFSPathFindHelper(new Point<>(x - 1, y), end, visited, map, pathPointer);
            if (steps < shortestDist) {
                shortestDist = steps;
                nextX = x - 1;
                nextY = y;
            }
        }

        if (y + 1 < height && !visited[x][y + 1]) {
            int steps = DFSPathFindHelper(new Point<>(x, y + 1), end, visited, map, pathPointer);
            if (steps < shortestDist) {
                shortestDist = steps;
                nextX = x;
                nextY = y + 1;
            }
        }

        if (y - 1 >= 0 && !visited[x][y - 1]) {
            int steps = DFSPathFindHelper(new Point<>(x, y - 1), end, visited, map, pathPointer);
            if (steps < shortestDist) {
                shortestDist = steps;
                nextX = x;
                nextY = y - 1;
            }
        }

        visited[x][y] = false;

        if (shortestDist == Integer.MAX_VALUE) {
            return shortestDist;
        } else {
            pathPointer.put(current, new Point<>(nextX, nextY));
            return shortestDist + 1;
        }
    }

    public List<Point<Integer>> AStarPathFind(Point<Integer> start, Point<Integer> end, MapData map) {
        throw new NotImplementedException();
    }

    public List<Point<Integer>> DijkstraPathFind(Point<Integer> start, Point< Integer> end, MapData map) {

        throw new NotImplementedException();
    }
}
