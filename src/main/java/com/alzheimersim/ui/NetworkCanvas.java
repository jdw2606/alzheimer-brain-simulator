package com.alzheimersim.ui;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

import com.alzheimersim.engine.SimulationEngine;
import com.alzheimersim.network.NeuronPopulation;
import com.alzheimersim.network.Region;
import com.alzheimersim.network.SynapseGraph;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

public class NetworkCanvas extends Canvas {
    public enum Overlay { PATHOLOGY, ACTIVITY, SYNAPTIC }

    private SimulationEngine engine;
    private NetworkLayout layout;

    private double scale = 1.0;
    private double panX = 0;
    private double panY = 0;
    private boolean fitted = false;

    private double dragStartX;
    private double dragStartY;
    private double dragStartPanX;
    private double dragStartPanY;
    private boolean dragged;

    private final Map<Region, Boolean> regionVisible = new EnumMap<>(Region.class);
    private boolean synapsesVisible = true;
    private Overlay overlay = Overlay.PATHOLOGY;
    private int selectedNeuron = -1;

    private IntConsumer onSelect = i -> { };

    public NetworkCanvas() {
        for (Region r : Region.values()) regionVisible.put(r, true);
        setFocusTraversable(true);
        addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
        addEventHandler(ScrollEvent.SCROLL, this::onScroll);
    }

    public void setEngine(SimulationEngine engine) {
        this.engine = engine;
        this.layout = NetworkLayout.compute(engine.population);
        this.selectedNeuron = -1;
        this.fitted = false;
        render();
    }

    public void setOnSelect(IntConsumer onSelect) {
        this.onSelect = onSelect;
    }

    public void setRegionVisible(Region region, boolean visible) {
        regionVisible.put(region, visible);
        render();
    }

    public void setSynapsesVisible(boolean visible) {
        this.synapsesVisible = visible;
        render();
    }

    public void setOverlay(Overlay overlay) {
        this.overlay = overlay;
        render();
    }

    public void resetView() {
        fitted = false;
        render();
    }

    private void fitToView() {
        double w = layout.maxX - layout.minX;
        double h = layout.maxY - layout.minY;
        double canvasW = getWidth() > 0 ? getWidth() : 900;
        double canvasH = getHeight() > 0 ? getHeight() : 600;
        double margin = 1.15;
        double sx = w > 0 ? canvasW / (w * margin) : 1;
        double sy = h > 0 ? canvasH / (h * margin) : 1;
        scale = Math.min(sx, sy);
        double centerX = (layout.minX + layout.maxX) / 2;
        double centerY = (layout.minY + layout.maxY) / 2;
        panX = canvasW / 2 - centerX * scale;
        panY = canvasH / 2 - centerY * scale;
        fitted = true;
    }

    private void onMousePressed(MouseEvent e) {
        dragStartX = e.getX();
        dragStartY = e.getY();
        dragStartPanX = panX;
        dragStartPanY = panY;
        dragged = false;
    }

    private void onMouseDragged(MouseEvent e) {
        double dx = e.getX() - dragStartX;
        double dy = e.getY() - dragStartY;
        if (Math.abs(dx) > 3 || Math.abs(dy) > 3) dragged = true;
        panX = dragStartPanX + dx;
        panY = dragStartPanY + dy;
        render();
    }

    private void onMouseReleased(MouseEvent e) {
        if (!dragged) selectNearest(e.getX(), e.getY());
    }

    private void onScroll(ScrollEvent e) {
        double factor = e.getDeltaY() > 0 ? 1.1 : 1 / 1.1;
        double newScale = Math.max(0.05, Math.min(20.0, scale * factor));
        double mx = e.getX();
        double my = e.getY();
        panX = mx - (mx - panX) * (newScale / scale);
        panY = my - (my - panY) * (newScale / scale);
        scale = newScale;
        render();
    }

    private void selectNearest(double screenX, double screenY) {
        if (engine == null) return;
        NeuronPopulation pop = engine.population;
        int best = -1;
        double bestDist = 12 * 12;
        for (int i = 0; i < pop.size; i++) {
            if (!pop.alive[i] || !regionVisible.get(pop.region[i])) continue;
            double sx = layout.x[i] * scale + panX;
            double sy = layout.y[i] * scale + panY;
            double d2 = (sx - screenX) * (sx - screenX) + (sy - screenY) * (sy - screenY);
            if (d2 < bestDist) {
                bestDist = d2;
                best = i;
            }
        }
        selectedNeuron = best;
        onSelect.accept(best);
        render();
    }

