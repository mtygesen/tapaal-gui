package pipe.gui.undo;

import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.DataLayer;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TransportArc;

public class AddTransportArcCommand extends TAPNElementCommand {
	private final TimedTransportArcComponent transportArcComponent;
	private final TransportArc transportArc;

	public AddTransportArcCommand(TimedTransportArcComponent transportArcComponent,
			TransportArc transportArc, TimedArcPetriNet tapn,
			DataLayer guiModel, DrawingSurfaceImpl view) {
		super(tapn, guiModel, view);
		this.transportArcComponent = transportArcComponent;
		this.transportArc = transportArc;
	}

	@Override
	public void undo() {
		transportArcComponent.delete();
		view.repaint();
	}

	@Override
	public void redo() {
		transportArcComponent.setUnderlyingArc(transportArc);
		transportArcComponent.getConnectedTo().setUnderlyingArc(transportArc);
		transportArcComponent.undelete(view);
		tapn.add(transportArc);
		view.repaint();
	}
}
