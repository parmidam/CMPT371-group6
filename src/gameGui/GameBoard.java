package gameGui;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import logic.util.ServerTile;
import logic.util.TileState;

/** Represents the game board, which contains an 8x8 grid of tiles */ 
public class GameBoard {
    private final GridPane gridPane;
    private final GridPane mainPane;
    private final int size = 8;

    private GameTile[][] gameTiles = new GameTile[8][8];

    public GameBoard(int clientID, ClientGuiEvents clientGuiEvents) {
        gridPane = new GridPane();
        gridPane.setHgap(2);
        gridPane.setVgap(2);
        gridPane.setGridLinesVisible(true);

        mainPane = new GridPane();
        mainPane.setVgap(5);

        mainPane.addRow(0, new Label("You are player " + (clientID + 1)));
        mainPane.addRow(1, gridPane);

        addTiles(clientID, clientGuiEvents);
    }

    public GridPane getBoard() {
        return mainPane;
    }

    public int getSize() {
        return size;
    }

    /**
     * Replaces all tiles on the game board with a given set of tiles.
     * Only replaces tiles which are not being drawn on
     */
    public void setTiles(ServerTile[][] tiles) {
        if (tiles == null) return;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (tiles[i][j].getState() != TileState.painting) {
                    gameTiles[i][j].fill(tiles[i][j].getOwnerId());
                }
            }
        }
    }

    private void addTiles(int clientID, ClientGuiEvents clientGuiEvents) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                GameTile tile = new GameTile(50, 50, clientID, clientGuiEvents, new Pair<>(i, j));
                gridPane.add(tile, i, j);
                gameTiles[i][j] = tile;
            }
        }
    }
}
