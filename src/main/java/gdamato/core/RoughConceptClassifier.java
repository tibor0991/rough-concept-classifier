package gdamato.core;

import gdamato.types.ProjectionValue;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.QNameShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.converters.ConverterUtils.DataSink;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.PrincipalComponents;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class RoughConceptClassifier {


    //Ontology stuff

    private OWLOntologyManager ontMgr;
    private OWLOntology onto;
    private OWLReasoner reasoner;
    private OWLDataFactory dataFactory;

    //Projection stuff
    private Instances projectionTable;


    public RoughConceptClassifier() {
        ontMgr = OWLManager.createOWLOntologyManager();
    }

    public void LoadOntology(String path_to) throws OWLOntologyCreationException {
        onto = ontMgr.loadOntologyFromOntologyDocument(new File(path_to));
        reasoner = new ReasonerFactory().createReasoner(onto);
        dataFactory = ontMgr.getOWLDataFactory();
    }


    public void BuildProjectionTable() {
        ArrayList<Attribute> attributes = new ArrayList<>();
        QNameShortFormProvider sfp = new QNameShortFormProvider();
        //Jesus Christ how awful
        Set<OWLClass> classes = onto.classesInSignature(Imports.INCLUDED).collect(Collectors.toSet());
        classes.removeIf(owlClass -> owlClass.isTopEntity());   //here I remove the owl:Thing class
        System.out.println("Classes in this ontology (" + classes.size()+") :");

        //adding a "Name" attribute in order to pass some info to the python script
        attributes.add(new Attribute("Name", true));

        //creating an attribute for each class
        for(OWLClass c : classes) {
            System.out.println(" -" + sfp.getShortForm(c));
            attributes.add(new Attribute(sfp.getShortForm(c)));
        }


        projectionTable = new Instances("OntologyTest", attributes, 0);

        Set<OWLNamedIndividual> individuals = onto.individualsInSignature().collect(Collectors.toSet());
        System.out.println("Individuals in this ontology: " + individuals.size());

        int count = 0;
        for(OWLNamedIndividual i:individuals) {
            Instance currentInstance = new DenseInstance(projectionTable.numAttributes());
            currentInstance.setDataset(projectionTable);
            String individualName = sfp.getShortForm(i);
            currentInstance.setValue(0, individualName);
            int idx = 1;
            for(OWLClass c: classes) {
                //System.out.print(String.format("Computing %s(%s):",sfp.getShortForm(c), individualName));
                ProjectionValue computedValue = computeProjectionValue(c, i);
                //System.out.println(computedValue);
                currentInstance.setValue(idx, mapValueToNumber(computedValue));
                idx++;
            }
            projectionTable.add(currentInstance);
            System.out.println(String.format("%d/%d done.", ++count, individuals.size()));
        }
    }

    private ProjectionValue computeProjectionValue(OWLClass c, OWLNamedIndividual i) {
        ProjectionValue v = ProjectionValue.UNCERTAIN;
        OWLAxiom instanceCheck = dataFactory.getOWLClassAssertionAxiom(c, i);
        if (reasoner.isEntailed(instanceCheck)) {
            v = ProjectionValue.TRUE;
        } else {
            OWLAxiom complementInstanceCheck = dataFactory.getOWLClassAssertionAxiom(c.getObjectComplementOf(), i);
            if (reasoner.isEntailed(complementInstanceCheck)) {
                v = ProjectionValue.FALSE;
            }
        }
        return v;
    }

    private double mapValueToNumber(ProjectionValue value) {
        double mapping = 0;
        switch(value) {
            case TRUE:
                mapping = 1;
                break;
            case FALSE:
                mapping = -1;
                break;
            case UNCERTAIN:
                mapping = 0;
                break;
        }
        return mapping;
    }

    public void ExportAsWekaARFF(String path_to)  throws Exception{
            DataSink.write(path_to, projectionTable);
    }

    public void LoadWekaARFF(String path_to) throws Exception {
        DataSource ds = new DataSource(path_to);
        projectionTable = ds.getDataSet();
    }

    public void ComputePCA() throws Exception {
        PrincipalComponents pca = new PrincipalComponents();
        pca.setInputFormat(projectionTable);
        Instances newTable = Filter.useFilter(projectionTable, pca);

        System.out.println(newTable);

    }


}
