using System;
using System.Collections;
using System.Collections.Generic;
using System.Threading;
using UnityEngine;

public class AutoCar : EdgeClient {
    // the point of the end of one specific path
    private Vector3 destination;
    // the current next goint point
    private Vector3 target;
    private float speed = 1f;
    private bool isWalking = false;
    private bool isAutomatic = true;
    private bool isSentRequest = false;

    private float coolDown;
    private float tick = 0;
    private Mutex mutex;
    private Queue<Vector3> movementQueue;

    // Unity reference
    private TextMesh text;

    void Start () {
        SetupConnection();
        mutex = new Mutex();
        movementQueue = new Queue<Vector3>();
        text = transform.GetChild(1).GetComponent<TextMesh>();

        destination = transform.position;
        speed = UnityEngine.Random.Range(1f, 3f);

        // TEST: just make all of them have constant speed for now
        speed = 1f;

        coolDown = UnityEngine.Random.Range(1f, 5f);
        tick = coolDown;
    }

    void Update () {
        tick -= Time.deltaTime;

        // if there is anything in the queue and we're not on the way
        if (isWalking && isAutomatic)
        {
            transform.position = Vector3.MoveTowards(transform.position, target, Time.deltaTime * speed);
            if (transform.position == target)
            {
                isWalking = false;
            }
        }

        mutex.WaitOne();
        if (movementQueue.Count > 0 && !isWalking && isAutomatic)
        {
            // take a new destination
            target = movementQueue.Dequeue();
            isWalking = true;
        }
        mutex.ReleaseMutex();

        if (tick < 0 && !isWalking && !isSentRequest)
        {
            Vector3 newPoint = MapUploader.GetRandomPointOnMap();
            while (newPoint == transform.position)
            {
                newPoint = MapUploader.GetRandomPointOnMap();
            }
            RequestForPath(newPoint);
            isSentRequest = true;
            tick = coolDown;
        }

        text.text = "(" + destination.x + "," + destination.y + ")";
    }

    // send a remote request to the server
    private void RequestForPath(Vector3 newTargetPosition)
    {
        Debug.Log("Request a path to " + newTargetPosition.x + " " + newTargetPosition.y);
        SendMessage(CommandList.GetCommandCode(Command.FIND_PATH));
        string newRequest = "";
        newRequest += (int)transform.position.x + " " + (int)transform.position.y + " ";
        newRequest += (int)newTargetPosition.x + " " + (int)newTargetPosition.y;
        SendMessage(newRequest);
    }

    // when it receieves a response, this function would be called
    protected override void ProcessResponse(string commandCode, string reponse)
    {
        if (commandCode.Equals(CommandList.GetCommandCode(Command.NULL)))
        {
            isSentRequest = false;
        }

        if (commandCode.Equals(CommandList.GetCommandCode(Command.FIND_PATH)))
        {
            Debug.Log("Received path answer from server");

            // decode reponse
            string[] elements = reponse.Split(' ');
            int numOfSteps = int.Parse(elements[0]);
            
            // +1 because there is a space character at the end
            if ((numOfSteps * 2 + 1 + 1) != (elements.Length) || numOfSteps == 0)
            {
                Debug.Log("Reponse has invalid form.");
                return;
            }

            mutex.WaitOne();
            for (int i = 0; i < numOfSteps; i++)
            {
                // the input is: numOfSteps s1a s1b s2a s2b ...
                int firstIndex = i * 2 + 1;
                int secondIndex = i * 2 + 2;

                int x = int.Parse(elements[firstIndex]);
                int y = int.Parse(elements[secondIndex]);

                destination = new Vector3(x, y, 0);
                movementQueue.Enqueue(destination);
            }        
            mutex.ReleaseMutex();

            isSentRequest = false;
        }
    }
}