    public void render() {
        if (engine == null) return;
        if (!fitted) fitToView();

        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.web("#1a1d23"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        NeuronPopulation pop = engine.population;
        SynapseGraph graph = engine.graph;

        if (synapsesVisible) {
            List<int[]> edges = graph.allEdges();
            for (int[] edge : edges) {
                int i = edge[0];
                int j = edge[1];
                if (!pop.alive[i] || !pop.alive[j]) continue;
                if (!regionVisible.get(pop.region[i]) || !regionVisible.get(pop.region[j])) continue;
                double w = graph.getWeight(i, j);
                double alpha = Math.min(pop.fadeAlpha[i], pop.fadeAlpha[j]) * Math.min(1.0, 0.15 + w);
                gc.setStroke(Color.web("#7f8c9a", Math.max(0, Math.min(1, alpha))));
                gc.setLineWidth(Math.max(0.4, Math.min(4.0, w * 4.0)) * Math.max(0.3, Math.min(2.0, scale)));
                gc.strokeLine(layout.x[i] * scale + panX, layout.y[i] * scale + panY,
                        layout.x[j] * scale + panX, layout.y[j] * scale + panY);
            }
        }

        double neuronRadius = Math.max(1.5, Math.min(6.0, 3.0 * Math.sqrt(scale)));
        for (int i = 0; i < pop.size; i++) {
            if (!pop.alive[i] || pop.fadeAlpha[i] <= 0) continue;
            if (!regionVisible.get(pop.region[i])) continue;
            double sx = layout.x[i] * scale + panX;
            double sy = layout.y[i] * scale + panY;
            gc.setGlobalAlpha(pop.fadeAlpha[i]);
            gc.setFill(colorFor(i));
            gc.fillOval(sx - neuronRadius, sy - neuronRadius, neuronRadius * 2, neuronRadius * 2);
            gc.setGlobalAlpha(1.0);

            if (i == selectedNeuron) {
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1.5);
                gc.strokeOval(sx - neuronRadius - 3, sy - neuronRadius - 3, (neuronRadius + 3) * 2, (neuronRadius + 3) * 2);
            }
        }

        drawRegionLabels(gc);
    }

    private void drawRegionLabels(GraphicsContext gc) {
        NeuronPopulation pop = engine.population;
        gc.setFill(Color.web("#c9d1d9"));
        gc.setFont(javafx.scene.text.Font.font("System", 13));
        for (Region r : Region.values()) {
            if (!regionVisible.get(r)) continue;
            double sumX = 0, minY = Double.MAX_VALUE;
            int n = 0;
            for (int i = 0; i < pop.size; i++) {
                if (pop.region[i] != r) continue;
                sumX += layout.x[i];
                minY = Math.min(minY, layout.y[i]);
                n++;
            }
            if (n == 0) continue;
            String name = switch (r) {
                case ENTORHINAL_CORTEX -> "Entorhinal cortex";
                case HIPPOCAMPUS -> "Hippocampus";
                case CORTEX -> "Cortex";
            };
            gc.fillText(name, (sumX / n) * scale + panX - 40, minY * scale + panY - 14);
        }
    }

    private Color colorFor(int i) {
        NeuronPopulation pop = engine.population;
        return switch (overlay) {
            case PATHOLOGY -> switch (pop.stateOf(i)) {
                case HEALTHY -> Color.web("#2ecc71");
                case MILD -> Color.web("#f1c40f");
                case SIGNIFICANT -> Color.web("#e67e22");
                case SEVERE -> Color.web("#e74c3c");
                case NONFUNCTIONAL -> Color.web("#5a5f66");
                case DEAD -> Color.TRANSPARENT;
            };
            case ACTIVITY -> Color.color(0.05, 0.15 + 0.7 * pop.activation[i], 0.35 + 0.6 * pop.activation[i]);
            case SYNAPTIC -> {
                double initial = engine.graph.initialStrength(i);
                double current = engine.graph.totalWeight(i);
                double intact = initial > 0 ? Math.max(0, Math.min(1, current / initial)) : 1;
                yield Color.color(1 - intact, intact, 0.3);
            }
        };
    }
}
