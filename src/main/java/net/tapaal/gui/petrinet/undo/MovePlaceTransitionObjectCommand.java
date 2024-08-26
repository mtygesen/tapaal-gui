package net.tapaal.gui.petrinet.undo;

import java.awt.Point;

import pipe.gui.TAPAALGUI;
import pipe.gui.canvas.DrawingSurfaceImpl;
import pipe.gui.petrinet.graphicElements.PlaceTransitionObject;

public class MovePlaceTransitionObjectCommand implements Command {
	
	private final int newY;
	private final int newX;
	private final PlaceTransitionObject objectToBeMoved;
	private final int oldY;
	private final int oldX;
    // XXX this should not be part of the state, change should be singaled in a better way
    private final DrawingSurfaceImpl canvas;
	
	
	public MovePlaceTransitionObjectCommand(PlaceTransitionObject object, Point point, DrawingSurfaceImpl canvas) {
		objectToBeMoved = object;
		this.newX = point.x;
		this.newY = point.y;
        this.oldY = objectToBeMoved.getOriginalY();
        this.oldX = objectToBeMoved.getOriginalX();
        this.canvas = canvas;
	}

	@Override
	public void undo() {
		objectToBeMoved.setOriginalX(oldX);
		objectToBeMoved.setOriginalY(oldY);

		objectToBeMoved.updateOnMoveOrZoom();
		objectToBeMoved.repaint();
		canvas.updatePreferredSize();
	}

	@Override
	public void redo() {
		objectToBeMoved.setOriginalX(newX);
		objectToBeMoved.setOriginalY(newY);

		objectToBeMoved.updateOnMoveOrZoom();
		objectToBeMoved.repaint();
		canvas.updatePreferredSize();
	}

}
