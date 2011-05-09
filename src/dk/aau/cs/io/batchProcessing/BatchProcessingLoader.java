package dk.aau.cs.io.batchProcessing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.HashTableSize;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.Parsing.TAPAALQueryParser;
import dk.aau.cs.TCTL.visitors.VerifyPlaceNamesVisitor;
import dk.aau.cs.gui.NameGenerator;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantStore;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedMarking;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;

public class BatchProcessingLoader {
	private HashMap<Tuple<TimedTransition, Integer>, TimedPlace> presetArcs;
	private HashMap<Tuple<TimedTransition, Integer>, TimedPlace> postsetArcs;
	private HashMap<String, String> placeIDToName;
	private HashMap<String, String> transitionIDToName;
	private HashMap<Tuple<TimedTransition, Integer>, TimeInterval> transportArcsTimeIntervals;

	private NameGenerator nameGenerator = new NameGenerator();

	public BatchProcessingLoader() {
		presetArcs = new HashMap<Tuple<TimedTransition,Integer>, TimedPlace>();
		postsetArcs = new HashMap<Tuple<TimedTransition,Integer>, TimedPlace>();
		placeIDToName = new HashMap<String, String>();
		transitionIDToName = new HashMap<String, String>();
		transportArcsTimeIntervals = new HashMap<Tuple<TimedTransition,Integer>, TimeInterval>();
	}

	public LoadedBatchProcessingModel load(File file) throws FormatException {
		Require.that(file != null && file.exists(), "file must be non-null and exist");

		Document doc = loadDocument(file);
		if(doc == null) return null;
		return parse(doc);
	}

