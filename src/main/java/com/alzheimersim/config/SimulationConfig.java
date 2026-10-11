package com.alzheimersim.config;

public class SimulationConfig {
    public long randomSeed = 42L;

    public int entorhinalCount = 75;
    public int hippocampusCount = 125;
    public int cortexCount = 300;
    public int smallWorldK = 10;
    public double smallWorldRewireProb = 0.1;
    public double interRegionLinksPerNeuron = 1.5;
    public double initialWeightMin = 0.2;
    public double initialWeightMax = 0.6;

    public double memoryPatternFraction = 0.10;
    public double memoryPatternActivation = 0.85;
    public double memoryBaselineActivation = 0.05;
    public double hebbianLearningRate = 0.1;
    public int encodeSteps = 10;
    public int recallSettleSteps = 20;
    public double recallLeak = 0.5;

    public double tauSeedFraction = 0.05;
    public double tauSeedMin = 0.05;
    public double tauSeedMax = 0.15;
    public double tauAccumulationRate = 0.02;
    public double tauSpreadRate = 0.008;

    public double synapticDamageRate = 0.05;
    public double synapseLossThreshold = 0.02;

    public double healthDecayRate = 0.02;
    public double synapticLossHealthWeight = 0.3;
    public double vulnerabilityMin = 0.7;
    public double vulnerabilityMax = 1.3;
    public double nonfunctionalHealthThreshold = 0.3;
    public double nonfunctionalHazardSteepness = 8.0;
    public double nonfunctionalHazardMidpoint = 0.15;
    public int fadeSteps = 20;

    public int totalNeurons() {
        return entorhinalCount + hippocampusCount + cortexCount;
    }

    public SimulationConfig copy() {
        SimulationConfig c = new SimulationConfig();
        c.randomSeed = randomSeed;
        c.entorhinalCount = entorhinalCount;
        c.hippocampusCount = hippocampusCount;
        c.cortexCount = cortexCount;
        c.smallWorldK = smallWorldK;
        c.smallWorldRewireProb = smallWorldRewireProb;
        c.interRegionLinksPerNeuron = interRegionLinksPerNeuron;
        c.initialWeightMin = initialWeightMin;
        c.initialWeightMax = initialWeightMax;
        c.memoryPatternFraction = memoryPatternFraction;
        c.memoryPatternActivation = memoryPatternActivation;
        c.memoryBaselineActivation = memoryBaselineActivation;
        c.hebbianLearningRate = hebbianLearningRate;
        c.encodeSteps = encodeSteps;
        c.recallSettleSteps = recallSettleSteps;
        c.recallLeak = recallLeak;
        c.tauSeedFraction = tauSeedFraction;
        c.tauSeedMin = tauSeedMin;
        c.tauSeedMax = tauSeedMax;
        c.tauAccumulationRate = tauAccumulationRate;
        c.tauSpreadRate = tauSpreadRate;
        c.synapticDamageRate = synapticDamageRate;
        c.synapseLossThreshold = synapseLossThreshold;
        c.healthDecayRate = healthDecayRate;
        c.synapticLossHealthWeight = synapticLossHealthWeight;
        c.vulnerabilityMin = vulnerabilityMin;
        c.vulnerabilityMax = vulnerabilityMax;
        c.nonfunctionalHealthThreshold = nonfunctionalHealthThreshold;
        c.nonfunctionalHazardSteepness = nonfunctionalHazardSteepness;
        c.nonfunctionalHazardMidpoint = nonfunctionalHazardMidpoint;
        c.fadeSteps = fadeSteps;
        return c;
    }
}
