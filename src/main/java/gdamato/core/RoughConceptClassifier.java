package gdamato.core;


import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;

public class RoughConceptClassifier {


    //Ontology stuff

    OWLOntologyManager ontMgr;
    OWLOntology onto;
    OWLReasoner reasoner;


    public RoughConceptClassifier() {
        ontMgr = OWLManager.createOWLOntologyManager();
    }

    public void LoadOntology(String path_to) throws OWLOntologyCreationException {
        onto = ontMgr.loadOntologyFromOntologyDocument(new File(path_to));
        reasoner = new ReasonerFactory().createReasoner(onto);
        System.out.println("Is this ontology consistent?" + reasoner.isConsistent());
    }


    public void BuildProjectionTable() {

    }


}
