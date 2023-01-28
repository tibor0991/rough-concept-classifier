package gdamato.core;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.Table;
import gdamato.types.ProjectionValue;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.QNameShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;


import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ProjectionBuilder {


    //Ontology
    private OWLOntologyManager ontMgr;
    private OWLOntology onto;
    private OWLReasoner reasoner;
    private OWLDataFactory dataFactory;

    private List<OWLClass> classes;
    private List<String> classNames;
    private List<OWLNamedIndividual> individuals;
    private List<String> individualNames;

    //Projection
    private Table<String, String, ProjectionValue> projectionTable;



    public ProjectionBuilder() {
        ontMgr = OWLManager.createOWLOntologyManager();
    }

    public void LoadOntology(String path_to) throws OWLOntologyCreationException {
        onto = ontMgr.loadOntologyFromOntologyDocument(new File(path_to));
        reasoner = new ReasonerFactory().createReasoner(onto);
        dataFactory = ontMgr.getOWLDataFactory();

        reasoner.isConsistent();
    }

    public void BuildProjectionTable() {
        QNameShortFormProvider sfp = new QNameShortFormProvider();
        Stopwatch buildTimer = Stopwatch.createStarted();
        //Retrieve list of classes defined in the ontology
        classes = onto.classesInSignature(Imports.INCLUDED).collect(Collectors.toList());
        classes.removeIf(owlClass -> owlClass.isTopEntity());   //here I remove the owl:Thing class
        System.out.println("Classes in this ontology (" + classes.size()+") :");

        classNames = new ArrayList<>();

        //Create an header for each class
        for(OWLClass c : classes) {
            String shortName = sfp.getShortForm(c);
            System.out.println(" -" + shortName);
            classNames.add(shortName);
        }

        //Retrieve the individuals in the ontology
        individuals = onto.individualsInSignature().collect(Collectors.toList());
        System.out.println("Individuals in this ontology: " + individuals.size());

        //Get the list of individual names
        individualNames = new ArrayList<>();
        for(OWLNamedIndividual i: individuals) {
            String shortName = sfp.getShortForm(i);
            System.out.println(" -" + shortName);
            individualNames.add(shortName);
        }

        //create the table that stores the data
        projectionTable = ArrayTable.create(individualNames, classNames);
        int progressCounter = 0;
        for(OWLClass c : classes) {
            String className = sfp.getShortForm(c);
            progressCounter++;
            System.out.print(String.format("(%d/%d) Reasoning on %s ", progressCounter, classes.size(), className));

            Stopwatch instanceCheckTimer = Stopwatch.createStarted();
            NodeSet<OWLNamedIndividual> trueInClass  = reasoner.getInstances(c);
            long retrievalTime = instanceCheckTimer.stop().elapsed(TimeUnit.MILLISECONDS);

            Stopwatch complementCheckTimer = Stopwatch.createStarted();
            NodeSet<OWLNamedIndividual> falseInClass = reasoner.getInstances(c.getObjectComplementOf());
            long complementTime = complementCheckTimer.stop().elapsed(TimeUnit.MILLISECONDS);


            for(OWLNamedIndividual i : individuals) {
                String individualName = sfp.getShortForm(i);
                ProjectionValue v;
                if(trueInClass.containsEntity(i)) {
                    v = ProjectionValue.TRUE;
                } else if(falseInClass.containsEntity(i)) {
                    v = ProjectionValue.FALSE;
                } else {
                    v = ProjectionValue.UNCERTAIN;
                }
                //System.out.println(String.format("%s(%s): %s", className, individualName, v));
                projectionTable.put(individualName, className, v);
            }
            System.out.println(String.format("took %3f seconds, %d ms for instance retrieval and %d ms for complement retrieval.", (retrievalTime + complementTime)/1000., retrievalTime, complementTime));
        }
        long buildTime = buildTimer.stop().elapsed(TimeUnit.MINUTES);
        System.out.println(String.format("Projection table built in %d minutes.", buildTime));
    }

    public void ExportAsCSV(String path_to) throws Exception {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(path_to))) {
            String delimiter = ";";
            //writes the header into the file
            StringJoiner headerJoiner = new StringJoiner(delimiter);
            headerJoiner.add("Name");
            for(String columnName : classNames) {
                headerJoiner.add(columnName);
            }

            bw.write(headerJoiner.toString());
            bw.newLine();

            //for each row...
            for(String individualName : individualNames) {
                StringJoiner rowJoiner = new StringJoiner(delimiter);
                rowJoiner.add(individualName);
                for (String className : classNames) {
                    rowJoiner.add(projectionTable.get(individualName, className).toString());
                }
                bw.write(rowJoiner.toString());
                bw.newLine();
            }
        }
    }


}
