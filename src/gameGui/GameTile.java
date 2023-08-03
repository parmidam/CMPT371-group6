package gameGui;

import javafx.scene.canvas.Canvas;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Pair;


public class GameTile extends Canvas {
    private final ClientGuiEvents clientGuiEvents;
    private final Pair<Integer, Integer> tilePoint;
    boolean drag = false;
    int id;
    final Paint[] Colors = {Color.GREEN, Color.RED, Color.YELLOW, Color.BLUE, Color.DARKGRAY, Color.DEEPPINK};
    private final Paint color;

    Pair<Double, Double> lastPos = new Pair<Double, Double>(0., 0.);

    public GameTile(int x, int y, int id, ClientGuiEvents clientGuiEvents, Pair<Integer, Integer> tilePoint) {
        super(x, y);
        this.tilePoint = tilePoint;
        this.id = id;
        this.clientGuiEvents = clientGuiEvents;
        color = Colors[id];
        setOnMousePressed(onMousePressed);
        setOnMouseReleased(onMouseRelease);
        setOnMouseDragged(onMouseMoved);
        GraphicsContext gc = getGraphicsContext2D();
        gc.setLineWidth(20);
        gc.setStroke(color);
        gc.setFill(color);
    }

    EventHandler<MouseEvent> onMouseRelease =
            new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    if(!drag){
                        return;
                    }
                    drag = false;
                    WritableImage a = snapshot(null, null);
                    int counter = 0;
                    int x = (int) getWidth();
                    int y = (int) getHeight();
                    for (int i = 0; i < x; i++) {
                        for (int j = 0; j < y; j++) {
                            if (a.getPixelReader().getArgb(i, j) != -1) {
                                counter += 1;
                            }
                        }
                    }
                    GraphicsContext gc = getGraphicsContext2D();
                    gc.closePath();

                    if (counter / getWidth() / getHeight() < .5) {
                        clientGuiEvents.onDragEnd(false, tilePoint);
                        gc.clearRect(0, 0, getWidth(), getHeight());
                    } else {
                        if (clientGuiEvents.onDragEnd(true, tilePoint)) {
                            gc.rect(0, 0, getWidth(), getHeight());
                            gc.fill();
                        } else {
                            gc.clearRect(0, 0, getWidth(), getHeight());
                        }

                    }
                }
            };
    EventHandler<MouseEvent> onMousePressed =
            new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    if (clientGuiEvents.onDragStart(tilePoint)) {
                        lastPos = new Pair<Double, Double>(t.getX(), t.getY());
                        GraphicsContext gc = getGraphicsContext2D();
                        gc.beginPath();
                        drag = true;
                    }
                }
            };
    EventHandler<MouseEvent> onMouseMoved =
            new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    if (drag) {
                        GraphicsContext gc = getGraphicsContext2D();
                        gc.moveTo(lastPos.getKey(), lastPos.getValue());
                        gc.lineTo(t.getX(), t.getY());
                        gc.stroke();
                        lastPos = new Pair<Double, Double>(t.getX(), t.getY());
                    }
                }
            };

    public void fill(int ownerId) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.closePath();
        if (ownerId == -1) {
            gc.clearRect(0, 0, getWidth(), getHeight());
        } else {
            gc.setFill(Colors[ownerId]);
            gc.rect(0, 0, getWidth(), getHeight());
            gc.fill();
            gc.setFill(color);
        }
    }
}
