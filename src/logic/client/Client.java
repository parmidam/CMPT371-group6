package logic.client;

import gameGui.ClientGuiEvents;
import gameGui.GameBoard;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.util.Pair;
import logic.util.ClientSendPacket;
import logic.util.ServerSendPacket;
import logic.util.ServerTile;
import logic.util.TileState;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


public class Client extends Thread {

    Socket socket;
    String serverIP;
    GameBoard board;

    Scene scene;
    Label label = new Label("game has not started yet. waiting for all players to join");
    private int clientID;

    public Client(String ip) throws IOException {
        scene = new Scene(label, 8 * 53, 9 * 53);
        serverIP = ip;
        socket = new Socket(InetAddress.getByName(ip), 12345);
    }

    ObjectInputStream getInput() throws IOException {
        return new ObjectInputStream(socket.getInputStream());
    }

    ObjectOutputStream getOutput() throws IOException {
        return new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        System.out.println("client sending");
        ClientSendPacket packet = new ClientSendPacket();
        packet.type = "getID";
        ServerSendPacket serverPacket;
        try {
            getOutput().writeObject(packet);
            System.out.println("waiting for number");
            serverPacket = (ServerSendPacket) getInput().readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("my num is :" + serverPacket.id);

        ClientGuiEvents guiHandler = new ClientGuiEvents() {
            @Override
            public boolean onDragStart(Pair<Integer, Integer> tilePoint) {
                ClientSendPacket packet = new ClientSendPacket();
                packet.type = "lockTile";
                packet.point = tilePoint;
                try {
                    getOutput().writeObject(packet);
                    ServerSendPacket serverPacket = (ServerSendPacket) getInput().readObject();
                    return serverPacket.status;
                } catch (IOException | ClassNotFoundException e) {
                    return false;
                }
            }

            @Override
            public boolean onDragEnd(boolean moreThanHalf, Pair<Integer, Integer> tilePoint) {
                ClientSendPacket packet = new ClientSendPacket();
                try {
                    if (moreThanHalf) {
                        packet.type = "ownTile";
                        packet.point = tilePoint;
                        getOutput().writeObject(packet);
                        ServerSendPacket serverPacket = (ServerSendPacket) getInput().readObject();
                        return serverPacket.status;

                    } else {
                        packet.type = "unlockTile";
                        packet.point = tilePoint;
                        getOutput().writeObject(packet);
                        ServerSendPacket serverPacket = (ServerSendPacket) getInput().readObject();
                        return serverPacket.status;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    return false;
                }
            }
        };



        this.clientID = serverPacket.id;
        board = new GameBoard(serverPacket.id, guiHandler);

        packet.type = "startGame";
        System.out.println("waiting for all players");
        while (!serverPacket.status) {
            try {
                getOutput().writeObject(packet);
                serverPacket = (ServerSendPacket) getInput().readObject();
                Thread.sleep(1000);
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                System.out.println("some network error");
            }
        }
        scene.setRoot(board.getBoard());

        while (!checkEndGame(serverPacket.map)) {
            try {
                packet.type = "status";
                getOutput().writeObject(packet);
                serverPacket = (ServerSendPacket) getInput().readObject();
                board.setData(serverPacket.map);
                Thread.sleep(1000);
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                System.out.println("some network error");
            }
        }

        showGameWinner(serverPacket.map, serverPacket.totalPlayers);
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("failed to close socket");
        }

    }

    private void showGameWinner(ServerTile[][] map, int totalPlayers) {
        int[] playersScores = new int[totalPlayers];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                playersScores[map[i][j].getOwnerId()] += 1;
            }
        }
        int winner = 0;
        int bestScore = playersScores[0];
        for (int i = 1; i < totalPlayers; i++) {
            if (playersScores[i] > bestScore) {
                bestScore = playersScores[i];
                winner = i;
            }
        }
        String status;
        if (winner == clientID) {
            status = "win";
        } else {
            status = "loose";
        }
        StringBuilder text = new StringBuilder("you " + status + " !!!");
        for (int i = 0; i < totalPlayers; i++) {
            text.append("\n" + "player ").append(i + 1).append(" : ").append(playersScores[i]);
        }
        Label label = new Label(text.toString());
        scene.setRoot(label);
    }

    private boolean checkEndGame(ServerTile[][] map) {
        if (map == null) {
            return false;
        }
        int counter = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (map[i][j].getState() == TileState.owned) {
                    counter += 1;
                }
            }
        }
        return counter == 64;
    }


    public Scene getClientBoard() {
        return scene;
    }

}
