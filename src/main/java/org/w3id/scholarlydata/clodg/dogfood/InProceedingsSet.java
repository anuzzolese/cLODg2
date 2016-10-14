package org.w3id.scholarlydata.clodg.dogfood;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		//Collection<InProceedings> inProceedingsList = new ArrayList<InProceedings>();
	
		StmtIterator stmtIterator = model.listStatements(null, RDF.type, ModelFactory.createDefaultModel().createResource("http://swrc.ontoware.org/ontology#InProceedings"));
		Stream<Statement> stmtStream = stmtIterator.toList().stream();
		
		Stream<Resource> resourceStream = stmtStream.map(stmt -> {
			Resource inProceedingsResource = stmt.getSubject();
			return inProceedingsResource;
		});
		
		resourceStream = resourceStream.distinct();
		Stream<InProceedings> inProcStream = resourceStream.map(resource -> {
			InProceedings inProceedings = new InProceedings(resource);
			return inProceedings;
		});
		
		
		
		return inProcStream.collect(Collectors.toList());
		
		/*
		while(stmtIterator.hasNext()){
			Statement stmt = stmtIterator.next();
			Resource inProceedingsResource = stmt.getSubject();
			InProceedings inProceedings = new InProceedings(inProceedingsResource);
			inProceedingsList.add(inProceedings);
		}
		return inProceedingsList;
		*/
	}
}
