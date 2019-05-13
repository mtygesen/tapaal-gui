package dk.aau.cs.gui.undo;

import java.awt.Point;

import pipe.gui.CreateGui;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.PlaceTransitionObject;

public class MovePlaceTransitionObject extends Command {
	
	private double newY;
	private double newX;
	private PlaceTransitionObject objectToBeMoved;
	private double oldY;
	private double oldX;
	
	
	public MovePlaceTransitionObject(PlaceTransitionObject object, Point point) {
		objectToBeMoved = object;
		this.newX = point.getX();
		this.newY = point.getY();
		
	}

	@Override
	public void undo() {
		objectToBeMoved.setPositionX(oldX);
		objectToBeMoved.setPositionY(oldY);
		CreateGui.getDrawingSurface().repaintAll();
		CreateGui.getModel().repaintAll(true);
		CreateGui.getDrawingSurface().updatePreferredSize();
	}

	@Override
	public void redo() {
		oldY = objectToBeMoved.getPositionY();
		oldX = objectToBeMoved.getPositionX();
		
		objectToBeMoved.setPositionX(newX);
		objectToBeMoved.setPositionY(newY);
		
	}

}
