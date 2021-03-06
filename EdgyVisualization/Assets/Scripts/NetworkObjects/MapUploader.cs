﻿using UnityEngine;
using System.Collections.Generic;
using System;

public class MapUploader : EdgeClient {
    public GameObject grid;
    public GameObject carInstance;
    public Transform mapRoot;
    private static MapData map;
    private List<List<GameObject>> mapObjects;

    const int NUM_OF_CARS = 4;

    void Start ()
    {
        mapObjects = new List<List<GameObject>>();

        SetupConnection();
        GenerateMap();
        DrawMap();

        SendMessage(CommandList.GetCommandCode(Command.UPLOAD_MAP));
        SendMessage(map.Stringify());

        SpawnCars();
    }

    void Update()
    {

    }

    protected override void ProcessResponse(string commandCode, string reponse)
    {
        if (commandCode.Equals(CommandList.GetCommandCode(Command.NULL)))
        {
            Debug.Log("Upload map failed..");
        } else
        {
            Debug.Log(reponse);
        }
    }

    private void GenerateMap()
    {
        List<List<int>> mapData = new List<List<int>>();

        /*
        int width = 50;
        int height = 50;

        for (int i = 0; i < width; i++)
        {
            List<int> row = new List<int>();
            for (int j = 0; j < height; j++)
            {
                int rand = UnityEngine.Random.Range(0, 1);
                if (rand < 0.7)
                {
                    row.Add(0);
                } else
                {
                    row.Add(1);
                }
            }
            mapData.Add(row);
        }
        */
        var a = new List<int>() { 1, 0, 0, 0, 0 };
        var b = new List<int>() { 0, 0, 0, 1, 0 };
        var c = new List<int>() { 0, 1, 0, 0, 0 };
        var d = new List<int>() { 0, 0, 1, 0, 1 };
        var e = new List<int>() { 0, 1, 0, 0, 1 };
        var f = new List<int>() { 0, 0, 0, 0, 0 };
        var g = new List<int>() { 1, 0, 1, 0, 1 };

        mapData.Add(a);
        mapData.Add(b);
        mapData.Add(c);
        mapData.Add(d);
        mapData.Add(e);
        mapData.Add(f);
        mapData.Add(g);

        map = new MapData("FirstMap", 1, mapData);
    }

    private void DrawMap()
    {
        if (map == null)
        {
            Debug.Log("Can't find any map data.");
            return;
        }

        int width = map.GetWidth();
        int height = map.GetHeight();

        float xOffset = width * 1.0f / 2;
        float yOffset = height * 1.0f / 2;

        // adjust the camera position
        Camera.main.transform.position += new Vector3(xOffset, yOffset, 0);

        for (int i = 0; i < width; i++)
        {
            List<GameObject> mapRowObj = new List<GameObject>();
            for(int j = 0; j < height; j++)
            {
                int value = map.GetValue(i, j);

                Vector3 pos = new Vector3(i, j, 0);
                GameObject go = Instantiate(grid, pos, Quaternion.identity, mapRoot);

                if (value == 0)
                {
                    go.transform.GetChild(0).GetComponent<SpriteRenderer>().color = Color.white;
                }
                else
                {
                    go.transform.GetChild(0).GetComponent<SpriteRenderer>().color = Color.yellow;
                }

                mapRowObj.Add(go);
            }
            mapObjects.Add(mapRowObj);
        }
    }

    private void SpawnCars()
    {
    	List<Vector3> usedPoints = new List<Vector3>();
        for (int i = 0; i < NUM_OF_CARS; i++)
        {
        	Vector3 point = GetRandomPointOnMap();
        	while (usedPoints.Contains(point)) {
        		point = GetRandomPointOnMap();
        	}

            GameObject car = Instantiate(carInstance, point, Quaternion.identity);
            car.transform.GetChild(0).GetComponent<SpriteRenderer>().color = UnityEngine.Random.ColorHSV(0f, 1f, 1f, 1f, 0.5f, 1f);
        	usedPoints.Add(point);
        }
    }

    public static Vector3 GetRandomPointOnMap()
    {
        int width = map.GetWidth();
        int height = map.GetHeight();

        int randomX;
        int randomY;
        while (true)
        {
            randomX = UnityEngine.Random.Range(0, width);
            randomY = UnityEngine.Random.Range(0, height);

            if (map.GetValue(randomX, randomY) == 0)
            {
                break;
            }
        }

        return new Vector3(randomX, randomY, 0);
    }
}
