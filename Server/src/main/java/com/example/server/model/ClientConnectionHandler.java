package com.example.server.model;

import com.example.server.model.exceptions.ModelException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Handles the continuous listening of incoming client connections and dispatches them to a thread pool.
 * <p>
 * This class acts as the main server loop. It listens on a specified port using a {@link ServerSocket}
 * and delegates the actual communication with each client to a {@link ClientConnectionExecutor}
 * running inside a fixed-size thread pool.
 * </p>
 * <p>
 * <b>Key features:</b>
 * <ul>
 * <li> Uses a fixed thread pool of {@value #MAX_THREAD} to limit concurrent connections and prevent resource exhaustion. </li>
 * <li> Implements a graceful shutdown mechanism: calling {@link #stopServer()} closes the socket,
 * safely breaking the blocking {@code accept()} loop without causing application crashes. </li>
 * </ul>
 * </p>
 * @author Michele Brescia
 */
public class ClientConnectionHandler implements Runnable {
    private final ServerModel model;
    private final ServerSocket socket;
    private static final int MAX_THREAD = 10;

    public ClientConnectionHandler(ServerModel model, int port) throws ModelException{
        this.model = model;

        try {
            this.socket = new ServerSocket(port);
        } catch (IOException e) {
            throw new ModelException(ModelException.ErrorCode.OPERATION_FAILED,e.getMessage());
        }
    }

    @Override
    public void run() {

        model.setLogString("ClientConnectionHandler started");

        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket client = socket.accept();

                model.setLogString("Connection with client established : " + client.getInetAddress());

                executor.execute(new ClientConnectionExecutor(client, model));

            } catch (IOException e) {
                if (socket.isClosed()) break;
                model.setLogString("Couldn't connect with client " + e.getMessage());
            }
        }
        executor.shutdown();
    }

    public void stopServer() {
        try { socket.close(); }
        catch (IOException e) { model.setLogString("Error closing server socket"); }
    }
}
