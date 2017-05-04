package org.w3id.scholarlydata.clodg.dogfood;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3id.scholarlydata.clodg.dogfood.arq.EventTypeBinder;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL2;

public class Process {

	private static final String INPUT_FOLDER = "i";
    private static final String INPUT_FOLDER_LONG = "input";
    private static final String OUTPUT_FOLDER = "o";
    private static final String OUTPUT_FOLDER_LONG = "output";
    private static final String APPEND_MODE = "a";
    private static final String APPEND_MODE_LONG = "append";
	
	public static void main(String[] args) {
        
        /*
         * Set-up the options for the command line parser.
         */
        Options options = new Options();
        
        Builder optionBuilder = Option.builder(INPUT_FOLDER);
        Option inputFolderOption = optionBuilder.argName("file")
                                 .hasArg()
                                 .required(true)
                                 .desc("MANDATORY - Input folder containing the RDF dumps of the Semantic Web Dog Food.")
                                 .longOpt(INPUT_FOLDER_LONG)
                                 .build();
        
        optionBuilder = Option.builder(OUTPUT_FOLDER);
        Option outputFolderOption = optionBuilder.argName("file")
                .hasArg()
                .required(true)
                .desc("MANDATORY - Output folder for generated RDF dumps.")
                .longOpt(OUTPUT_FOLDER_LONG)
                .build();
        
        optionBuilder = Option.builder(APPEND_MODE);
        Option appendModeOption = optionBuilder.argName("flag")
                .required(false)
                .hasArg(false)
                .desc("OPTIONAL - Set the write mode to append.")
                .longOpt(APPEND_MODE_LONG)
                .build();
        
        options.addOption(inputFolderOption);
        options.addOption(outputFolderOption);
        options.addOption(appendModeOption);
        
        CommandLine commandLine = null;
        
        CommandLineParser cmdLineParser = new DefaultParser();
        try {
            commandLine = cmdLineParser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "process", options );
        }
        
