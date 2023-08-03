package logic.util;

import javafx.util.Pair;

import java.io.Serializable;

public class ClientSendPacket implements Serializable {
    public String type;
    public Pair<Integer,Integer> point;

}
