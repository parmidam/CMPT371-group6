package gameGui;

import javafx.util.Pair;

public interface ClientGuiEvents {

    public boolean onDragStart(Pair<Integer, Integer> tilePoint);
    public boolean onDragEnd(boolean moreThanHalf, Pair<Integer, Integer> tilePoint);


}
