package logic.util;

import java.io.Serializable;

public class ServerTile implements Serializable {

    int ownerId;
    TileState state;

    public ServerTile() {
        ownerId = -1;
        state = TileState.free;
    }

    public int getOwnerId() {
        return ownerId;
    }

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