        if(commandLine != null){
            for(Option option : commandLine.getOptions()){
                System.out.println(option.getValue());
            }
            String inputFolder = commandLine.getOptionValue(INPUT_FOLDER);
            String outputFolder = commandLine.getOptionValue(OUTPUT_FOLDER);
            boolean isAppend  = commandLine.hasOption(APPEND_MODE);
            
            File outF = new File(outputFolder);
            if(!outF.exists()) {
            	outF.mkdirs();
            }
            else if(!outF.isDirectory()) System.exit(-1);
            
            File folderForSimpleGraphs = new File(outputFolder + "/simple");
            if(!folderForSimpleGraphs.exists()) folderForSimpleGraphs.mkdirs();
            File folderForAlignmentGraphs = new File(outputFolder + "/alignments");
            if(!folderForAlignmentGraphs.exists()) folderForAlignmentGraphs.mkdirs();
            
            RoleMappings roleMappings = RoleMappings.getInstance();
            
            
            if(inputFolder != null){
                
                File dumpFolder = new File(inputFolder);
                if(dumpFolder.exists() && dumpFolder.isDirectory()){
                	
                	
                	FunctionRegistry.get().put(EventTypeBinder.IRI, EventTypeBinder.class);
                	
                	File[] dumps = dumpFolder.listFiles();
                	for(File dump : dumps){
                		String dumpName = dump.getName();
                		System.out.println(dumpName);
                		dumpName = dumpName.substring(0, dumpName.lastIndexOf("."));
                		
                		File ftest = new File(folderForAlignmentGraphs.getAbsolutePath() + "/" + dumpName + "-alignments.rdf");
                		
                		
                		if(!isAppend || !ftest.exists()){
	                		Model modelOut = ModelFactory.createDefaultModel();
	                		Model model = DatasetLoader.load(dump);
	                		//model.write(System.out, "N-TRIPLES");
	                		
	                		//RoleKB.getInstance().addRolesFromModel(model);
	                		People people = new People(model);
	                		Organisations organisations = new Organisations(model);
	                		Proceedings proceedings = new Proceedings(model);
	                		Events events = new Events(model);
	                		
	                		List<Resource> proceedingsVolumes = proceedings.asConfResource(modelOut);
	                		
	                		Collection<Person> persons = people.list();
	                		Collection<Organisation> orgs = organisations.list();
	                		Collection<InProceedings> inProcs = proceedings.list();
	                		Collection<Event> evs = events.list();
	                		System.out.println("Processing dataset " + dump.getName());
	                		System.out.println("    The dataset contains " + persons.size() + " people.");
	                		System.out.println("    The dataset contains " + orgs.size() + " organisations.");
	                		System.out.println("    The dataset contains " + proceedingsVolumes.size() + " proceedings volumes.");
	                		System.out.println("    The dataset contains " + inProcs.size() + " in proceedings.");
	                		System.out.println("    The dataset contains " + evs.size() + " events.");
	                		for(Person person : persons){
	                			person.asConfResource(modelOut);
	                		}
	                		for(Organisation organisation : orgs){
	                			organisation.asConfResource(modelOut);
	                		}
	                		for(InProceedings inProceedings : inProcs){
	                			inProceedings.asConfResource(modelOut);
	                		}
	                		
	                		for(Event event : evs){
	                			event.asConfResource(modelOut);
	                		}
	                		
	                		ConferenceEvent conferenceEvent = ConferenceEvent.getInstance(model);
	                		String acronym = conferenceEvent.getAcronym();
	                		acronym = acronym.toLowerCase().replaceAll(" ", "");
	                		
	                		StmtIterator stmtIt = model.listStatements(null, ModelFactory.createDefaultModel().createProperty(SWC.NS + "completeGraph"), (RDFNode) null);
	                		
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
								modelOut.write(new FileOutputStream(folderForSimpleGraphs.getAbsolutePath() + "/" + dumpName + ".rdf"));
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
								inf.write(new FileOutputStream(new File(folderForAlignmentGraphs.getAbsolutePath() + "/" + dumpName + "-alignments.rdf")));
								
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                		
	                		
	                		/*
	                		try {
	                			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	                	        
	                			
	                	        OWLOntologyMerger merger = new OWLOntologyMerger(manager);
	                	        
	                	        OWLOntology data = jena2OwlApi(manager, modelOut);
	                	        OWLOntology ontology = jena2OwlApi(manager, FileManager.get().loadModel("ontologies/conference-ontology-alignments.owl"));
	                	        
	                	        Reasoner.ReasonerFactory rf = new Reasoner.ReasonerFactory() {
	                	            @Override
	                	            public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) {
	                	                Configuration configuration = new Configuration();
	                	                configuration.ignoreUnsupportedDatatypes = true;
	                	                return super.createReasoner(ontology, configuration);
	                	            }
	                	        };
	                	        
	                	        OWLOntology ont = merger.createMergedOntology(manager, IRI.create(ConferenceOntology.NS));
	                	        Reasoner reasoner = (Reasoner)rf.createReasoner(ont);
	                			//org.semanticweb.HermiT.Reasoner hreasoner = new org.semanticweb.HermiT.Reasoner(ont);
	                	        reasoner.classifyDataProperties();
	                			
	                	        reasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
	                			List<InferredAxiomGenerator<? extends OWLAxiom>> generators=new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
	                	        
	                	        generators.add(new InferredClassAssertionAxiomGenerator());
	                	        
	
	                	        InferredOntologyGenerator iog=new InferredOntologyGenerator(reasoner,generators);
	                	        OWLOntology inferredAxiomsOntology = manager.createOntology();
	                	        // Now we use the inferred ontology generator to fill the ontology. That might take some 
	                	        // time since it involves possibly a lot of calls to the reasoner.    
	                	        iog.fillOntology(manager, inferredAxiomsOntology);
	                	        OutputStream outputStream=new FileOutputStream(new File("test.rdf"));
	                	        // We use the same format as for the input ontology.
	                	        manager.saveOntology(inferredAxiomsOntology, new RDFXMLOntologyFormat(), outputStream);
	                	        //infModel.getDeductionsModel().write(new FileOutputStream(new File("test.rdf")));
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (OWLOntologyCreationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (OWLOntologyStorageException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							*/
	                	}
                		else System.out.println("---" + ftest.getAbsolutePath());
                	}
                }
                
            }
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
