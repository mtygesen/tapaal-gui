package dk.aau.cs.verification.VerifyTAPN;

import com.sun.jna.Platform;
import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.WorkflowMode;
import pipe.gui.MessengerImpl;
import net.tapaal.gui.petrinet.verification.InclusionPlaces;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class VerifyDTAPNOptions extends VerifyTAPNOptions {
	
	private final boolean gcd;
	private final boolean timeDarts;
	private final boolean pTrie;
	private final WorkflowMode workflow;
	private final long workflowbound;
	//only used for boundedness analysis
	private boolean dontUseDeadPlaces = false;
	private boolean useStubbornReduction = true;
	private final boolean partition;
	private final boolean colorFixpoint;
    private final boolean unfold;
	private boolean useRawVerification;
	private String rawVerificationOptions;

	//Only used for boundedness analysis
	public VerifyDTAPNOptions(
			boolean dontUseDeadPlaces,
			int extraTokens,
			TraceOption traceOption,
			SearchOption search,
			boolean symmetry,
            boolean gcd,
			boolean timeDarts,
			boolean pTrie,
			boolean enableOverApproximation,
			boolean enableUnderApproximation,
			int approximationDenominator,
			boolean stubbornReduction,
            boolean partition,
            boolean colorFixpoint,
            boolean unfoldNet,
			boolean useRawVerification,
			String rawVerificationOptions
	) {
		this(extraTokens, traceOption, search, symmetry, gcd, timeDarts, pTrie, false, false, new InclusionPlaces(), WorkflowMode.NOT_WORKFLOW, 0, enableOverApproximation, enableUnderApproximation, approximationDenominator, stubbornReduction, null, partition, colorFixpoint, unfoldNet, useRawVerification, rawVerificationOptions);
		this.dontUseDeadPlaces = dontUseDeadPlaces;
	}

	public VerifyDTAPNOptions(
			int extraTokens,
			TraceOption traceOption,
			SearchOption search,
			boolean symmetry,
			boolean gcd,
			boolean timeDarts,
			boolean pTrie,
			boolean useStateequationCheck,
			boolean discreteInclusion,
			InclusionPlaces inclusionPlaces,
			WorkflowMode workflow,
			long workflowbound,
			boolean enableOverApproximation,
			boolean enableUnderApproximation,
			int approximationDenominator,
			boolean stubbornReduction,
            String reducedModelPath,
            boolean partition,
            boolean colorFixpoint,
            boolean unfoldNet,
			boolean useRawVerification,
			String rawVerificationOptions
	) {
		super(extraTokens, traceOption, search, symmetry, useStateequationCheck, discreteInclusion, inclusionPlaces, enableOverApproximation, enableUnderApproximation, approximationDenominator);
		this.timeDarts = timeDarts;
		this.pTrie = pTrie;
		this.workflow = workflow;
		this.gcd = gcd;
		this.workflowbound = workflowbound;
		this.useStubbornReduction = stubbornReduction;
		this.reducedModelPath = reducedModelPath;
		this.partition = partition;
		this.colorFixpoint = colorFixpoint;
        this.unfold = unfoldNet;
		this.useRawVerification = useRawVerification;
		this.rawVerificationOptions = rawVerificationOptions;

        if(unfold && trace() != TraceOption.NONE && !useRawVerification) // we only force unfolding when traces are involved
        {
            try {
                unfoldedModelPath = File.createTempFile("unfolded-", ".pnml").getAbsolutePath();
                unfoldedQueriesPath = File.createTempFile("unfoldedQueries-", ".xml").getAbsolutePath();
            } catch (IOException e) {
                new MessengerImpl().displayErrorMessage(e.getMessage(), "Error");
            }
        }
	}
	
	@Override
	public List<String> getOptions() {
		options.clear();

		if (useRawVerification && rawVerificationOptions != null) {
			add(rawVerificationString(rawVerificationOptions, traceArg(traceOption)));
			return options;
		}

		add("--k-bound", Integer.toString(kBound()));
		add(deadTokenArg());
		add(traceArg(traceOption).split(" "));

		if (unfold && trace() != TraceOption.NONE) {
			add("--write-unfolded-queries", unfoldedQueriesPath);
			add("--write-unfolded-net", unfoldedModelPath);
			add("--bindings");
		}

		add(searchArg(searchOption).split(" "));
		add("--verification-method", timeDarts ? "1" : "0");
		add("--memory-optimization", pTrie ? "1" : "0");

		if (!useStubbornReduction) {
			add("--disable-partial-order");
		}

		if (workflow == WorkflowMode.WORKFLOW_SOUNDNESS){
			add("--workflow", "1");
		} else if (workflow == WorkflowMode.WORKFLOW_STRONG_SOUNDNESS){
			add("--workflow", "2");
			add("--strong-workflow-bound", Long.toString(workflowbound));
		}

		if (workflow != WorkflowMode.WORKFLOW_SOUNDNESS && workflow != WorkflowMode.WORKFLOW_STRONG_SOUNDNESS) {
			add(gcd ? "--gcd-lower" : ""); // GCD optimization is not sound for workflow analysis
		}

		return options;
	}

	public boolean timeDarts() {
		return timeDarts;
	}
	
	public boolean pTrie() {
		return pTrie;
	}
        
	public WorkflowMode getWorkflowMode(){
		return workflow;
	}

    // TODO make this a proper class member s.t. this can be reused where it makes sense
    public static String traceArg(TraceOption opt) {
        switch (opt)
        {
            case SOME:
                return "--trace 1 ";
            case FASTEST:
                return "--trace 2 ";
            default:
                assert (false);
            case NONE:
                return "--trace 0 ";
        }
    }

    private static String searchArg(SearchOption arg) {
        switch (arg)
        {
            case BFS:
                return "--search-strategy BFS ";
            case DFS:
                return "--search-strategy DFS ";
            case RANDOM:
                return "--search-strategy RDFS ";
            case HEURISTIC:
                return "--search-strategy BestFS ";
            case OVERAPPROXIMATE:
                return "--search-strategy OverApprox ";
            default:
            case BatchProcessingKeepQueryOption:
                assert (false);
            case DEFAULT:
                return "--search-strategy default ";
        }
    }

}
