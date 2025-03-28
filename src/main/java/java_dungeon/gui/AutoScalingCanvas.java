package java_dungeon.gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;

// Custom Region for an auto-scaling canvas
// Updates a scaling factor based on a fixed width/height when the region is resized (used to upscale rendering correctly)
public class AutoScalingCanvas extends Region {
    private final Canvas canvas;

    // scale factor property, use add listener to redraw when the canvas is resized
    private final DoubleProperty scalingPy;

    // The fixed width and height to scale from
    private double fixedWidth;
    private double fixedHeight;

    public AutoScalingCanvas(double width, double height) {
        this.canvas = new Canvas(width, height);
        this.scalingPy = new SimpleDoubleProperty(1.0);

        this.fixedWidth = width;
        this.fixedHeight = height;

        getChildren().add(canvas);
    }

    public GraphicsContext getGraphicsContext2D() {
        return this.canvas.getGraphicsContext2D();
    }
    public Canvas getCanvas() {
        return this.canvas;
    }

    public DoubleProperty scalingProperty() {
        return scalingPy;
    }

    public double getFixedWidth() {
        return fixedWidth;
    }
    public void setFixedWidth(double fixedWidth) {
        this.fixedWidth = fixedWidth;
    }

    public double getFixedHeight() {
        return fixedHeight;
    }
    public void setFixedHeight(double fixedHeight) {
        this.fixedHeight = fixedHeight;
    }

    @Override
    protected void layoutChildren() {
        double x = getInsets().getLeft();
        double y = getInsets().getTop();
        double w = getWidth() - getInsets().getRight() - x;
        double h = getHeight() - getInsets().getBottom() - y;

        // preserve aspect ratio while also staying within the available space
        double sf = Math.min(w / fixedWidth, h / fixedHeight);

        // Update the canvas size
        canvas.setWidth(fixedWidth * sf);
        canvas.setHeight(fixedHeight * sf);

        // Update the scaling property (refresh any drawing attached to it)
        this.scalingPy.set(sf);

        // Center the canvas in this region
        positionInArea(canvas, x, y, w, h, -1, HPos.CENTER, VPos.CENTER);
    }
}
