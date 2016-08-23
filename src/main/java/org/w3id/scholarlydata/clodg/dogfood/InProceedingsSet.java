package org.w3id.scholarlydata.clodg.dogfood;

import java.util.ArrayList;
import java.util.Collection;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class InProceedingsSet {

	
	private Model model;
	
	public InProceedingsSet(Model model) {
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}
	
	public Collection<InProceedings> list(){
		Collection<InProceedings> inProceedingsList = new ArrayList<InProceedings>();
	
		StmtIterator stmtIterator = model.listStatements(null, RDF.type, ModelFactory.createDefaultModel().createResource("http://swrc.ontoware.org/ontology#InProceedings"));
		while(stmtIterator.hasNext()){
			Statement stmt = stmtIterator.next();
			Resource inProceedingsResource = stmt.getSubject();
			InProceedings inProceedings = new InProceedings(inProceedingsResource);
			inProceedingsList.add(inProceedings);
		}
		return inProceedingsList;
	}
}
