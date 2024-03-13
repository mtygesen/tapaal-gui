package dk.aau.cs.verification;

import java.util.ArrayList;
import java.util.List;

import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;

public abstract class VerificationOptions {
	protected List<String> options = new ArrayList<>();
	protected SearchOption searchOption;
	protected TraceOption traceOption;
	protected boolean enabledOverApproximation;
    protected boolean enabledUnderApproximation;
	protected int approximationDenominator;
	protected boolean useStateequationCheck;
	protected int extraTokens;

	protected String reducedModelPath;
    protected static String unfoldedModelPath;
    protected static String unfoldedQueriesPath;

	public abstract List<String> getOptions();

	@Override
	public String toString() {
		return String.join(" ", getOptions());
	}

	protected void add(String option) {
		if (option == null || option.isBlank()) {
			return;
		}

		option = option.trim();
		options.add(option);
	}

	protected void add(String... options) {
		for (String option : options) {
			add(option);
		}
	}

	public boolean enabledStateequationsCheck() {
		return useStateequationCheck;
	}

	public boolean enabledOverApproximation() {
		return enabledOverApproximation;
	}

	public boolean enabledUnderApproximation() {
		return enabledUnderApproximation;
	}

	public int approximationDenominator() {
		return approximationDenominator;
	}

	public int extraTokens() {
		return extraTokens;
	}

	public TraceOption traceOption() {
		return traceOption;
	}

	public void setTraceOption(TraceOption option) {
		traceOption = option;
	}

	public SearchOption searchOption() {
		return searchOption;
	}

    public String unfoldedModelPath(){
        return unfoldedModelPath;
    }

    public String unfoldedQueriesPath(){
        return unfoldedQueriesPath;
    }
}
