package gdamato;

import gdamato.core.RoughConceptClassifier;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import javax.swing.*;
import java.io.File;

public class Main {
    private static RoughConceptClassifier RCC;

    public static void main(String[] args) {
        System.out.println("- Projection Table Builder -");
        RCC = new RoughConceptClassifier();

        String ontologyPath, csvPath;

        //Utility window to choose which file to open and where should the table be saved
        JFileChooser fileChooser = new JFileChooser();
        int userSelection;

        //Ask for the ontology path
        fileChooser.setDialogTitle("Specify an ontology file to open.");
        userSelection = fileChooser.showOpenDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            ontologyPath = fileChooser.getSelectedFile().getAbsolutePath();
            System.out.println("Opening ontology: " + ontologyPath);
        } else return;

        //Ask where to save the CSV file
        fileChooser.setDialogTitle("Specify where to save the table.");
        userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            csvPath = fileChooser.getSelectedFile().getAbsolutePath();
            System.out.println("Saving table to: " + csvPath);
        } else return;

        try {
            RCC.LoadOntology(ontologyPath);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        RCC.BuildProjectionTable();

        try {
            RCC.ExportAsCSV(csvPath);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
