package dk.aau.cs.verification.VerifyTAPN;

import java.util.HashMap;
import java.util.Map;

import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;
import net.tapaal.gui.petrinet.verification.InclusionPlaces;
import net.tapaal.gui.petrinet.verification.InclusionPlaces.InclusionPlacesOption;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.util.Require;
import dk.aau.cs.verification.VerificationOptions;

public class VerifyTAPNOptions extends VerificationOptions{

	protected int tokensInModel;
	private final boolean symmetry;
	private final boolean discreteInclusion;
    private final boolean tarOption;
	private InclusionPlaces inclusionPlaces;
	
	//only used for boundedness analysis
	private final boolean dontUseDeadPlaces = false;

	private static final Map<TraceOption, String> traceMap = createTraceOptionsMap();
	private static final Map<SearchOption, String> searchMap = createSearchOptionsMap();
	
	public VerifyTAPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean symmetry, boolean useStateequationCheck, boolean discreteInclusion, boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator) {
		this(extraTokens,traceOption, search, symmetry, useStateequationCheck, discreteInclusion, new InclusionPlaces(), enableOverApproximation, enableUnderApproximation, approximationDenominator);
	}

    public VerifyTAPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean symmetry, boolean useStateequationCheck, boolean discreteInclusion, InclusionPlaces inclusionPlaces, boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator) {
        this(extraTokens,traceOption, search, symmetry, useStateequationCheck, discreteInclusion, new InclusionPlaces(), enableOverApproximation, enableUnderApproximation, approximationDenominator, false);
    }
	
	public VerifyTAPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean symmetry, boolean useStateEquationCheck, boolean discreteInclusion, InclusionPlaces inclusionPlaces, boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator, boolean tarOption) {
		this.extraTokens = extraTokens;
		this.traceOption = traceOption;
		searchOption = search;
		this.symmetry = symmetry;
		this.discreteInclusion = discreteInclusion;
		this.useStateequationCheck = useStateEquationCheck;
		this.inclusionPlaces = inclusionPlaces;
		this.enabledOverApproximation = enableOverApproximation;
		this.enabledUnderApproximation = enableUnderApproximation;
		this.approximationDenominator = approximationDenominator;
        this.tarOption = tarOption;
	}

	public TraceOption trace() {
		return traceOption;
	}
	
	public boolean symmetry() {
		return symmetry;
	}
	
	public boolean discreteInclusion(){
		return discreteInclusion;
	}
	
	public void setTokensInModel(int tokens){ // TODO: Get rid of this method when verifytapn refactored
		tokensInModel = tokens;
	}

    public String kBoundArg() {
        return " --k-bound " + (extraTokens+tokensInModel) + " ";
    }

    public String deadTokenArg() {
        return dontUseDeadPlaces ? " --keep-dead-tokens " : "";
    }

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append(kBoundArg());
        result.append(deadTokenArg());
		result.append(traceMap.get(traceOption));
		result.append(' ');
		result.append(searchMap.get(searchOption));
		result.append(' ');
		result.append(symmetry ? "" : "--disable-symmetry"); // symmetry is on by default in verifyTAPN so "-s" disables it
		result.append(' ');
		result.append(discreteInclusion ? " --factory 1" : "");
		result.append(discreteInclusion ? " --inc-places " + generateDiscretePlacesList() : "");
		return result.toString();
	}

	private String generateDiscretePlacesList() {
		if(inclusionPlaces.inclusionOption() == InclusionPlacesOption.AllPlaces) return "*ALL*";
		if(inclusionPlaces.inclusionPlaces().isEmpty()) return "*NONE*";
		
		StringBuilder s = new StringBuilder();
		boolean first = true;
		for(TimedPlace p : inclusionPlaces.inclusionPlaces()) {
			if(!first) s.append(',');
			
			s.append(p.name());
			if(first) first = false;
		}
		
		return s.toString();
	}

	public static Map<TraceOption, String> createTraceOptionsMap() {
		HashMap<TraceOption, String> map = new HashMap<TraceOption, String>();
		map.put(TraceOption.SOME, "--trace 1 --xml-trace");
		map.put(TraceOption.FASTEST, "--trace 2 --xml-trace");
		map.put(TraceOption.NONE, "");

		return map;
	}

	private static Map<SearchOption, String> createSearchOptionsMap() {
		HashMap<SearchOption, String> map = new HashMap<SearchOption, String>();
		map.put(SearchOption.BFS, "--search-type 0");
		map.put(SearchOption.DFS, "--search-type 1");
		map.put(SearchOption.RANDOM, "--search-type 2");
		map.put(SearchOption.HEURISTIC, "--search-type 3");
		map.put(SearchOption.DEFAULT, "");
		return map;
	}

	public InclusionPlaces inclusionPlaces() {
		return inclusionPlaces;
	}
	
	public void setInclusionPlaces(InclusionPlaces inclusionPlaces) {
		Require.that(inclusionPlaces != null, "Inclusion places cannot be null");
		
		this.inclusionPlaces = inclusionPlaces;
	}

}
