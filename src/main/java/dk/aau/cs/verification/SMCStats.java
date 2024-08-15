package dk.aau.cs.verification;

import java.util.List;
import java.util.ArrayList;
import pipe.gui.graph.GraphPoint;

public class SMCStats extends Stats {

    private int executedRuns;
    private int validRuns;
    private float averageRunTime;
    private float averageRunLength;
    private float verificationTime;
    private float averageValidRunTime = -1.0f;
    private float averageValidRunLength = -1.0f;
    private float validRunTimeStdDev = -1.0f;
    private float validRunLengthStdDev = -1.0f;

    private List<GraphPoint> cumulativeStepPoints;
    private List<GraphPoint> cumulativeDelayPoints;

    public SMCStats(int executedRuns, int validRuns, float averageTime, float averageLength) {
        super(-1,-1,-1);
        this.executedRuns = executedRuns;
        this.validRuns = validRuns;
        this.averageRunTime = averageTime;
        this.averageRunLength = averageLength;
        this.verificationTime = -1.0f;
    }

    public SMCStats(int executedRuns, int validRuns, float averageTime, float averageLength, float verificationTime, List<GraphPoint> cumulativeStepPoints, List<GraphPoint> cumulativeDelayPoints) {
        super(-1,-1,-1);
        this.executedRuns = executedRuns;
        this.validRuns = validRuns;
        this.averageRunTime = averageTime;
        this.averageRunLength = averageLength;
        this.verificationTime = verificationTime;
        this.cumulativeStepPoints = cumulativeStepPoints;
        this.cumulativeDelayPoints = cumulativeDelayPoints; 
    }

    public float getVerificationTime() {
        return this.verificationTime;
    }

    public int getExecutedRuns() {
        return executedRuns;
    }

    public void setExecutedRuns(int executedRuns) {
        this.executedRuns = executedRuns;
    }

    @Override
    public String toString() {
        return "Number of runs executed: " +
            executedRuns +
            System.getProperty("line.separator") +
            "Number of valid runs: " +
            validRuns +
            System.getProperty("line.separator") +
            "Average run duration: " +
            averageRunTime +
            System.getProperty("line.separator") +
            "Average transitions fired per run: " +
            averageRunLength +
            (averageValidRunTime >= 0 ?
                System.getProperty("line.separator") +
                "Valid run duration (average): " +
                averageValidRunTime : "") +
            (validRunTimeStdDev >= 0 ?
                System.getProperty("line.separator") +
                    "Valid run duration (standard deviation): " +
                    validRunTimeStdDev : "") +
            (averageValidRunLength >= 0 ?
                System.getProperty("line.separator") +
                "Transitions fired per valid run (average): " +
                averageValidRunLength : "") +
            (validRunLengthStdDev >= 0 ?
                System.getProperty("line.separator") +
                    "Transitions fired per valid run (standard deviation): " +
                    validRunLengthStdDev : "");
    }

    public float getAverageValidRunTime() {
        return averageValidRunTime;
    }

    public void setAverageValidRunTime(float averageValidRunTime) {
        this.averageValidRunTime = averageValidRunTime;
    }

    public float getAverageValidRunLength() {
        return averageValidRunLength;
    }

    public void setAverageValidRunLength(float averageValidRunLength) {
        this.averageValidRunLength = averageValidRunLength;
    }


    public List<GraphPoint> getCumulativeStepPoints() {
        return cumulativeStepPoints;
    }

    public List<GraphPoint> getCumulativeDelayPoints() {
        return cumulativeDelayPoints;
    }

    public float getValidRunTimeStdDev() {
        return validRunTimeStdDev;
    }

    public void setValidRunTimeStdDev(float validRunTimeStdDev) {
        this.validRunTimeStdDev = validRunTimeStdDev;
    }

    public float getValidRunLengthStdDev() {
        return validRunLengthStdDev;
    }

    public void setValidRunLengthStdDev(float validRunLengthStdDev) {
        this.validRunLengthStdDev = validRunLengthStdDev;
    }
}
