package logic.server;

import logic.util.ClientSendPacket;
import logic.util.ServerSendPacket;
import logic.util.ServerTile;
import logic.util.TileState;

import java.io.*;
import java.net.Socket;

public class GameClientHandler extends Thread {
    private final ServerTile[][] tiles;
    final Socket socket;
    private final int totalPlayers;

    public void setStarted(boolean started) {
        this.started = started;
    }

    boolean started = false;
    int id;

    public GameClientHandler(int id, Socket socket, ServerTile[][] tiles, int totalPlayers) throws IOException {
        this.id = id;
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
    public void run() {
        while (socket.isConnected()) {
            try {
                ClientSendPacket clientPacket = (ClientSendPacket) getInput().readObject();
                switch (clientPacket.type) {
                    case "getID": {
                        ServerSendPacket packet = new ServerSendPacket();
                        packet.id = this.id;
                        getOutput().writeObject(packet);
                        break;
                    }
                    case "startGame": {
                        ServerSendPacket packet = new ServerSendPacket();
                        packet.status = started;
                        getOutput().writeObject(packet);
                        break;
                    }
                    case "lockTile": {
                        ServerSendPacket packet = new ServerSendPacket();
                        int x = clientPacket.point.getKey();
                        int y = clientPacket.point.getValue();
                        synchronized (tiles) {
                            if (tiles[x][y].getState() == TileState.free) {
                                tiles[x][y].setState(TileState.painting);
                                tiles[x][y].setOwnerId(id);
                                packet.status = true;
                            } else {
                                packet.status = false;
                            }
                            getOutput().writeObject(packet);
                        }
                        break;
                    }
                    case "unlockTile": {
                        ServerSendPacket packet = new ServerSendPacket();
                        int x = clientPacket.point.getKey();
                        int y = clientPacket.point.getValue();
                        synchronized (tiles) {
                            if (tiles[x][y].getState() == TileState.painting && tiles[x][y].getOwnerId() == id) {
                                tiles[x][y].setState(TileState.free);
                                tiles[x][y].setOwnerId(-1);
                            }
                            getOutput().writeObject(packet);
                        }
                        break;
                    }
                    case "ownTile": {
                        ServerSendPacket packet = new ServerSendPacket();
                        int x = clientPacket.point.getKey();
                        int y = clientPacket.point.getValue();
                        synchronized (tiles) {
                            if (tiles[x][y].getState() == TileState.painting && tiles[x][y].getOwnerId() == id) {
                                tiles[x][y].setState(TileState.owned);
                                packet.status = true;
                            } else {
                                packet.status = false;
                            }
                            getOutput().writeObject(packet);
                        }
                        break;
                    }
                    case "status": {
                        ServerSendPacket packet = new ServerSendPacket();
                        packet.map = tiles;
                        packet.totalPlayers = totalPlayers;
                        getOutput().writeObject(packet);
                        break;
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
