package dk.aau.cs.verification;

import dk.aau.cs.TCTL.visitors.HasDeadlockVisitor;
import dk.aau.cs.model.tapn.TAPNQuery;

public class QueryResult {
	private final boolean satisfied;
    private final QuantitativeResult quantitativeResult;
	private boolean approximationInconclusive = false;
	private final boolean discreteInclusion;
	private final TAPNQuery query;
	private final BoundednessAnalysisResult boundednessAnalysis;

    public boolean isSolvedUsingQuerySimplification() {
        return solvedUsingQuerySimplification;
    }

    public void setSolvedUsingQuerySimplification(boolean solvedUsingQuerySimplification) {
        this.solvedUsingQuerySimplification = solvedUsingQuerySimplification;
    }

    public boolean isSolvedUsingTraceAbstractRefinement() {
        return solvedUsingTraceAbstractRefinement;
    }

    public void setSolvedUsingTraceAbstractRefinement(boolean solvedUsingTraceAbstractRefinement) {
        this.solvedUsingTraceAbstractRefinement = solvedUsingTraceAbstractRefinement;
    }

    public boolean isSolvedUsingSiphonTrap() {
        return solvedUsingSiphonTrap;
    }

    public void setSolvedUsingSiphonTrap(boolean solvedUsingSiphonTrap) {
        this.solvedUsingSiphonTrap = solvedUsingSiphonTrap;
    }

    private boolean solvedUsingQuerySimplification;
    private boolean solvedUsingTraceAbstractRefinement;
    private boolean solvedUsingSiphonTrap;

	public boolean isCTL = false;
	public QueryResult(boolean satisfied, BoundednessAnalysisResult boundednessAnalysis, TAPNQuery query, boolean discreteInclusion){
		this.satisfied = satisfied;
		this.boundednessAnalysis = boundednessAnalysis;
		this.query = query;
		this.discreteInclusion = discreteInclusion;
        this.quantitativeResult = null;
	}

    public QueryResult(QuantitativeResult quantitativeResult, BoundednessAnalysisResult boundednessAnalysis, TAPNQuery query, boolean discreteInclusion){
        this.satisfied = true;
        this.boundednessAnalysis = boundednessAnalysis;
        this.query = query;
        this.discreteInclusion = discreteInclusion;
        this.quantitativeResult = quantitativeResult;
    }
	
	public boolean isQuerySatisfied() {
		return satisfied;
	}
	
	public boolean isApproximationInconclusive() {
		return approximationInconclusive;
	}
	
	public void setApproximationInconclusive(boolean result) {
		approximationInconclusive = result;
	}
	
	public boolean isDiscreteIncludion() {
		return discreteInclusion;
	}
	
	public boolean hasDeadlock(){
		return new HasDeadlockVisitor().hasDeadLock(query.getProperty());
	}
	
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		if(approximationInconclusive)
			buffer.append(getInconclusiveString());
		else if(this.quantitativeResult == null) {
			buffer.append("Property is ");
			buffer.append(satisfied ? "satisfied." : "not satisfied.");
		} else {
            buffer.append(quantitativeResult);
        }
		if(shouldAddExplanation())
			buffer.append(getExplanationString());
		return buffer.toString();
	}
	
	public QueryType queryType(){
		return query.queryType();
	}
	
	private boolean shouldAddExplanation() {
		return (queryType().equals(QueryType.EF) && !isQuerySatisfied()) 
		|| (queryType().equals(QueryType.EG)) // && !isQuerySatisfied()) 
		|| (queryType().equals(QueryType.AF)) // && isQuerySatisfied())
		|| (queryType().equals(QueryType.AG) && isQuerySatisfied())
        || (queryType().equals(QueryType.A))
        || (queryType().equals(QueryType.E))
        || (isSMC())
		|| (hasDeadlock() && 
				(!isQuerySatisfied() && queryType().equals(QueryType.EF)) || 
				(isQuerySatisfied() && queryType().equals(QueryType.AG))
                || (hasDeadlock() && boundednessAnalysis.isUPPAAL()) );
	}
	
	protected String getExplanationString(){
        if(isSMC()) {
            String timeBound = query.getSMCSettings().timeBound < Integer.MAX_VALUE ?
                String.valueOf(query.getSMCSettings().timeBound) : "&infin;";
            String stepBound = query.getSMCSettings().stepBound < Integer.MAX_VALUE ?
                String.valueOf(query.getSMCSettings().stepBound) : "&infin;";
            return  "<br/>" +
                    "<br/>The engine explored runs satisfying: " +
                    "time " + " &le; " + timeBound + " and " +
                    "steps " + "&le; " + stepBound +
                    ((isQuantitative()) ?
                        "<br/>Confidence level: " + (query.getSMCSettings().confidence * 100) + "%" :
                        (query.getSMCSettings().compareToFloat) ?
                        "<br/>Probability of false positive: " + query.getSMCSettings().falsePositives +
                        "<br/>Probability of false negative: " + query.getSMCSettings().falseNegatives +
                        "<br/>Indifference region: [" + (query.getSMCSettings().geqThan - query.getSMCSettings().indifferenceWidth) +
                        ";" +  (query.getSMCSettings().geqThan + query.getSMCSettings().indifferenceWidth) + "]" : ""
                    );
        } else {
            return boundednessAnalysis.toString();
        }
	}
	
	protected String getInconclusiveString(){
        return "The result of the approximation was inconclusive.";
	}

	public BoundednessAnalysisResult boundednessAnalysis() {
		return boundednessAnalysis;
	}

    public TAPNQuery getQuery() {
        return query;
    }

    public boolean isSMC() {
        return query.getCategory() == net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory.SMC;
    }

    public boolean isQuantitative() {
        return quantitativeResult != null;
    }

    public String getProbabilityString() {
        return quantitativeResult.getProbabilityString();
    }
}
