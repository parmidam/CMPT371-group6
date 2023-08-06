package gameGui;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Pair;


/**
 * Represents a tile on the game board, which can be drawn on and claimed by a user
 */
public class GameTile extends Canvas {
    private final ClientGuiEvents clientGuiEvents;
    /** The tile's coordinate on the game board */
    private final Pair<Integer, Integer> tilePoint;
    private final Paint[] colors = {Color.GREEN, Color.RED, Color.YELLOW, Color.BLUE, Color.DARKGRAY, Color.DEEPPINK};
    private final Paint color;
    
    /** The last position that has been drawn */
    private Pair<Double, Double> lastPos = new Pair<Double, Double>(0., 0.);
    /** Whether this tile is being drawn on */
    private boolean drag = false;
    public int clientID;

    public GameTile(int x, int y, int clientID, ClientGuiEvents clientGuiEvents, Pair<Integer, Integer> tilePoint) {
        super(x, y);

        this.tilePoint = tilePoint;
        this.clientID = clientID;
        this.clientGuiEvents = clientGuiEvents;
        this.color = colors[clientID];

        EventHandler<MouseEvent> onMousePressed = getOnMousePressedHandler();
        setOnMousePressed(onMousePressed);

        EventHandler<MouseEvent> onMouseRelease = getOnMouseReleaseHandler();
        setOnMouseReleased(onMouseRelease);

        EventHandler<MouseEvent> onMouseMoved = getOnMouseMovedHandler();
        setOnMouseDragged(onMouseMoved);

        GraphicsContext gc = getGraphicsContext2D();

        gc.setLineWidth(20);
        gc.setStroke(color);
        gc.setFill(color);
    }
            
    public void fill(int ownerId) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.closePath();

        if (ownerId == -1) {
            gc.clearRect(0, 0, getWidth(), getHeight());
        } else {
            gc.setFill(colors[ownerId]);
            gc.rect(0, 0, getWidth(), getHeight());
            gc.fill();
            gc.setFill(color);
        }
    }

    /**
     * @return a handler which draws a line when the user moves their mouse
     */
    private EventHandler<MouseEvent> getOnMouseMovedHandler() {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (drag) {
                    GraphicsContext gc = getGraphicsContext2D();

                    gc.moveTo(lastPos.getKey(), lastPos.getValue());
                    gc.lineTo(t.getX(), t.getY());
                    gc.stroke();

                    // Set the last drawn position to the current mouse position
                    lastPos = new Pair<Double, Double>(t.getX(), t.getY());
                }
            }
        };
    }

    /**
     * @return a handler which starts drawing a line when the mouse is clicked and held
     */
    private EventHandler<MouseEvent> getOnMousePressedHandler() {
        return new EventHandler<MouseEvent>() {
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
    }

    /**
     * @return a handler which stops drawing a line and claims the square if the user has filled
     * more than 50% of the tile when the mouse is released
     */
    private EventHandler<MouseEvent> getOnMouseReleaseHandler() {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (!drag) { return; }

                drag = false;

                int pixelAmount = getDrawnPixelAmount();
                
                GraphicsContext gc = getGraphicsContext2D();
                gc.closePath();

                endDrag(gc, pixelAmount);
            }
        };
    }

    /**
     * @return the amount of pixels which have been drawn on the tile
     */
    private int getDrawnPixelAmount() {
        int counter = 0;
        WritableImage tileDrawing = snapshot(null, null);

        int width = (int) getWidth();
        int height = (int) getHeight();
        PixelReader pixelReader = tileDrawing.getPixelReader();

        // Check each pixel of the tile for paint
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (pixelReader.getArgb(i, j) != -1) {
                    counter += 1;
                }
            }
        }

        return counter;
    }

    /**
     * 
     * @param gc the graphics context
     * @param pixelAmount the amount of drawn pixels on this tile
     */
    private void endDrag(GraphicsContext gc, int pixelAmount) {
        // If the pixel amount is less than half the area of the tile,
        // unlock the tile and clear the drawing
        if (pixelAmount / getWidth() / getHeight() < .5) {
            clientGuiEvents.onDragEnd(false, tilePoint);
            gc.clearRect(0, 0, getWidth(), getHeight());
        
        // else paint the tile for the player
        } else {
            if (clientGuiEvents.onDragEnd(true, tilePoint)) {
                gc.rect(0, 0, getWidth(), getHeight());
                gc.fill();
            } else {
                gc.clearRect(0, 0, getWidth(), getHeight());
            }
        }        
    }
}
