package dk.aau.cs.verification.VerifyTAPN;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sun.jna.Platform;

import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryReductionTime;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.AlgorithmOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory;
import pipe.gui.MessengerImpl;
import net.tapaal.gui.petrinet.verification.InclusionPlaces;

public class VerifyPNOptions extends VerifyTAPNOptions{
	private static final Map<TraceOption, String> traceMap = Map.of(
        TraceOption.SOME, " --trace",
		TraceOption.NONE, ""
    );
	private static final Map<SearchOption, String> searchMap = Map.of(
        SearchOption.HEURISTIC, " --search-strategy BestFS",
        SearchOption.RANDOMHEURISTIC, " --search-strategy RPFS",
        SearchOption.BFS, "--search-strategy BFS",
        SearchOption.DFS, " --search-strategy DFS",
        SearchOption.RANDOM, " --search-strategy RDFS",
        SearchOption.OVERAPPROXIMATE, " --search-strategy OverApprox"

    );

	private final ModelReduction modelReduction;
	private final QueryCategory queryCategory;
	private final AlgorithmOption algorithmOption;
	private final boolean useSiphontrap;
	private final QueryReductionTime queryReductionTime;
	private final boolean useStubbornReduction;
	private final boolean unfold;
	private final boolean colored;
	private final boolean useTarOption;
	private final boolean partition;
	private final boolean colorFixpoint;
    private final boolean symmetricVars;
    private final boolean useTarjan;
    private final boolean useColoredReduction;
    private boolean useRawVerification;
    private String rawVerificationOptions;

    public VerifyPNOptions(
        int extraTokens,
        TraceOption traceOption,
        SearchOption search,
        boolean useOverApproximation,
        ModelReduction modelReduction,
        boolean enableOverApproximation,
        boolean enableUnderApproximation,
        int approximationDenominator,
        QueryCategory queryCategory,
        AlgorithmOption algorithmOption,
        boolean siphontrap,
        QueryReductionTime queryReduction,
        boolean stubbornReduction,
        String pathToReducedNet,
        boolean useTarOption,
        boolean useTarjan,
        boolean colored,
        boolean unfold,
        boolean partition,
        boolean colorFixpoint,
        boolean useSymmetricVars,
        boolean useColoredReduction,
        boolean useRawVerification,
        String rawVerificationOptions
    ) {
		super(extraTokens, traceOption, search, true, useOverApproximation, false, new InclusionPlaces(), enableOverApproximation, enableUnderApproximation, approximationDenominator, useTarOption);

        this.modelReduction = modelReduction;
		this.queryCategory = queryCategory;
		this.algorithmOption = algorithmOption;
		this.useSiphontrap = siphontrap;
		this.queryReductionTime = queryReduction;
		this.useStubbornReduction = stubbornReduction;
		this.unfold = unfold;
		this.colored = colored;
        this.partition = partition;
        this.colorFixpoint = colorFixpoint;
        this.useTarOption = useTarOption;
        this.useTarjan = useTarjan;
		this.reducedModelPath = pathToReducedNet;
		this.symmetricVars = useSymmetricVars;
		this.useColoredReduction = useColoredReduction;
        this.useRawVerification = useRawVerification;
        this.rawVerificationOptions = rawVerificationOptions;

        if (unfold && !useRawVerification) {
            try {
                if (Platform.isWindows()) {
                    unfoldedModelPath = "\"" + File.createTempFile("unfolded-", ".pnml").getAbsolutePath() + "\"";
                    unfoldedQueriesPath = "\"" + File.createTempFile("unfoldedQueries-", ".xml").getAbsolutePath() + "\"";
                } else {
                    unfoldedModelPath = File.createTempFile("unfolded-", ".pnml").getAbsolutePath();
                    unfoldedQueriesPath = File.createTempFile("unfoldedQueries-", ".xml").getAbsolutePath();
                }
            } catch (IOException e) {
                new MessengerImpl().displayErrorMessage(e.getMessage(), "Error");
            }
        }
	}

    public VerifyPNOptions(
        int extraTokens,
        TraceOption traceOption,
        SearchOption search,
        boolean useOverApproximation,
        ModelReduction modelReduction,
        boolean enableOverApproximation,
        boolean enableUnderApproximation,
        int approximationDenominator,
        QueryCategory queryCategory,
        AlgorithmOption algorithmOption,
        boolean siphontrap,
        QueryReductionTime queryReduction,
        boolean stubbornReduction,
        String pathToReducedNet,
        boolean useTarOption,
        boolean useTarjan,
        boolean colored,
        boolean unfold,
        boolean partition,
        boolean colorFixpoint,
        boolean useSymmetricVars
    ) {
        this(extraTokens, traceOption, search, useOverApproximation, modelReduction, enableOverApproximation, enableUnderApproximation, approximationDenominator,queryCategory, algorithmOption, siphontrap, queryReduction, stubbornReduction, pathToReducedNet, useTarOption, useTarjan, colored, false, partition, colorFixpoint, useSymmetricVars, false, true, null);
    }

