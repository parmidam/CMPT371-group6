import gameGui.StartMenu;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import logic.client.Client;
import logic.server.Server;

public class Main extends Application {
    private StartMenu menu;
    private Server server;
    private Client client;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage)  {
        EventHandler<MouseEvent> joinServerEventHandler = this.getJoinServerEventHandler(stage);
        EventHandler<MouseEvent> startServerEventHandler = this.getStartServerEventHandler(stage);

        menu = new StartMenu(joinServerEventHandler, startServerEventHandler);
        stage.setScene(menu.getMenu());
        stage.setTitle("Deny and Conquer");
        stage.show();
    }

    /**
     * @param stage The stage on which to render the board
     * @return a handler that joins a server and renders the game board
     */
    private EventHandler<MouseEvent> getJoinServerEventHandler(Stage stage) {
        return event -> {
            try {
                client = new Client(menu.getIp());
                client.start();

                System.out.println("Client started");

                stage.setScene(client.getClientBoard());
            } catch (Exception e) {
                System.out.println("Can't start client");
            }
        };
    }
    
    /** 
     * @param stage The stage on which to render the board
     * @return a handler which starts a server and renders the game board
     */
    private EventHandler<MouseEvent> getStartServerEventHandler(Stage stage) {
        return event -> {
            server = new Server(menu.getPlayerCount());
            server.start();
            System.out.println("Server started");
            try {
                client = new Client("127.0.0.1");
                client.start();

                System.out.println("Client started");

                stage.setScene(client.getClientBoard());
            } catch (Exception e) {
                System.out.println("Can't start client");
            }
        };
    }
}