	private Document loadDocument(File file) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(file);
		} catch (ParserConfigurationException e) {
			return null;
		} catch (SAXException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	private LoadedBatchProcessingModel parse(Document doc) throws FormatException {
		Map<String, Constant> constants = parseConstants(doc);

		TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork(new ConstantStore(constants.values()));

		parseSharedPlaces(doc, network, constants);
		parseSharedTransitions(doc, network);
		
		parseTemplates(doc, network, constants);
		Collection<TAPNQuery> queries = parseQueries(doc, network);

		network.buildConstraints();
		return new LoadedBatchProcessingModel(network, queries);
	}


	private void parseSharedPlaces(Document doc, TimedArcPetriNetNetwork network, Map<String, Constant> constants) {
		NodeList sharedPlaceNodes = doc.getElementsByTagName("shared-place");

		for(int i = 0; i < sharedPlaceNodes.getLength(); i++){
			Node node = sharedPlaceNodes.item(i);

			if(node instanceof Element){
				SharedPlace place = parseSharedPlace((Element)node, network.marking(), constants);
				network.add(place);
			}
		}
	}

	private SharedPlace parseSharedPlace(Element element, TimedMarking marking, Map<String, Constant> constants) {
		String name = element.getAttribute("name");
		TimeInvariant invariant = TimeInvariant.parse(element.getAttribute("invariant"), constants);
		int numberOfTokens = Integer.parseInt(element.getAttribute("initialMarking"));

		SharedPlace place = new SharedPlace(name, invariant);
		place.setCurrentMarking(marking);
		for(int j = 0; j < numberOfTokens; j++){
			marking.add(new TimedToken(place));
		}
		return place;
	}

	private void parseSharedTransitions(Document doc, TimedArcPetriNetNetwork network) {
		NodeList sharedTransitionNodes = doc.getElementsByTagName("shared-transition");

		for(int i = 0; i < sharedTransitionNodes.getLength(); i++){
			Node node = sharedTransitionNodes.item(i);

			if(node instanceof Element){
				SharedTransition transition = parseSharedTransition((Element)node);
				network.add(transition);
			}
		}
	}

	private SharedTransition parseSharedTransition(Element element) {
		String name = element.getAttribute("name");
		
		return new SharedTransition(name);
	}

	private Collection<TAPNQuery> parseQueries(Document doc, TimedArcPetriNetNetwork network) {
		Collection<TAPNQuery> queries = new ArrayList<TAPNQuery>();
		NodeList queryNodes = doc.getElementsByTagName("query");
		
		ArrayList<Tuple<String, String>> templatePlaceNames = getPlaceNames(network);
		for (int i = 0; i < queryNodes.getLength(); i++) {
			Node q = queryNodes.item(i);

			if (q instanceof Element) {
				TAPNQuery query = parseTAPNQuery((Element) q);
				
				if (query != null) {
					if(!doesPlacesUsedInQueryExist(query, templatePlaceNames)) {
						continue;
					}

					queries.add(query);
				}
			}
		}
		
		return queries;
	}

	private boolean doesPlacesUsedInQueryExist(TAPNQuery query, ArrayList<Tuple<String, String>> templatePlaceNames) {
		VerifyPlaceNamesVisitor nameChecker = new VerifyPlaceNamesVisitor(templatePlaceNames);

		VerifyPlaceNamesVisitor.Context c = nameChecker.VerifyPlaceNames(query.getProperty());
		
		return c.getResult();
		
	}

	private ArrayList<Tuple<String, String>> getPlaceNames(TimedArcPetriNetNetwork network) {
		ArrayList<Tuple<String,String>> templatePlaceNames = new ArrayList<Tuple<String,String>>();
		for(TimedArcPetriNet tapn : network.allTemplates()) {
			for(TimedPlace p : tapn.places()) {
				templatePlaceNames.add(new Tuple<String, String>(tapn.name(), p.name()));
			}
		}
		
		for(TimedPlace p : network.sharedPlaces()) {
			templatePlaceNames.add(new Tuple<String, String>("", p.name()));
		}
		return templatePlaceNames;
	}

	private void parseTemplates(Document doc, TimedArcPetriNetNetwork network, Map<String, Constant> constants) throws FormatException {
		NodeList nets = doc.getElementsByTagName("net");
		
		if(nets.getLength() <= 0)
			throw new FormatException("File did not contain any TAPN components.");
		
		for (int i = 0; i < nets.getLength(); i++) {
			parseTimedArcPetriNet(nets.item(i), network, constants);
		}
	}

	private Map<String, Constant> parseConstants(Document doc) {
		TreeMap<String, Constant> constants = new TreeMap<String, Constant>();
		NodeList constantNodes = doc.getElementsByTagName("constant");
		for (int i = 0; i < constantNodes.getLength(); i++) {
			Node c = constantNodes.item(i);

			if (c instanceof Element) {
				Constant constant = parseAndAddConstant((Element) c);
				constants.put(constant.name(), constant);
			}
		}
		return constants;
	}

	private void parseTimedArcPetriNet(Node tapnNode, TimedArcPetriNetNetwork network, Map<String, Constant> constants) throws FormatException {
		String name = getTAPNName(tapnNode);
		boolean active = getActiveStatus(tapnNode);

		TimedArcPetriNet tapn = new TimedArcPetriNet(name);
		tapn.setActive(active);
		network.add(tapn);
		nameGenerator.add(tapn);

		NodeList nodeList = tapnNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if(node instanceof Element){
				parseElement((Element)node, tapn, network, constants);
			}
		}
	}
	
	private boolean getActiveStatus(Node tapnNode) {
		if (tapnNode instanceof Element) {
			Element element = (Element)tapnNode;
			String activeString = element.getAttribute("active");
			
			if (activeString == null || activeString.equals(""))
				return true;
			else
				return activeString.equals("true");
		} else {
			return true;
		}
	}

	private void parseElement(Element element, TimedArcPetriNet tapn, TimedArcPetriNetNetwork network, Map<String, Constant> constants) throws FormatException {
		if ("place".equals(element.getNodeName())) {
			parsePlace(element, network, tapn, constants);
		} else if ("transition".equals(element.getNodeName())) {
			parseTransition(element, network, tapn);
		} else if ("arc".equals(element.getNodeName())) {
			parseAndAddArc(element, tapn, constants);
		}
	}

	private boolean isNameAllowed(String name) {
		Require.that(name != null, "name was null");

		return !name.isEmpty() && java.util.regex.Pattern.matches("[a-zA-Z]([_a-zA-Z0-9])*", name);
	}


	private String getTAPNName(Node tapnNode) {
		if (tapnNode instanceof Element) {
			Element element = (Element)tapnNode;
			String name = element.getAttribute("name");

			if (name == null || name.equals(""))
				name = element.getAttribute("id");

			if(!isNameAllowed(name)){
				name = nameGenerator.getNewTemplateName();
			}
			nameGenerator.updateTemplateIndex(name);
			return name;
		} else {
			return nameGenerator.getNewTemplateName();
		}
	}

	private void parseTransition(Element transition, TimedArcPetriNetNetwork network, TimedArcPetriNet tapn) {
		String idInput = transition.getAttribute("id");
		String nameInput = transition.getAttribute("name");
	
		if (idInput.length() == 0 && nameInput.length() > 0) {
			idInput = nameInput;
		}

		if (nameInput.length() == 0 && idInput.length() > 0) {
			nameInput = idInput;
		}
		
		TimedTransition t = new TimedTransition(nameInput);
		if(network.isNameUsedForShared(nameInput)){
			t.setName(nameGenerator.getNewTransitionName(tapn)); // introduce temporary name to avoid exceptions
			tapn.add(t);
			if(!transitionIDToName.containsKey(idInput))
				transitionIDToName.put(idInput, nameInput);
			network.getSharedTransitionByName(nameInput).makeShared(t);
		}else{
			tapn.add(t);
			transitionIDToName.put(idInput, nameInput);
		}
		nameGenerator.updateIndicesForAllModels(nameInput);
	}

	private void parsePlace(Element place, TimedArcPetriNetNetwork network, TimedArcPetriNet tapn, Map<String, Constant> constants) {
		String idInput = place.getAttribute("id");
		String nameInput = place.getAttribute("name");
		int initialMarkingInput = Integer.parseInt(place.getAttribute("initialMarking"));
		String invariant = place.getAttribute("invariant");

		if (idInput.length() == 0 && nameInput.length() > 0) {
			idInput = nameInput;
		}

		if (nameInput.length() == 0 && idInput.length() > 0) {
			nameInput = idInput;
		}

		TimedPlace p;
		if(network.isNameUsedForShared(nameInput)){
			p = network.getSharedPlaceByName(nameInput);
			if(!placeIDToName.containsKey(idInput))
				placeIDToName.put(idInput, nameInput);
			tapn.add(p);
		}else{
			p = new LocalTimedPlace(nameInput, TimeInvariant.parse(invariant, constants));
			tapn.add(p);
			placeIDToName.put(idInput, nameInput);
			for (int i = 0; i < initialMarkingInput; i++) {
				network.marking().add(new TimedToken(p));
			}
		}
		nameGenerator.updateIndicesForAllModels(nameInput);
	}

	private void parseAndAddArc(Element arc, TimedArcPetriNet tapn, Map<String, Constant> constants) throws FormatException {
		String sourceId = arc.getAttribute("source");
		String targetId = arc.getAttribute("target");
		String inscription = arc.getAttribute("inscription");
		String type = arc.getAttribute("type");

		if (type.equals("tapnInhibitor"))
			parseAndAddTimedInhibitorArc(sourceId, targetId, inscription, tapn, constants);
		else if (type.equals("timed"))
				parseAndAddTimedInputArc(sourceId, targetId, inscription, tapn, constants);
		else if (type.equals("transport"))
			parseAndAddTransportArc(sourceId, targetId, inscription, tapn, constants);
		else
			parseAndAddTimedOutputArc(sourceId, targetId, inscription, tapn);
	}

	private void parseAndAddTimedOutputArc(String sourceId, String targetId, String inscription, TimedArcPetriNet tapn) throws FormatException {
		TimedTransition transition = tapn.getTransitionByName(transitionIDToName.get(sourceId));
		TimedPlace place = tapn.getPlaceByName(placeIDToName.get(targetId));

		TimedOutputArc outputArc = new TimedOutputArc(transition, place);
		
		if(tapn.hasArcFromTransitionToPlace(outputArc.source(),outputArc.destination())) {
			throw new FormatException("Multiple arcs between a place and a transition is not allowed");
		}

		tapn.add(outputArc);
	}

	private void parseAndAddTransportArc(String sourceId, String targetId,	String inscription, TimedArcPetriNet tapn, Map<String, Constant> constants) {
		String[] inscriptionSplit = {};
		if (inscription.contains(":")) {
			inscriptionSplit = inscription.split(":");
		}
		boolean isInPreSet = false;
		TimedPlace sourcePlace = null;
		if(placeIDToName.containsKey(sourceId))
			sourcePlace = tapn.getPlaceByName(placeIDToName.get(sourceId));
		
		if (sourcePlace != null) {
			isInPreSet = true;
		}
		
		if (isInPreSet) {
			TimedTransition transition = tapn.getTransitionByName(transitionIDToName.get(targetId));
			Tuple<TimedTransition, Integer> hashKey = new Tuple<TimedTransition, Integer>(transition, Integer.parseInt(inscriptionSplit[1]));
			
			if (postsetArcs.containsKey(hashKey)) {
				TimedPlace destPlace = postsetArcs.get(hashKey);
				TimeInterval interval = TimeInterval.parse(inscriptionSplit[0],	constants);

				assert (sourcePlace != null);
				assert (transition != null);
				assert (destPlace != null);

				TransportArc transArc = new TransportArc(sourcePlace, transition, destPlace, interval);
				tapn.add(transArc);

				postsetArcs.remove(hashKey);
			} else {
				presetArcs.put(hashKey, sourcePlace);
				transportArcsTimeIntervals.put(hashKey, TimeInterval.parse(inscriptionSplit[0], constants));
			}
		} else {
			TimedTransition trans = tapn.getTransitionByName(transitionIDToName.get(sourceId));
			TimedPlace destPlace = tapn.getPlaceByName(placeIDToName.get(targetId));
			Tuple<TimedTransition, Integer> hashKey = new Tuple<TimedTransition, Integer>(trans,  Integer.parseInt(inscriptionSplit[1]));
			
			if (presetArcs.containsKey(hashKey)) {
				sourcePlace = presetArcs.get(hashKey);
				TimeInterval interval = transportArcsTimeIntervals.get(hashKey);

				assert (sourcePlace != null);
				assert (trans != null);
				assert (destPlace != null);

				TransportArc transArc = new TransportArc(sourcePlace, trans, destPlace, interval);
				tapn.add(transArc);

				presetArcs.remove(hashKey);
				transportArcsTimeIntervals.remove(hashKey);
			} else {
				postsetArcs.put(hashKey, destPlace);
			}
		}
	}

	private void parseAndAddTimedInputArc(String sourceId, String targetId, String inscription, TimedArcPetriNet tapn, Map<String, Constant> constants) throws FormatException {
		TimedPlace place = tapn.getPlaceByName(placeIDToName.get(sourceId));
		TimedTransition transition = tapn.getTransitionByName(transitionIDToName.get(targetId));
		TimeInterval interval = TimeInterval.parse(inscription, constants);

		TimedInputArc inputArc = new TimedInputArc(place, transition, interval);

		if(tapn.hasArcFromPlaceToTransition(inputArc.source(), inputArc.destination())) {
			throw new FormatException("Multiple arcs between a place and a transition is not allowed");
		}

		tapn.add(inputArc);
	}

	private void parseAndAddTimedInhibitorArc(String sourceId, String targetId, String inscription, TimedArcPetriNet tapn, Map<String, Constant> constants) {
		TimedPlace place = tapn.getPlaceByName(placeIDToName.get(sourceId));
		TimedTransition transition = tapn.getTransitionByName(transitionIDToName.get(targetId));
		
		TimedInhibitorArc inhibArc = new TimedInhibitorArc(place, transition, TimeInterval.ZERO_INF);
		tapn.add(inhibArc);
	}

	private TAPNQuery parseTAPNQuery(Element queryElement) {
		String comment = getQueryComment(queryElement);
		TraceOption traceOption = getQueryTraceOption(queryElement);
		SearchOption searchOption = getQuerySearchOption(queryElement);
		HashTableSize hashTableSize = getQueryHashTableSize(queryElement);
		ExtrapolationOption extrapolationOption = getQueryExtrapolationOption(queryElement);
		ReductionOption reductionOption = getQueryReductionOption(queryElement);
		int capacity = Integer.parseInt(queryElement.getAttribute("capacity"));
		boolean symmetry = getSymmetryReductionOption(queryElement);
		boolean active = getActiveStatus(queryElement);
		
		TCTLAbstractProperty query;
		query = parseQueryProperty(queryElement.getAttribute("query"));

		if (query != null) {
			TAPNQuery parsedQuery = new TAPNQuery(comment, capacity, query, traceOption,
					searchOption, reductionOption, symmetry, hashTableSize, extrapolationOption);
			parsedQuery.setActive(active);
			return parsedQuery;
		} else
			return null;
	}
	
	private boolean getSymmetryReductionOption(Element queryElement) {
		boolean symmetry;
		try {
			symmetry = queryElement.getAttribute("symmetry").equals("true");
		} catch(Exception e) {
			symmetry = true;
		}
		return symmetry;	
	}

	private TCTLAbstractProperty parseQueryProperty(String queryToParse) {
		TCTLAbstractProperty query = null;
		TAPAALQueryParser queryParser = new TAPAALQueryParser();

		try {
			query = queryParser.parse(queryToParse);
		} catch (Exception e) {
			System.err.println("No query was specified: " + e.getStackTrace());
		}
		return query;
	}

	private ReductionOption getQueryReductionOption(Element queryElement) {
		ReductionOption reductionOption;
		try {
			reductionOption = ReductionOption.valueOf(queryElement.getAttribute("reductionOption"));
		} catch (Exception e) {
			reductionOption = ReductionOption.STANDARD;
		}
		return reductionOption;
	}

	private ExtrapolationOption getQueryExtrapolationOption(Element queryElement) {
		ExtrapolationOption extrapolationOption;
		try {
			extrapolationOption = ExtrapolationOption.valueOf(queryElement.getAttribute("extrapolationOption"));
		} catch (Exception e) {
			extrapolationOption = ExtrapolationOption.AUTOMATIC;
		}
		return extrapolationOption;
	}

	private HashTableSize getQueryHashTableSize(Element queryElement) {
		HashTableSize hashTableSize;
		try {
			hashTableSize = HashTableSize.valueOf(queryElement.getAttribute("hashTableSize"));
		} catch (Exception e) {
			hashTableSize = HashTableSize.MB_16;
		}
		return hashTableSize;
	}

	private SearchOption getQuerySearchOption(Element queryElement) {
		SearchOption searchOption;
		try {
			searchOption = SearchOption.valueOf(queryElement.getAttribute("searchOption"));
		} catch (Exception e) {
			searchOption = SearchOption.BFS;
		}
		return searchOption;
	}

	private TraceOption getQueryTraceOption(Element queryElement) {
		TraceOption traceOption;
		try {
			traceOption = TraceOption.valueOf(queryElement.getAttribute("traceOption"));
		} catch (Exception e) {
			traceOption = TraceOption.NONE;
		}
		return traceOption;
	}

	private String getQueryComment(Element queryElement) {
		String comment;
		try {
			comment = queryElement.getAttribute("name");
		} catch (Exception e) {
			comment = "No comment specified";
		}
		return comment;
	}

	private Constant parseAndAddConstant(Element constantElement) {
		String name = constantElement.getAttribute("name");
		int value = Integer.parseInt(constantElement.getAttribute("value"));

		return new Constant(name, value);
	}
}