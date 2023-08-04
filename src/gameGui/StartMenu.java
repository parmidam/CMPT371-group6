package gameGui;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

/** The menu to start the game, shown to the user when the application opens */
public class StartMenu {
    private final Scene scene;
    private final TextField ipText;
    private final TextField numberOfPlayers;

    public StartMenu(EventHandler<MouseEvent> joinServerEventHandler, EventHandler<MouseEvent> startServerEventHandler) {
        GridPane pane = new GridPane();
        pane.setHgap(5);
        pane.setVgap(2);

        Label numOfPlayerLabel = new Label("Number of players:");
        Label ipLabel = new Label("Server IP:");

        ipText = new TextField();
        ipText.setPromptText("Server IP");
        ipText.setText("127.0.0.1");

        numberOfPlayers = new TextField();
        numberOfPlayers.setPromptText("Number of players");
        numberOfPlayers.setText("2");

        Button joinServer = new Button("Join Server");
        Button startServer = new Button("Start Server");

        joinServer.setOnMouseClicked(joinServerEventHandler);
        startServer.setOnMouseClicked(startServerEventHandler);

        pane.add(ipLabel, 0, 0);
        pane.add(ipText, 1, 0);
        pane.add(joinServer, 2, 0);

        pane.add(numOfPlayerLabel, 0, 1);
        pane.add(numberOfPlayers, 1, 1);
        pane.add(startServer, 2, 1);

        scene = new Scene(pane, 500, 100);
    }

    public Scene getMenu() {
        return scene;
    }

    public String getIp() {
        return ipText.getText();
    }

    public Integer getPlayerCount() {
        try {
            return Integer.valueOf(numberOfPlayers.getText());
        } catch (Exception e) {
            return 2;
        }
    }
}
