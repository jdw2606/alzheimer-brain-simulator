package com.alzheimersim.ui;

import com.alzheimersim.network.NeuronPopulation;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class InspectorPanel extends VBox {
    private final Label title = new Label("Neuron inspector");
    private final Label body = new Label("Click a neuron to inspect it.");

    public InspectorPanel() {
        setSpacing(6);
        setPadding(new Insets(10));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        body.setWrapText(true);
        getChildren().addAll(title, body);
    }

    public void showNothingSelected() {
        body.setText("Click a neuron to inspect it.");
    }

    public void show(int index, NeuronPopulation pop) {
        if (index < 0) {
            showNothingSelected();
            return;
        }
        body.setText(String.format(
                "Neuron %d%nRegion: %s%nState: %s%n%nTau pathology: %.3f%nFunctional health: %.3f%nActivation: %.3f%nVulnerability: %.2f",
                index, pop.region[index], pop.stateOf(index),
                pop.tau[index], pop.health[index], pop.activation[index], pop.vulnerability[index]));
    }
}
