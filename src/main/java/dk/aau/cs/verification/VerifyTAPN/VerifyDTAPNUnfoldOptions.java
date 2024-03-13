package dk.aau.cs.verification.VerifyTAPN;

import java.util.List;

import dk.aau.cs.verification.VerificationOptions;
import net.tapaal.gui.petrinet.verification.TAPNQuery;

public class VerifyDTAPNUnfoldOptions extends VerificationOptions {
    private final String modelOut;
    private final String queryOut;
    private final int numQueries;

    public VerifyDTAPNUnfoldOptions(String modelOut, String queryOut, int numQueries) {
        this.modelOut = modelOut;
        this.queryOut = queryOut;
        this.numQueries = numQueries;
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

        String queryIndexList = "1";
        for (int i = 2; i <= numQueries; i++){
            queryIndexList += "," + i;
        }
        add("--xml-queries", queryIndexList);

        add("--bindings");

        return options;
    }
}
