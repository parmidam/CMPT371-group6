package gameGui;

import javafx.util.Pair;

/** An interface to allow the client and game board to interact with eachother */
public interface ClientGuiEvents {
    public boolean onDragStart(Pair<Integer, Integer> tilePoint);
    public boolean onDragEnd(boolean moreThanHalf, Pair<Integer, Integer> tilePoint);
}
