package gdamato;

import gdamato.core.RoughConceptClassifier;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, world!");
        RoughConceptClassifier RCC = new RoughConceptClassifier();
        try {
            RCC.LoadOntology("C:\\Users\\Gianf\\Dropbox\\Tesi\\Ontologie\\wine.owl");
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }
}
