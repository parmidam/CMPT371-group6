package logic.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import logic.util.ClientSendPacket;
import logic.util.ServerSendPacket;
import logic.util.ServerTile;
import logic.util.TileState;

/** Allows the server to handle client requests */
public class GameClientHandler extends Thread {
    private final ServerTile[][] tiles;
    private final Socket socket;
    private final int totalPlayers;
    
    /** Whether the game has started or not */
    private boolean started = false;
    private int clientID;

    /** Starts or ends the game */
    public void setStarted(boolean started) {
        this.started = started;
    }
    
    public GameClientHandler(int clientID, Socket socket, ServerTile[][] tiles, int totalPlayers) {
        this.clientID = clientID;
        this.socket = socket;
        this.tiles = tiles;
        this.totalPlayers = totalPlayers;
    }

    ObjectInputStream getInput() throws IOException {
        return new ObjectInputStream(socket.getInputStream());
    }

    ObjectOutputStream getOutput() throws IOException {
        return new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    /**
     * Responds to incoming packets from the associated client
     * @throws RuntimeException if there is a network issue
     */
    public void run() {
        while (socket.isConnected()) {
            try {
                ClientSendPacket clientPacket = (ClientSendPacket) getInput().readObject();
                
                switch (clientPacket.type) {
                    case "getID": {
                        handleGetID();
                        break;
                    }
                    case "startGame": {
                        handleStartGame();
                        break;
                    }
                    case "lockTile": {
                        handleLockTile(clientPacket);
                        break;
                    }
                    case "unlockTile": {
                        handleUnlockTile(clientPacket);
                        break;
                    }
                    case "ownTile": {
                        handleOwnTile(clientPacket);
                        break;
                    }
                    case "status": {
                        handleStatus();
                        break;
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Handles the get ID request, returning a client's ID
     */
    private void handleGetID() throws IOException {
        ServerSendPacket packet = new ServerSendPacket();
        packet.clientID = this.clientID;

        getOutput().writeObject(packet);
    }
    
    /**
     * Handles the start game request, starting the game on the server side
     */
    private void handleStartGame() throws IOException {
        ServerSendPacket packet = new ServerSendPacket();
        packet.status = started;

        getOutput().writeObject(packet);
    }

    /**
     * Handles the lock tile request, locking a tile for a client on the game board
     * @param clientPacket The incoming request
     */
    private void handleLockTile(ClientSendPacket clientPacket) throws IOException {
        ServerSendPacket packet = new ServerSendPacket();
        int x = clientPacket.point.getKey();
        int y = clientPacket.point.getValue();

        synchronized (tiles) {
            ServerTile tile = tiles[x][y];

            if (tile.getState() == TileState.free) {
                tile.setState(TileState.painting);
                tile.setOwnerId(clientID);
                packet.status = true;
            } else {
                packet.status = false;
            }

            getOutput().writeObject(packet);
        }
    }
    
    /**
     * Handles the unlock tile request, unlocking a tile on the game board
     * @param clientPacket The incoming request
     */
    private void handleUnlockTile(ClientSendPacket clientPacket) throws IOException {
        ServerSendPacket packet = new ServerSendPacket();
        int x = clientPacket.point.getKey();
        int y = clientPacket.point.getValue();

        synchronized (tiles) {
            ServerTile tile = tiles[x][y];

            if (isUserPainting(tile)) {
                tile.setState(TileState.free);
                tile.setOwnerId(-1);
            }

            getOutput().writeObject(packet);
        }
    }

    /**
     * Handles the own tile request, setting a client as the owner of a tile on the game board
     * @param clientPacket The incoming request
     */
    private void handleOwnTile(ClientSendPacket clientPacket) throws IOException {
        ServerSendPacket packet = new ServerSendPacket();
        int x = clientPacket.point.getKey();
        int y = clientPacket.point.getValue();

        synchronized (tiles) {
            ServerTile tile = tiles[x][y];

            if (isUserPainting(tile)) {
                tile.setState(TileState.owned);
                packet.status = true;
            } else {
                packet.status = false;
            }

            getOutput().writeObject(packet);
        }
    }

    /**
     * Handles the status request, returning the game state and a copy of the board tiles
     * @param clientPacket The incoming request
     */
    private void handleStatus() throws IOException {
        // System.out.println("Handling status..." + tiles.length);

        ServerSendPacket packet = new ServerSendPacket();
        packet.tiles = tiles;
        packet.totalPlayers = totalPlayers;

        getOutput().writeObject(packet);
    }

    /**
     * @return true if this client is currently painting a tile
     */
    private boolean isUserPainting(ServerTile tile) {
        return tile.getState() == TileState.painting && tile.getOwnerId() == clientID;
    }
}