    public VerifyPNOptions(
        int extraTokens,
        TraceOption traceOption,
        SearchOption search,
        boolean useOverApproximation,
        ModelReduction modelReduction,
        boolean enableOverApproximation,
        boolean enableUnderApproximation,
        int approximationDenominator,
        QueryCategory queryCategory,
        AlgorithmOption algorithmOption,
        boolean siphontrap,
        QueryReductionTime queryReduction,
        boolean stubbornReduction,
        String pathToReducedNet,
        boolean useTarOption,
        boolean useTarjan,
        boolean colored,
        boolean unfold,
        boolean partition,
        boolean colorFixpoint,
        boolean useSymmetricVars,
        boolean useRawVerification,
        String rawVerificationOptions
    ) {
        this(extraTokens, traceOption, search, useOverApproximation, modelReduction, enableOverApproximation, enableUnderApproximation, approximationDenominator,queryCategory, algorithmOption, siphontrap, queryReduction, stubbornReduction, pathToReducedNet, useTarOption, useTarjan, colored, false, partition, colorFixpoint, useSymmetricVars, false, useRawVerification, rawVerificationOptions);
    }

    public VerifyPNOptions(
        int extraTokens,
        TraceOption traceOption,
        SearchOption search,
        boolean useOverApproximation,
        ModelReduction modelReduction,
        boolean enableOverApproximation,
        boolean enableUnderApproximation,
        int approximationDenominator,
        QueryCategory queryCategory,
        AlgorithmOption algorithmOption,
        boolean siphontrap,
        QueryReductionTime queryReduction,
        boolean stubbornReduction,
        String pathToReducedNet,
        boolean useTarOption,
        boolean useTarjan,
        boolean colored,
        boolean partition,
        boolean colorFixpoint,
        boolean useSymmetricVars
    ) {
        this(extraTokens, traceOption, search, useOverApproximation, modelReduction, enableOverApproximation, enableUnderApproximation, approximationDenominator,queryCategory, algorithmOption, siphontrap, queryReduction, stubbornReduction, pathToReducedNet, useTarOption, useTarjan, colored, false, partition, colorFixpoint, useSymmetricVars);
    }

    @Override
    public List<String> getOptions() {
        options.clear();

        if (useRawVerification && rawVerificationOptions != null) {
            add(rawVerificationString(rawVerificationOptions, traceMap.get(traceOption)));
            System.out.println("dudu");
            System.out.println(String.join(" ", options));
            return options;
        }

        add("--k-bound", Integer.toString(kBound()));
        add(traceMap.get(traceOption));
        add(searchMap.get(searchOption).split(" "));

        switch (getModelReduction()) {
            case AGGRESSIVE:
                add("--reduction", "1");
                if (reducedModelPath != null && !reducedModelPath.isEmpty()){
                    add("--write-reduced", reducedModelPath);
                }
                break;
            case NO_REDUCTION:
                add("--reduction", "0");
                break;
            case BOUNDPRESERVING:
                add("--reduction", "2");
                if (reducedModelPath != null && !reducedModelPath.isEmpty()){
                    add("--write-reduced", reducedModelPath);
                }
                break;
            default:
                break;
        }

        if (unfold) {
            add("--write-unfolded-net", unfoldedModelPath);
            add("--write-unfolded-queries", unfoldedQueriesPath);
            add("--bindings");
        }

        if (queryCategory == QueryCategory.CTL){
            add("--ctl-algorithm", getAlgorithmOption() == AlgorithmOption.CERTAIN_ZERO ? "czero" : "local");
            add("--xml-queries", "1");
        } else if (queryCategory == QueryCategory.LTL || queryCategory == QueryCategory.HyperLTL) {
            add("--ltl-algorithm", useTarjan ? "" : "ndfs");
            add("--xml-queries", "1");
        }

        if (useSiphontrap) {
            add("--siphon-trap", "10");
        }

        if (queryReductionTime == QueryReductionTime.NoTime) {
            add("--query-reduction", "0");
        } else if (queryReductionTime == QueryReductionTime.ShortestTime) {
            add("--query-reduction", "1");
        }

        if (!useStubbornReduction) {
            add("--disable-partial-order");
        }

        if (useTarOption) {
            add("--trace-abstraction");
        }

        if (colored) {
            if (!partition) {
                add("--disable-partitioning");
            }
            if (!colorFixpoint) {
                add("--disable-cfp");
            }
            if (!symmetricVars) {
                add("--disable-symmetry-vars");
            }
        }

        if (!useColoredReduction) {
            add("--col-reduction", "0");
        }

        return options;
    }
	
	public ModelReduction getModelReduction(){
		return modelReduction;
	}
	
	public AlgorithmOption getAlgorithmOption(){
		return algorithmOption;
	}
}
