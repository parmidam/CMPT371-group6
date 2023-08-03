package gameGui;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import logic.util.ServerTile;
import logic.util.TileState;

public class GameBoard {
    private final GridPane gridPane;
    private final GridPane mainPane;
    int size = 8;
    GameTile[][] gameTiles = new GameTile[8][8];

    public GameBoard(int id, ClientGuiEvents clientGuiEvents) {
        gridPane = new GridPane();
        gridPane.setHgap(2);
        gridPane.setVgap(2);
        gridPane.setGridLinesVisible(true);

        mainPane = new GridPane();
        mainPane.setVgap(5);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                GameTile tile = new GameTile(50, 50, id, clientGuiEvents, new Pair<>(i, j));
                gridPane.add(tile, i, j);
                gameTiles[i][j] = tile;
            }
        }
        mainPane.addRow(0, new Label("you are player " + (id + 1)));
        mainPane.addRow(1, gridPane);

    }

    public GridPane getBoard() {
        return mainPane;
    }

    public void setData(ServerTile[][] map) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (map[i][j].getState() != TileState.painting)
                    gameTiles[i][j].fill(map[i][j].getOwnerId());
            }
        }
    }
}
