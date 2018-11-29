package utils;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

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
            result.add(start);
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

    public List<List<Point<Integer>>> CoordinatePaths(List<List<Point<Integer>>> paths) {
        // make sure each path has a start and end
        for (List<Point<Integer>> path : paths) if (path.size() < 2) return null;

        // we assume that all paths that get passed in have neither the same start nor the end
        int numOfPaths = paths.size();

        List<List<Point<Integer>>> resultPath = new ArrayList<>();
        for (int i = 0; i < numOfPaths; i++) {
            resultPath.add(new ArrayList<>(0));
        }

        // this would map each point on the map to a list of all time steps where some agents will stay on top of it
        Map<Point<Integer>, List<Integer>> pointTimes = new HashMap<>();

        // this is used to store the id of each path
        Map<List<Point<Integer>>, Integer> pathIDs = new HashMap<>();

        for (int i = 0; i < paths.size(); i++) {
            List<Point<Integer>> path = paths.get(i);
            pathIDs.put(path, i);
        }

        // sort them in decreasing order
        Collections.sort(paths, (o1, o2) -> o2.size() - o1.size());

        for (int i = 0; i < numOfPaths; i++) {
            List<Point<Integer>> currentPath = paths.get(i);
            int currentID = pathIDs.get(currentPath);

            int position = 0;
            int timestamp = 0;

            // add the start to the final result
            resultPath.get(currentID).add(currentPath.get(0));
            // timestamp the start position
            if (!pointTimes.containsKey(currentPath.get(0))) {
                pointTimes.put(currentPath.get(0), new ArrayList<>());
            }
            pointTimes.get(currentPath.get(0)).add(timestamp);

            // we loop from the first to the second to the last
            while(position < currentPath.size() - 1) {
                // and we find the next step to go
                int next = position + 1;
                Point<Integer> nextPoint = currentPath.get(next);
                // we get the mex of next step
                if (!pointTimes.containsKey(nextPoint)) {
                    pointTimes.put(nextPoint, new ArrayList<>());
                }
                int mex = GetMex(pointTimes.get(nextPoint), timestamp + 1);

                for (int j = timestamp + 1; j < mex; j++) {
                    // we stay at current cell
                    Point<Integer> currentPoint = currentPath.get(position);
                    resultPath.get(currentID).add(currentPoint);
                    pointTimes.get(currentPoint).add(j);
                }

                // we add our mex to the next cell
                pointTimes.get(nextPoint).add(mex);
                // and move to the next cell
                resultPath.get(currentID).add(nextPoint);

                // we move our point forward
                position++;
                // update our timestamp
                timestamp = mex;
            }
        }

        return resultPath;
    }

    private int GetMex(List<Integer> time, int desired) {
        while (time.contains(desired)) {
            desired++;
        }
        return desired;
    }

    public List<Point<Integer>> AStarPathFind(Point<Integer> start, Point<Integer> end, MapData map) {
        throw new NotImplementedException();
    }

    public List<Point<Integer>> DijkstraPathFind(Point<Integer> start, Point<Integer> end, MapData map) {

        throw new NotImplementedException();
    }
}
