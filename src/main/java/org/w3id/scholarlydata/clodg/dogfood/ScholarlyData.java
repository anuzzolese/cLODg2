package org.w3id.scholarlydata.clodg.dogfood;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Date;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL2;

public class ScholarlyData {

	public static void convert(Model dogFood, File outF) {
        
        if(!outF.exists()) {
        	outF.mkdirs();
        }
        else if(!outF.isDirectory()) System.exit(-1);
        
        File scholarly = new File(outF, "scholarlydata");
        File folderForSimpleGraphs = new File(scholarly, "simple");
        if(!folderForSimpleGraphs.exists()) folderForSimpleGraphs.mkdirs();
        File folderForAlignmentGraphs = new File(scholarly, "alignments");
        if(!folderForAlignmentGraphs.exists()) folderForAlignmentGraphs.mkdirs();
        
        Model modelOut = ModelFactory.createDefaultModel();
	    People people = new People(dogFood);
	    Organisations organisations = new Organisations(dogFood);
	    InProceedingsSet inProceedingsSet = new InProceedingsSet(dogFood);
	    Events events = new Events(dogFood);
	                		
		Collection<Person> persons = people.list();
		Collection<Organisation> orgs = organisations.list();
		Collection<InProceedings> inProcs = inProceedingsSet.list();
		Collection<Event> evs = events.list();
		System.out.println("Processing dataset ");
		System.out.println("    The dataset contains " + persons.size() + " people.");
		System.out.println("    The dataset contains " + orgs.size() + " organisations.");
		System.out.println("    The dataset contains " + inProcs.size() + " in proceedings.");
		System.out.println("    The dataset contains " + evs.size() + " events.");
		
		System.out.println();
		
		
		long start = System.currentTimeMillis();
		persons.forEach(person -> {
			person.asConfResource(modelOut);
		});
		long end = System.currentTimeMillis();
		System.out.println("    People converted in " + (end-start) + " millis.");
		
		start = System.currentTimeMillis();
		orgs.forEach(organisation -> {
			organisation.asConfResource(modelOut);
		});
		end = System.currentTimeMillis();
		System.out.println("    Organisations converted in " + (end-start) + " millis.");
		
		start = System.currentTimeMillis();
		inProcs.forEach(inProceedings -> {
			inProceedings.asConfResource(modelOut);
		});
		end = System.currentTimeMillis();
		System.out.println("    InProceedings converted in " + (end-start) + " millis.");
		
		start = System.currentTimeMillis();
		evs.forEach(event -> {
			event.asConfResource(modelOut);
		});
		end = System.currentTimeMillis();
		System.out.println("    Events converted in " + (end-start) + " millis.");
		
		ConferenceEvent conferenceEvent = new ConferenceEvent(dogFood);
		String acronym = conferenceEvent.getAcronym();
		acronym = acronym.toLowerCase().replaceAll(" ", "");
		
		StmtIterator stmtIt = dogFood.listStatements(null, ModelFactory.createDefaultModel().createProperty(SWC.NS + "completeGraph"), (RDFNode) null);
		
		Resource completeGraph = null;
		if(stmtIt.hasNext()) completeGraph = (Resource)stmtIt.next().getObject();
		
		if(completeGraph == null){
			String uri = conferenceEvent.getResource().getURI();
			uri = uri.replace("http://data.semanticweb.org/conference/", ConferenceOntology.RESOURCE_NS + "conference/");
			completeGraph = ModelFactory.createDefaultModel().createResource(uri + "/complete");
		}
		Model voidDescriptor = addVOID(completeGraph, acronym);
	                		
		try {
			modelOut.add(voidDescriptor);
			modelOut.write(new FileOutputStream(new File(folderForSimpleGraphs, "conference.ttl")), "TURTLE");
			modelOut.remove(voidDescriptor);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Model schema = FileManager.get().loadModel("ontologies/conference-ontology-alignments.owl");
		
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		reasoner = reasoner.bindSchema(schema);
		
		
		String sparql = "CONSTRUCT {?s <" + OWL2.sameAs + "> ?o} WHERE {?s <" + OWL2.sameAs + "> ?o}";
		Model sameAsModel = QueryExecutor.execConstruct(modelOut, sparql);
		
		/*
		 * Remove the owl:sameAs axioms towards the SWDF as the harden reasoning 
		 */
		modelOut.remove(sameAsModel);
		
		InfModel infmodel = ModelFactory.createInfModel(reasoner, modelOut);
		StmtIterator it = infmodel.listStatements();
		Model inf = ModelFactory.createDefaultModel();
		while(it.hasNext()){
			Statement stmt = it.next();
			inf.add(stmt);
		}
		try {
			inf.remove(infmodel.getDeductionsModel());
			
			voidDescriptor = addVOID(completeGraph, acronym + "-alignments");
			
			inf.add(voidDescriptor);
			/*
			 * Remove owl:sameAs having the same entity as subject and object
			 */
			SameAsRemoval.remove(inf);
			/*
			 * Add the owl:sameAs axioms towards the SWDF 
			 */
			inf.add(sameAsModel);
			inf.write(new FileOutputStream(new File(folderForAlignmentGraphs.getAbsolutePath(), "conference-alignments.ttl")), "TURTLE");
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	                		
	                		
        
    }
	
	private static Model addVOID(Resource completeGraph, String datasetName){
		
		Model model = ModelFactory.createDefaultModel(); 
		
		System.out.println("Complete graph " + completeGraph);
		System.out.println("Dataset name " + datasetName);
		
		Resource datasetType = model.createResource("http://rdfs.org/ns/void#Dataset");
		Resource dataset = model.createResource(ConferenceOntology.RESOURCE_NS + "dataset/" + datasetName, datasetType);
		dataset.addLiteral(DCTerms.title, "Dataset about " + datasetName + ".");
		dataset.addLiteral(DCTerms.created, new Date(System.currentTimeMillis()).toString());
		
		Resource andrea = model.createResource(ConferenceOntology.RESOURCE_NS + "person/andrea-giovanni-nuzzolese", FOAF.Person);
		andrea.addProperty(FOAF.name, "Andrea Giovanni Nuzzolese");
		andrea.addProperty(FOAF.givenname, "Andrea Giovanni");
		andrea.addProperty(ModelFactory.createDefaultModel().createProperty(FOAF.NS + "familyName"), "Nuzzolese");
		
		Resource annalisa = model.createResource(ConferenceOntology.RESOURCE_NS + "person/anna-lisa-gentile", FOAF.Person);
		annalisa.addProperty(FOAF.name, "Anna Lisa Gentile");
		annalisa.addProperty(FOAF.givenname, "Anna Lisa");
		annalisa.addProperty(ModelFactory.createDefaultModel().createProperty(FOAF.NS + "familyName"), "Gentile");
		
		Resource valentina = model.createResource(ConferenceOntology.RESOURCE_NS + "person/valentina-presutti", FOAF.Person);
		valentina.addProperty(FOAF.name, "Valentina Presutti");
		valentina.addProperty(FOAF.givenname, "Valentina");
		valentina.addProperty(ModelFactory.createDefaultModel().createProperty(FOAF.NS + "familyName"), "Presutti");
		
		Resource aldo = model.createResource(ConferenceOntology.RESOURCE_NS + "person/aldo-gangemi", FOAF.Person);
		aldo.addProperty(FOAF.name, "Aldo Gangemi");
		aldo.addProperty(FOAF.givenname, "Aldo");
		aldo.addProperty(ModelFactory.createDefaultModel().createProperty(FOAF.NS + "familyName"), "Gangemi");
		
		dataset.addProperty(DCTerms.creator, andrea);
		dataset.addProperty(DCTerms.creator, annalisa);
		dataset.addProperty(DCTerms.creator, valentina);
		dataset.addProperty(DCTerms.creator, aldo);
		
		
		dataset.addProperty(model.createProperty("http://www.w3.org/ns/prov#hadPrimarySource"), completeGraph);
		
		return model;
	}
	
}
