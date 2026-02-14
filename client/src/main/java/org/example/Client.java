package org.example;

import org.acme.protocol.Command;
import org.acme.protocol.ServerResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client implements AutoCloseable {
    private static final int PORT = 9090;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connect() throws IOException {
        clientSocket = new Socket("127.0.0.1", PORT);
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(clientSocket.getInputStream());
    }

    public ServerResponse send(Command command) throws IOException, ClassNotFoundException {
        out.writeObject(command);
        out.flush();
        return (ServerResponse) in.readObject();
    }

    @Override
    public void close() throws IOException{
        if(out!=null) out.close();
        if(in!=null) in.close();
        if(clientSocket!=null) clientSocket.close();
    }

}
