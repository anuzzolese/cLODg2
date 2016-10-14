package org.w3id.scholarlydata.clodg.dogfood;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;

public class Organisations {

	private Model model;
	public Organisations(Model model) {
		this.model = model;
	}
	
	public Collection<Organisation> list(){
		Stream<Statement> stmtStream = model.listStatements(null, RDF.type, FOAF.Organization)
				.toList()
				.stream();
		
		Stream<Resource> resourceStream = stmtStream.map(stmt -> {
			return stmt.getSubject();
		}).distinct();
		
		return resourceStream.map(resource -> {
			return new Organisation(resource);
		}).collect(Collectors.toList());
	}
}
