package com.alzheimersim.network;

import java.util.Arrays;

public class NeuronPopulation {
    public int size;
    public Region[] region;
    public double[] activation;
    public double[] tau;
    public double[] health;
    public double[] vulnerability;
    public boolean[] functional;
    public boolean[] alive;
    public double[] fadeAlpha;
    public int[] fadeCountdown;

    public NeuronPopulation(int size) {
        this.size = size;
        this.region = new Region[size];
        this.activation = new double[size];
        this.tau = new double[size];
        this.health = new double[size];
        this.vulnerability = new double[size];
        this.functional = new boolean[size];
        this.alive = new boolean[size];
        this.fadeAlpha = new double[size];
        this.fadeCountdown = new int[size];

        Arrays.fill(alive, true);
        Arrays.fill(functional, true);
        Arrays.fill(health, 1.0);
        Arrays.fill(fadeAlpha, 1.0);
    }

    public enum NeuronState{HEALTHY, MILD, SIGNIFICANT, SEVERE, NONFUNCTIONAL, DEAD};

    public NeuronState stateOf(int i) {
            if (!alive[i]) return NeuronState.DEAD;
            if (!functional[i]) return NeuronState.NONFUNCTIONAL;
            double h = health[i];
            if (h >= 0.75) return NeuronState.HEALTHY;
            if (h >= 0.50) return NeuronState.MILD;
            if (h >= 0.25) return NeuronState.SIGNIFICANT;
            return NeuronState.SEVERE;
        }
}
