package org.w3id.scholarlydata.clodg.dogfood;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.OWL2;

public class SameAsRemoval {

	private static final String sourceConferences = "out/dumps/conferences/alignments";
	private static final String sourceWorkshops = "out/dumps/workshops/alignments";
	
	private static final String targetConferences = "out/dumps/sparql/conferences";
	private static final String targetWorkshops = "out/dumps/sparql/workshops";
	
	
	public static Model remove(Model model){
		
		String sparql = "CONSTRUCT {?subj <" + OWL2.sameAs + "> ?obj} "
				+ "WHERE{?subj <" + OWL2.sameAs + "> ?obj . "
				+ "FILTER(REGEX(STR(?obj), \"" + ConferenceOntology.RESOURCE_NS + "\"))"
				+ "}";
		Model remove = QueryExecutor.execConstruct(model, sparql);
		
		return model.remove(remove);
	}
	
	public static void main(String[] args) {
		
		File[] confsIn = new File(sourceConferences).listFiles();
		for(File confIn : confsIn){
			String name = confIn.getName();
			
			Model model = FileManager.get().loadModel(confIn.getAbsolutePath());
			model = SameAsRemoval.remove(model);
			
			try {
				model.write(new FileOutputStream(new File(targetConferences + "/" + name)));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		confsIn = new File(sourceWorkshops).listFiles();
		for(File workIn : confsIn){
			String name = workIn.getName();
			
			Model model = FileManager.get().loadModel(workIn.getAbsolutePath());
			model = SameAsRemoval.remove(model);
			
			try {
				model.write(new FileOutputStream(new File(targetWorkshops + "/" + name)));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
