package org.w3id.scholarlydata.clodg.dogfood;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;

public class People {

	private Model model;
	
	public People(Model model) {
		this.model = model;
	}
	
	public Collection<Person> list(){
		//Collection<Person> personList = new ArrayList<Person>();
	
		Stream<Statement> stmtStream = model.listStatements(null, RDF.type, FOAF.Person).toList().stream();
		Stream<Resource> stmtResource = stmtStream.map(stmt -> {
			Resource personResource = stmt.getSubject();
			return personResource;
		}).distinct();
		
		return stmtResource.map(resource -> {
			return new Person(resource);
		}).collect(Collectors.toList());
		
		/*
		while(stmtIterator.hasNext()){
			Statement stmt = stmtIterator.next();
			Resource personResource = stmt.getSubject();
			Person person = new Person(personResource);
			personList.add(person);
		}
		return personList;
		*/
	}
	
}
