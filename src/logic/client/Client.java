package logic.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import gameGui.ClientGuiEvents;
import gameGui.GameBoard;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.util.Pair;
import logic.util.ClientSendPacket;
import logic.util.ServerSendPacket;
import logic.util.ServerTile;
import logic.util.TileState;

/** A class allowing the game to interact with the server */
public class Client extends Thread {
    private Socket socket;
    private GameBoard board;
    private Scene scene;
    private int clientID;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Client(String ip) throws IOException {
        Label waitingLabel = new Label("Game has not started yet, please wait for all players to join.");

        scene = new Scene(waitingLabel, 8 * 53, 9 * 53);
        socket = new Socket(InetAddress.getByName(ip), 12345);
    }

    ObjectInputStream getInput() throws IOException {
        return in;
    }

    ObjectOutputStream getOutput() throws IOException {
        return out;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Client sending");
        
        ServerSendPacket lastServerPacket = getClientIDPacket();
        this.clientID = lastServerPacket.clientID;
        
        System.out.println("My client ID is :" + clientID);

        ClientGuiEvents guiHandler = getGUIHandler();
        board = new GameBoard(clientID, guiHandler);
        
        lastServerPacket = startGame(lastServerPacket);
        lastServerPacket = runGameLoop(lastServerPacket);
        
        showGameWinner(lastServerPacket.tiles, lastServerPacket.totalPlayers);
        
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Failed to close socket");
        }
    }

    /**
     * Calculates the winner, and displays the score and winner on the screen
     */
    private void showGameWinner(ServerTile[][] boardTiles, int totalPlayers) {
        int[] playersScores = new int[totalPlayers];

        // Calculates each player's score
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                playersScores[boardTiles[i][j].getOwnerId()] += 1;
            }
        }
        
        int winner = 0;
        int bestScore = playersScores[0];

        // Calculate the player with the highest score
        for (int i = 1; i < totalPlayers; i++) {
            if (playersScores[i] > bestScore) {
                bestScore = playersScores[i];
                winner = i;
            }
        }

        StringBuilder text = new StringBuilder("You " + (winner == clientID ? "win" : "lose") + "!!!");

        for (int i = 0; i < totalPlayers; i++) {
            text.append("\nPlayer ").append(i + 1).append(" : ").append(playersScores[i]);
        }
        
        Label label = new Label(text.toString());
        scene.setRoot(label);
    }

    /**
     * Returns true if all tiles have been claimed
     */
    private boolean isGameOver(ServerTile[][] gameTiles) {
        if (gameTiles == null) {
            return false;
        }

        int counter = 0;

        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (gameTiles[i][j].getState() == TileState.owned) {
                    counter += 1;
                }
            }
        }

        return counter == board.getSize() * board.getSize();
    }

    public Scene getClientBoard() {
        return scene;
    }

    /**
     * Fetches this client's id from the server
     * @throws RuntimeException if there is a problem reading the server's response
     */
    private ServerSendPacket getClientIDPacket() throws RuntimeException {
        ServerSendPacket serverPacket;
        ClientSendPacket clientPacket = new ClientSendPacket();
        clientPacket.type = "getID";

        try {
            sendPacket(clientPacket);
            System.out.println("Waiting for client ID...");
            serverPacket = (ServerSendPacket) getInput().readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }

        return serverPacket;
    }

    /**
     * Polls the server until all players have joined, and the game can be started.
     * @param lastServerPacket The last packet returned by the server, which contains the game state
     * @return the latest packet returned by the server
     */
    private ServerSendPacket startGame(ServerSendPacket lastServerPacket) {
        ServerSendPacket serverPacket = lastServerPacket;
        ClientSendPacket clientPacket = new ClientSendPacket();
        clientPacket.type = "startGame";

        System.out.println("Waiting for all players");

        // Poll every second to see if all players have joined the game yet
        while (!serverPacket.status) {
            try {
                sendPacket(clientPacket);
                serverPacket = (ServerSendPacket) getInput().readObject();
                Thread.sleep(1000);
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                System.out.println("A network error ocurred");
            }
        }

        // Initialize the game board
        scene.setRoot(board.getBoard());

        return serverPacket;
    }

    /**
     * Runs the game loop, polling the server for the latest game state
     * and re-rendering the game board
     * @param lastServerPacket The last packet returned by the server, which contains the game state
     * @return the latest packet returned by the server
     */
    private ServerSendPacket runGameLoop(ServerSendPacket lastServerPacket) {
        ServerSendPacket serverPacket = lastServerPacket;
        ClientSendPacket clientPacket = new ClientSendPacket();

        while (!isGameOver(serverPacket.tiles)) {
            try {
                clientPacket.type = "status";
                sendPacket(clientPacket);
                
                serverPacket = (ServerSendPacket) getInput().readObject();

                // Re-render game board with the new game board
                board.setTiles(serverPacket.tiles);

                Thread.sleep(250);
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                System.out.println("A network error ocurred");
            }
        }

        return serverPacket;
    }

    private ClientGuiEvents getGUIHandler() {
        return new ClientGuiEvents() {
            @Override
            /** Locks a tile when the user starts to draw on a tile 
             * @param tilePoint the tile the user is drawing on
             * @return if the server call to lock the tile was successful
            */
            public boolean onDragStart(Pair<Integer, Integer> tilePoint) {
                ClientSendPacket packet = new ClientSendPacket();
                packet.type = "lockTile";
                packet.point = tilePoint;

                try {
                    sendPacket(packet);
                    ServerSendPacket serverPacket = (ServerSendPacket) getInput().readObject();
                    return serverPacket.status;
                } catch (IOException | ClassNotFoundException e) {
                    return false;
                }
            }

            @Override
            /** Unlocks or claims a tile when the user starts to draw on a tile
             * @param moreThanHalf whether the user has painted at least 50% of the tile
             * @param tilePoint the tile the user is drawing on
             * @return if the server call to unlock/claim the tile was successful
            */
            public boolean onDragEnd(boolean moreThanHalf, Pair<Integer, Integer> tilePoint) {
                ClientSendPacket packet = new ClientSendPacket();

                try {
                    if (moreThanHalf) {
                        packet.type = "ownTile";
                        packet.point = tilePoint;
                        sendPacket(packet);
                        
                        ServerSendPacket serverPacket = (ServerSendPacket) getInput().readObject();
                        return serverPacket.status;
                    } else {
                        packet.type = "unlockTile";
                        packet.point = tilePoint;
                        sendPacket(packet);
                        
                        ServerSendPacket serverPacket = (ServerSendPacket) getInput().readObject();
                        return serverPacket.status;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    return false;
                }
            }
        };
    }

    /**
     * Sends a packet to the server
     * Also resets the OutputStream, ensuring duplicate objects can be written again
     * @throws IOException if there is a network error
     */
    private void sendPacket(ClientSendPacket packet) throws IOException {
        getOutput().writeUnshared(packet);
        getOutput().reset();
    }
}
