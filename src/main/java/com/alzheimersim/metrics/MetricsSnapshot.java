package com.alzheimersim.metrics;

public class MetricsSnapshot {
    public int step;

    public double avgRecallAccuracy;

    public double avgTauBurden;
    public double ecTauBurden;
    public double hippocampusTauBurden;
    public double cortexTauBurden;

    public int healthyCount;
    public int mildCount;
    public int significantCount;
    public int severeCount;
    public int nonfunctionalCount;
    public int deadCount;

    public int functionalSynapseCount;
    public int lostSynapseCount;
    public double avgSynapticStrength;
}
