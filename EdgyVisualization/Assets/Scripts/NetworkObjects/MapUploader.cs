using UnityEngine;
using System.Collections.Generic;
using System;

public class MapUploader : EdgeClient {
    public GameObject grid;
    public GameObject carInstance;
    public Transform mapRoot;
    MapData map;

    void Start ()
    {
        //SetupConnection();
        GenerateMap();
        DrawMap();

        // generate a car instance
        float carX = 1;
        float carY = 0;
        Vector3 pos = new Vector3(carX, carY, 0);
        GameObject car = Instantiate(carInstance, pos, Quaternion.identity);
    }

    void Update()
    {
        if (Input.GetKeyDown(KeyCode.E))
        {
            SendMessage(CommandList.GetCommandCode(Command.ECHO));
            SendMessage("Echo from client...");
        }

        if (Input.GetKeyDown(KeyCode.Space))
        {
            SendMessage(CommandList.GetCommandCode(Command.UPLOAD_MAP));
            SendMessage(map.Stringify());
        }

        if (Input.GetKeyDown(KeyCode.F))
        {
            SendMessage(CommandList.GetCommandCode(Command.FIND_PATH));
            SendMessage("0 3 2 0");
        }
    }

    protected override void ProcessReponse(string commandCode, string reponse)
    {
        Debug.Log(commandCode);
        Debug.Log(reponse);
    }

    private void GenerateMap()
    {
        List<List<int>> mapData = new List<List<int>>();

        var a = new List<int>() { 1, 0, 0, 0 };
        var b = new List<int>() { 0, 0, 0, 1 };
        var c = new List<int>() { 0, 1, 0, 0 };
        var d = new List<int>() { 1, 1, 0, 0 };
        var e = new List<int>() { 0, 0, 0, 0 };
        var f = new List<int>() { 0, 0, 1, 1 };

        mapData.Add(a);
        mapData.Add(b);
        mapData.Add(c);
        mapData.Add(d);
        mapData.Add(e);
        mapData.Add(f);

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
            }
        }
    }
}
