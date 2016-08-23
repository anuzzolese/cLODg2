package org.w3id.scholarlydata.clodg.dogfood;

public class schemagen {
	
	public static void main(String[] args) {
		jena.schemagen.main(new String[]{"-i", "ontologies/conference-ontology.owl", "--package", "org.scholarlydata.builder", "-o", "src/main/java", "--owl"});
		//jena.schemagen.main(new String[]{"-i", "ontologies/swc_2009-05-09.rdf", "--package", "org.scholarlydata.builder", "-o", "src/main/java", "--owl"});
		
	}

}
