package logic.server;

import logic.util.ServerTile;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Server extends Thread {


    int connectedPlayer = 0;
    ServerSocket socket;
    final int numberOfPlayer;

    Thread[] threadList = new Thread[8];
    ServerTile[][] tiles = new ServerTile[8][8];

    public Server(int numberOfPlayer) {

        this.numberOfPlayer = numberOfPlayer;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ServerTile tile = new ServerTile();
                tiles[i][j] = tile;
            }
        }
    }

    @Override
    public void run() {
        try {
            socket = new ServerSocket(12345);
            System.out.println("waiting for  : " + numberOfPlayer + " player to connect");
            while (connectedPlayer < numberOfPlayer) {
                try {
                    Socket s = socket.accept();
                    System.out.println("A new player is connected !! ");
                    Thread t = new GameClientHandler(connectedPlayer, s, tiles, numberOfPlayer);
                    threadList[connectedPlayer] = t;
                    t.start();
                    connectedPlayer += 1;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        for (int i = 0; i < connectedPlayer; i++) {
            ((GameClientHandler) threadList[i]).setStarted(true);
        }
        System.out.println("all players connected starting the game");
    }


}
