package gdamato;

import gdamato.core.RoughConceptClassifier;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class Main {
    private static RoughConceptClassifier RCC;

    public static void main(String[] args) {
        System.out.println("Hello, world!");
        RCC = new RoughConceptClassifier();
        BuildNewDataset();

    }

    public static void BuildNewDataset() {
        try {
            RCC.LoadOntology("C:\\Users\\Gianf\\Dropbox\\Tesi\\Ontologie\\wine.owl");
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        RCC.BuildProjectionTable();

        try {
            RCC.ExportAsWekaARFF("C:\\Users\\Gianf\\Desktop\\table.arff");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void LoadDataSet() {
        try {
            RCC.LoadWekaARFF("C:\\Users\\Gianf\\Desktop\\table.arff");
            RCC.ComputePCA();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
