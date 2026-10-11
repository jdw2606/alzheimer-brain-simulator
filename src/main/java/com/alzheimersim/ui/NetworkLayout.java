package com.alzheimersim.ui;

import java.util.EnumMap;
import java.util.Map;

import com.alzheimersim.network.NeuronPopulation;
import com.alzheimersim.network.Region;

public class NetworkLayout {
    private static final double GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5));
    private static final double POINT_SPACING = 13.0;
    private static final double CLUSTER_GAP = 90.0;

    public final double[] x;
    public final double[] y;
    public final double minX;
    public final double maxX;
    public final double minY;
    public final double maxY;

    private NetworkLayout(double[] x, double[] y, double minX, double maxX, double minY, double maxY) {
        this.x = x;
        this.y = y;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public static NetworkLayout compute(NeuronPopulation pop) {
        Map<Region, Integer> counts = new EnumMap<>(Region.class);
        for (Region r : Region.values()) counts.put(r, 0);
        for (int i = 0; i < pop.size; i++) counts.merge(pop.region[i], 1, Integer::sum);

        Map<Region, Double> radius = new EnumMap<>(Region.class);
        for (Region r : Region.values()) radius.put(r, POINT_SPACING * Math.sqrt(Math.max(1, counts.get(r))));

        Region[] order = {Region.ENTORHINAL_CORTEX, Region.HIPPOCAMPUS, Region.CORTEX};
        Map<Region, Double> centerX = new EnumMap<>(Region.class);
        double cursor = 0;
        for (Region r : order) {
            cursor += radius.get(r);
            centerX.put(r, cursor);
            cursor += radius.get(r) + CLUSTER_GAP;
        }

        Map<Region, Integer> seen = new EnumMap<>(Region.class);
        for (Region r : Region.values()) seen.put(r, 0);

        double[] xs = new double[pop.size];
        double[] ys = new double[pop.size];
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (int i = 0; i < pop.size; i++) {
            Region r = pop.region[i];
            int n = counts.get(r);
            int j = seen.merge(r, 1, Integer::sum) - 1;
            double rr = radius.get(r) * Math.sqrt((j + 0.5) / n);
            double theta = j * GOLDEN_ANGLE;
            xs[i] = centerX.get(r) + rr * Math.cos(theta);
            ys[i] = rr * Math.sin(theta);
            minX = Math.min(minX, xs[i]);
            maxX = Math.max(maxX, xs[i]);
            minY = Math.min(minY, ys[i]);
            maxY = Math.max(maxY, ys[i]);
        }
        return new NetworkLayout(xs, ys, minX, maxX, minY, maxY);
    }
}
