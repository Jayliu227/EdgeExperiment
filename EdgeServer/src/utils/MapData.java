package utils;

import java.util.ArrayList;
import java.util.List;

public class MapData {
    private String name;
    private int id;

    private List<List<Integer>> map;
    private int width;
    private int height;

    private boolean isValid;
    private String buildString;

    public MapData() {
        isValid = false;
    }

    public void PrintMap() {
        if (!isValid) {
            System.out.printf("Map is not valid.");
            return;
        }

        System.out.println("Start printing map: id-> " + id + " name-> " + name);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                System.out.print(map.get(i).get(j) + " ");
            }
            System.out.println();
        }
    }

    public void BuildMap(String buildString) {
        isValid = true;
        this.buildString = buildString;
        String[] elements = buildString.split(" ");

        // check if it contains at least width and height
        if (elements.length < 4) {
            isValid = false;
            return;
        }

        id = Integer.parseInt(elements[0]);
        name = elements[1];
        int x = Integer.parseInt(elements[2]);
        int y = Integer.parseInt(elements[3]);

        // check validity of size
        if (x > 0 && y > 0) {
            this.width = x;
            this.height = y;
        } else {
            isValid = false;
            return;
        }

        // check if it has enough data in it
        if (width * height > (elements.length - 4)) {
            isValid = false;
            return;
        }

        map = new ArrayList<>();

        for (int i = 0; i < width; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < height; j++) {
                int value = Integer.parseInt(elements[4 + i * height + j]);
                row.add(value);
            }
            map.add(row);
        }
    }

    public boolean getIsValid() {
        return isValid;
    }

    public List<List<Integer>> getMap() {
        return map;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getBuildString() { return buildString; }

    @Override
    public boolean equals(Object obj) {
        return getBuildString().equals(((MapData)obj).getBuildString());
    }
}
