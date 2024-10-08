package dk.aau.cs.model.tapn;

import java.util.LinkedHashMap;

public class SMCDiscreteUniformDistribution extends SMCDistribution {

    public static final String NAME = "discrete uniform";

    public SMCDiscreteUniformDistribution(double a, double b) {
        this.a = a;
        this.b = b;
        this.mean = (a + b) / 2.0;
    }

    @Override
    public String distributionName() {
        return NAME;
    }

    @Override
    public LinkedHashMap<String, Double> getParameters() {
        LinkedHashMap<String, Double> params = new LinkedHashMap<>();
        params.put("a", a);
        params.put("b", b);
        return params;
    }

    @Override
    public Double getMean() {
        return mean;
    }

    @Override
    public String explanation() {
        return "<html>" +
            "Will choose an integer point between<br/>" + 
            "two numbers A and B (B included),<br/>" + 
            "where every number has the same<br/>" + 
            "probability of being chosen." +
            "</html>";
    }

    public static SMCDiscreteUniformDistribution defaultDistribution() {
        return new SMCDiscreteUniformDistribution(0, 10);
    }

    public double a;
    public double b;

    private final double mean;
}
