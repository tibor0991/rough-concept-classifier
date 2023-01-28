package gdamato;

import gdamato.core.ProjectionBuilder;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import javax.swing.*;

public class Main {
    private static ProjectionBuilder builder;

    public static void main(String[] args) {
        System.out.println("- Projection Table Builder -");
        builder = new ProjectionBuilder();

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
            builder.LoadOntology(ontologyPath);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        builder.BuildProjectionTable();

        try {
            builder.ExportAsCSV(csvPath);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
