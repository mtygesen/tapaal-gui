package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.verification.VerificationOptions;
import net.tapaal.gui.petrinet.verification.TAPNQuery;

import java.util.List;

public class VerifyPNUnfoldOptions extends VerificationOptions {
    private final String modelOut;
    private final String queryOut;
    private final int numQueries;
    private final boolean partition;
    private final boolean computeColorFixpoint;
    private final boolean symmetricVars;

    public VerifyPNUnfoldOptions(String modelOut, String queryOut, int numQueries, boolean partition, boolean computeColorFixpoint, boolean useSymmetricVars) {
        this.modelOut = modelOut;
        this.queryOut = queryOut;
        this.numQueries = numQueries;
        this.partition = partition;
        this.computeColorFixpoint = computeColorFixpoint;
        symmetricVars = useSymmetricVars;
    }

    @Override
    public boolean enabledOverApproximation() {
        return false;
    }

    @Override
    public boolean enabledUnderApproximation() {
        return false;
    }

    @Override
    public int approximationDenominator() {
        return 0;
    }

    @Override
    public int extraTokens() {
        return 0;
    }

    @Override
    public TAPNQuery.TraceOption traceOption() {
        return null;
    }

    @Override
    public void setTraceOption(TAPNQuery.TraceOption option) { }

    @Override
    public TAPNQuery.SearchOption searchOption() {
        return null;
    }

    @Override
    public List<String> getOptions() {
        options.clear();

        add("--write-unfolded-queries", queryOut);
        add("--write-unfolded-net", modelOut);
        add("--search-strategy", "OverApprox");
        add("--reduction", "0");
        add("--query-reduction", "0");
        
        String queryIndexList = "1";
        for (int i = 2; i <= numQueries; i++){
            queryIndexList += "," + i;
        }

        add("--xml-queries", queryIndexList);

        if (!partition){
            add("--disable-partitioning");
        }

        if (!computeColorFixpoint){
            add("--disable-cfp");
        }

        if (!symmetricVars){
            add("--disable-symmetry-vars");
        }

        add("--col-reduction", "0");
        add("--bindings");

        return options;
    }
}
