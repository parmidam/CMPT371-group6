package logic.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import logic.util.ServerTile;

/** A class representing the backend server for the game */
public class Server extends Thread {
    private int connectedPlayerCount = 0;
    private ServerSocket serverSocket;
    
    // Config
    private final int numberOfPlayers;
    private final int port = 12345;
    
    private GameClientHandler[] threadList = new GameClientHandler[8];
    private ServerTile[][] tiles = new ServerTile[8][8];

    public Server(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;

        // Initialize the game board
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ServerTile tile = new ServerTile();
                tiles[i][j] = tile;
            }
        }
    }

    
    @Override
    /**
     * Starts the server, and waits for all players to join
     * @throws RuntimeException if there is a problem with the network when accepting client connections
     */
    public void run() throws RuntimeException {
        try {
            serverSocket = new ServerSocket(port);
            
            System.out.println("Waiting for " + numberOfPlayers + " players to connect");

            while (connectedPlayerCount < numberOfPlayers) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    
                    System.out.println("A new player is connected!");

                    GameClientHandler t = new GameClientHandler(connectedPlayerCount, clientSocket, tiles, numberOfPlayers);
                    threadList[connectedPlayerCount] = t;
                    t.start();

                    connectedPlayerCount += 1;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < connectedPlayerCount; i++) {
            (threadList[i]).setStarted(true);
        }

        System.out.println("All players have connected, starting the game...");
    }
}
