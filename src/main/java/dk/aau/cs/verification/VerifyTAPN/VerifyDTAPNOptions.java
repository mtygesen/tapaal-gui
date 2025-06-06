package dk.aau.cs.verification.VerifyTAPN;

import com.sun.jna.Platform;

import dk.aau.cs.verification.SMCTraceType;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory;
import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.WorkflowMode;
import pipe.gui.MessengerImpl;
import net.tapaal.gui.petrinet.verification.InclusionPlaces;

import java.io.File;
import java.io.IOException;

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
	private boolean useRawVerification;
	private String rawVerificationOptions;
    private boolean parallel = false;
    private boolean benchmark = false;
    private int benchmarkRuns = 100;
	private boolean isSmc;
    private int numberOfTraces;
    private SMCTraceType smcTraceType;
    private int granularity;
    private boolean maxGranularity = false;

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
		this(extraTokens, traceOption, search, symmetry, gcd, timeDarts, pTrie, false, false, new InclusionPlaces(), WorkflowMode.NOT_WORKFLOW, 0, enableOverApproximation, enableUnderApproximation, approximationDenominator, stubbornReduction, null, partition, colorFixpoint, unfoldNet, useRawVerification, rawVerificationOptions, false, 0, false, QueryCategory.Default, 1, new SMCTraceType(), false, 500, false);
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
			String rawVerificationOptions,
            boolean benchmark,
            int benchmarkRuns,
            boolean parallel,
			QueryCategory queryCategory,
            int numberOfTraces,
            SMCTraceType smcTraceType,
            boolean isSimulate,
            int granularity,
            boolean maxGranularity
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
        this.benchmark = benchmark;
        this.benchmarkRuns = benchmarkRuns;
        this.parallel = parallel;
		this.isSmc = queryCategory == QueryCategory.SMC;
        this.numberOfTraces = numberOfTraces;
        this.smcTraceType = smcTraceType;
        this.isSimulate = isSimulate;
        this.granularity = granularity;
        this.maxGranularity = maxGranularity;

		// we only force unfolding when traces are involved
        if((unfold && trace() != TraceOption.NONE || enableOverApproximation || enableUnderApproximation || isSmc && isSimulate && unfold) && !useRawVerification)
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
	public String toString() {
		StringBuilder result = new StringBuilder();
	
		if (useRawVerification && rawVerificationOptions != null) {
            String rawOptions = benchmark ?
                ("--smc-benchmark " + benchmarkRuns + " ") + rawVerificationOptions :
                rawVerificationOptions;
			return rawVerificationString(rawOptions, traceArg(traceOption));
		}

        result.append(kBoundArg());
        result.append(deadTokenArg());
        result.append(traceArg(traceOption));
        if(unfold && trace() != TraceOption.NONE || enabledOverApproximation || enabledUnderApproximation || isSmc && isSimulate && unfold)
        {
            result.append(writeUnfolded());
            result.append(" --bindings ");
        }
        if(!isSmc) {
            result.append(searchArg(searchOption));
            result.append("--verification-method ");
            result.append(timeDarts ? "1" : "0");
            result.append(' ');
            result.append("--memory-optimization ");
            result.append(pTrie ? "1" : "0");
            if (!useStubbornReduction) {
                result.append(" --disable-partial-order ");
            }
        }
		if(workflow == WorkflowMode.WORKFLOW_SOUNDNESS){
			result.append(" --workflow 1 ");
		} else if(workflow == WorkflowMode.WORKFLOW_STRONG_SOUNDNESS){
			result.append(" --workflow 2 ");
			result.append(" --strong-workflow-bound ");
			result.append(workflowbound);
		}
		result.append(' ');

		if (workflow != WorkflowMode.WORKFLOW_SOUNDNESS && workflow != WorkflowMode.WORKFLOW_STRONG_SOUNDNESS) {
			result.append(gcd ? " --gcd-lower " : ""); // GCD optimization is not sound for workflow analysis
		}

        if (isSmc) {
            result.append(parallel ? "--smc-parallel " : "");
            result.append(benchmark ? "--smc-benchmark " + benchmarkRuns + " " : "");
            result.append("--smc-obs-scale " + (maxGranularity ? 0 : granularity) + " ");

            result.append("--smc-print-cumulative-stats 4 ");
            if (isSimulate) {
                result.append(" --smc-traces ");
                result.append(numberOfTraces);
                result.append(" ");
                result.append(smcTraceType.getArg());
            }
        }

		return result.toString();
	}

	private String writeUnfolded() {
		if (Platform.isWindows()) {
			return " --write-unfolded-queries " + "\"" + unfoldedQueriesPath + "\"" + " --write-unfolded-net " + "\"" + unfoldedModelPath + "\"";
		}

		return " --write-unfolded-queries " + unfoldedQueriesPath + " --write-unfolded-net " + unfoldedModelPath + ' ';
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
