import gameGui.StartMenu;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import logic.client.Client;
import logic.server.Server;

public class Main extends Application {
    StartMenu menu;
    Server server;
    Client client;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage)  {

        EventHandler<MouseEvent> joinServerEventHandler = event -> {
            try {
                client = new Client(menu.getIp());
                client.start();
                System.out.println("client started");
                stage.setScene(client.getClientBoard());

            } catch (Exception e) {
                System.out.println("cant start client");
            }
        };


        EventHandler<MouseEvent> startServerEventHandler = event -> {
            server = new Server(menu.getPlayerCount());
            server.start();
            System.out.println("server started");
            try {
                client = new Client("127.0.0.1");
                client.start();
                System.out.println("client started");
                stage.setScene(client.getClientBoard());

            } catch (Exception e) {
                System.out.println("cant start client");
            }
        };

        menu = new StartMenu(joinServerEventHandler, startServerEventHandler);
        stage.setScene(menu.getMenu());
        stage.setTitle("Deny Conquer");
        stage.show();
    }
}