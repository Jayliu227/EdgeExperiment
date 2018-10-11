using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class AutoCar : EdgeClient {

    private Vector3 destination;
    private float speed = 1f;
    private bool isWalking = false;

    void Start () {
        SetDestination(new Vector3(1, 2, 0));
    }
	
	void Update () {
        if (isWalking)
        {
            transform.position = Vector3.MoveTowards(transform.position, destination, Time.deltaTime * speed);
            if (transform.position == destination)
            {
                isWalking = false;
            }
        }
    }

    private void SetDestination(Vector3 dest)
    {
        destination = dest;
        isWalking = true;
    }

    protected override void ProcessReponse(string commandCode, string reponse)
    {
        throw new NotImplementedException();
    }
}
