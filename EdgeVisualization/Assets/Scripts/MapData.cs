using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MapData {

    private string name;
    private int id;

    private List<List<int>> map;
    private int width;
    private int height;

    public MapData(string name, int id, List<List<int>> map)
    {
        this.name = name;
        this.id = id;
        this.map = map;

        width = map.Count;
        height = width == 0 ? 0 : map[0].Count;
    }

    public void SetMap(List<List<int>> newMap)
    {
        map = newMap;
        width = map.Count;
        height = ((width == 0) ? 0 : map[0].Count);
    }

    public void SetPoint(int x, int y, int value)
    {
        if (x < width && y < height)
        {
            map[x][y] = value;
        }
    }

    public int GetValue(int x, int y)
    {
        if (x < width && y < height)
        {
            return map[x][y];
        }

        return -1;
    }

    public int GetWidth() { return width; }

    public int GetHeight() { return height; }

    public string Stringify()
    {
        string result = "";
        // add id and name
        result += id + " " + name + " ";
        // add width and height
        result += width + " " + height + " ";
        // add each element in the array
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                result += (map[i][j].ToString() + " ");
            }
        }

        return result;
    }
}
