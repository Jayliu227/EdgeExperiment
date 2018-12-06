using System;
using System.IO;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using UnityEngine;

public abstract class EdgeClient : MonoBehaviour {

    private string hostName = "localhost";
    //private string hostName = "192.168.1.99";
    private int port = 8000;

    private TcpClient clientConnection;
    private NetworkStream stream;
    private StreamReader reader;

    private Thread ListenThread;

    protected void OnDisable()
    {
        RemoveConnection();
    }

    protected void ListenHanlder()
    {
        while (true)
        {
            string commandCode = null;
            string message = null;
            commandCode = ReceiveMessage();
            message = ReceiveMessage();
            ProcessResponse(commandCode, message);
        }
    }

    protected void SetupConnection()
    {
        try
        {
            clientConnection = new TcpClient(hostName, port);
            stream = clientConnection.GetStream();
            reader = new StreamReader(stream);

            // setup a new thread for listening from the server
            ListenThread = new Thread(new ThreadStart(ListenHanlder));
            ListenThread.IsBackground = true;
            ListenThread.Start();
        }
        catch (Exception e)
        {
            Debug.Log("Can't connect to server: " + e.Message);
        }
    }

    protected void RemoveConnection()
    {
        if (clientConnection != null)
        {
            SendMessage("quit");
            clientConnection.Close();
            ListenThread.Abort();
        }
    }

    protected string ReceiveMessage()
    {
        string msg = "null";
        if (stream.CanRead)
        {
            msg = reader.ReadLine();
        }
        return msg;
    }

    protected new void SendMessage(string msg)
    {
        if (stream.CanWrite)
        {
            byte[] msgBytes = Encoding.ASCII.GetBytes(msg + "\n");
            stream.Write(msgBytes, 0, msgBytes.Length);
        }
    }

    protected abstract void ProcessResponse(string commandCode, string reponse);
}
