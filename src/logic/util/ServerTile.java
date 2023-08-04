package logic.util;

import java.io.Serializable;

/** Represents a tile on the server's game board */
public class ServerTile implements Serializable {
    public int ownerId;
    public TileState state;

    public ServerTile() {
        ownerId = -1;
        state = TileState.free;
    }

    /**
     * @return the client ID of the owner of this tile, or -1 if no client has claimed ownership yet
     */
    public int getOwnerId() {
        return ownerId;
    }

    /**
     * @param ownerId The client ID of the new owner of the tile
     */
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public TileState getState() {
        return state;
    }

    public void setState(TileState state) {
        this.state = state;
    }
}
