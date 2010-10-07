package dk.aau.cs.translations.tapn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.StandardUPPAALQuery;
import dk.aau.cs.TA.TimedAutomaton;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.TCTL.visitors.OptimizedStandardTranslationQueryVisitor;
import dk.aau.cs.TCTL.visitors.QueryVisitor;
import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;

public class TAPNToNTASymmetryTransformer extends TAPNToNTATransformer{
	private int numberOfInitChannels = 0;
	protected static final String TokenTAName = "P";
	public TAPNToNTASymmetryTransformer(int extraNumberOfTokens) {
		super(extraNumberOfTokens);
	}


	@Override
	protected List<TimedAutomaton> createAutomata(TimedArcPetriNet model){
		List<TimedAutomaton> tas = createTokenAutomata(model);

		TimedAutomaton control = createControlTemplate(model);
		tas.add(control);

		return tas;
	}

	private List<TimedAutomaton> createTokenAutomata(TimedArcPetriNet model) {
		TimedAutomaton token = new TimedAutomaton();
		TimedAutomaton lock = new TimedAutomaton();

		createLocations(model, token, lock);
		token.setName(TokenTAName);
		token.setParameters("const pid_t id");
		token.setDeclarations("clock " + CLOCK_NAME + ";");
		token.setInitLocation(getLocationByName("P_capacity"));

		lock.setName("Lock");
		lock.setInitLocation(getLocationByName("P_lock"));

		createTransitions(model, token, lock);
		addSymmetricInitialization(token, model);

		ArrayList<TimedAutomaton> tas = new ArrayList<TimedAutomaton>();
		tas.add(token);
		if(lock.getLocations().size() > 0){
			tas.add(lock);
		}
		return tas;
	}

	private void createTransitions(TimedArcPetriNet model, TimedAutomaton token,
			TimedAutomaton lock) {
		for(TAPNTransition transition : model.getTransitions()){
			boolean changeSymbol = false;
			HashSet<Arc> usedFromPostset= new HashSet<Arc>();
			
			for(Arc presetArc : transition.getPreset()){
				for(Arc postsetArc : transition.getPostset()){
					String sourceName = presetArc.getSource().getName();
					String targetName = postsetArc.getTarget().getName();

					if(isPartOfLockTemplate(sourceName)){
						if(isPartOfLockTemplate(targetName) && !usedFromPostset.contains(postsetArc)){
							String update = "";

//							if(sourceName.equals(PLOCK)){
//								update = "lock = 1";
//							}else if(targetName.equals(PLOCK)){
//								update = "lock = 0";
//							}

							Edge e = new Edge(getLocationByName(sourceName),
									getLocationByName(targetName),
									"",
									createSyncExpression(transition, '!'),
									update);
							lock.addTransition(e);
							usedFromPostset.add(postsetArc);
							break;
						}
					}else{
						if(!isPartOfLockTemplate(targetName)){
							if(isMatchingArcs(presetArc, postsetArc) && !usedFromPostset.contains(postsetArc)){
								char symbol = '?';
								
								if(transition.isFromOriginalNet()){
									if(changeSymbol){
										symbol = '!';
										changeSymbol = false;
									}else{
										changeSymbol = true;
									}
								}
								
								Edge e = new Edge(getLocationByName(sourceName),
										getLocationByName(targetName),
										createTransitionGuard(((TAPNArc)presetArc).getGuard(), transition.isFromOriginalNet()),
										createSyncExpression(transition, symbol),
										createUpdateExpression((TAPNArc)presetArc));
								token.addTransition(e);
								usedFromPostset.add(postsetArc);
								break;
							}
						}
					}
				}
			}
		}
	}


	private boolean isMatchingArcs(Arc presetArc, Arc postsetArc) {
		if (presetArc instanceof TAPNTransportArc && postsetArc instanceof TAPNTransportArc){
			return presetArc == postsetArc; // they are only matching if its the same arc. Handles case where a degree 2 transition is preserved with two transport arcs
		}
		
		if(presetArc instanceof TAPNTransportArc && !(postsetArc instanceof TAPNTransportArc)) return false;
		
		if(!(presetArc instanceof TAPNTransportArc) && postsetArc instanceof TAPNTransportArc) return false;
		
		return true;
	}


	private void createLocations(TimedArcPetriNet model, TimedAutomaton token,
			TimedAutomaton lock) {

		for(TAPNPlace place : model.getPlaces()){
			if(isPartOfLockTemplate(place.getName())){
				Location l = new Location(place.getName(), "");
				if(place.getInvariant().equals("<=0")){
					l.setCommitted(true);
				}
				lock.addLocation(l);
				addLocationMapping(l.getName(), l);
			}else{
				Location l = new Location(place.getName(), convertInvariant(place.getInvariant()));
				token.addLocation(l);
				addLocationMapping(l.getName(), l);
			}
		}
	}


	@Override
	protected String createGlobalDeclarations(TimedArcPetriNet model) {
		StringBuilder builder = new StringBuilder("const int N = ");
		builder.append(model.getTokens().size()-1);
		builder.append(";\ntypedef scalar[N] pid_t;\n");


		for(int i = 0; i < numberOfInitChannels; i++){
			builder.append("chan c");
			builder.append(i);
			builder.append(";\n");
		}

		builder.append(super.createGlobalDeclarations(model));
		return builder.toString();
	}
	
	private void addSymmetricInitialization(TimedAutomaton ta, TimedArcPetriNet model){
		numberOfInitChannels = 0;
		Location pcapacity = getLocationByName("P_capacity");
		Location plock = getLocationByName("P_lock");
		ta.setName(TokenTAName);
		ta.setParameters("const pid_t id");
		ta.setInitLocation(pcapacity);

		List<Token> tokens = model.getTokens();
		for(int i = 0; i < tokens.size(); i++){
			Token token = tokens.get(i);
			Location destination = getLocationByName(token.getPlace().getName());

			if(destination != pcapacity && destination != plock){
				numberOfInitChannels++;
				Edge e = new Edge(pcapacity, destination, "", "c" + i + "?", "");
				ta.addTransition(e);
			}
		}
	}

	private TimedAutomaton createControlTemplate(TimedArcPetriNet model) {
		TimedAutomaton control = new TimedAutomaton();

		Location lastLocation = new Location("","");
		lastLocation.setCommitted(true);
		control.addLocation(lastLocation);
		control.setInitLocation(lastLocation);

		for(int i = 1; i < numberOfInitChannels; i++){
			Location l = new Location("","");
			l.setCommitted(true);
			control.addLocation(l);

			Edge e = new Edge(lastLocation, l, "", "c" + (i-1) + "!", "");
			control.addTransition(e);

			lastLocation = l;
		}

		Location finish = new Location("finish","");
		control.addLocation(finish);

		Edge e = new Edge(lastLocation, finish, "", "c" + (numberOfInitChannels-1) + "!", "");
		control.addTransition(e);

		control.setName("Control");

		return control;		
	}

	private boolean isPartOfLockTemplate(String name){
		Pattern pattern = Pattern.compile("^(P_(?:[a-zA-Z][a-zA-Z0-9_]*)_(?:(?:[0-9]*_(?:in|out)|check))|P_lock|P_deadlock)$");

		Matcher matcher = pattern.matcher(name);
		return matcher.find();
	}

	
	public UPPAALQuery transformQuery(TAPNQuery tapnQuery) throws Exception {
		QueryVisitor visitor = new OptimizedStandardTranslationQueryVisitor();
		return new StandardUPPAALQuery(visitor.getUppaalQueryFor(tapnQuery));
	}
}
