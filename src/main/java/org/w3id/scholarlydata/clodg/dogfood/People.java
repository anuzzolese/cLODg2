package org.w3id.scholarlydata.clodg.dogfood;

import java.util.ArrayList;
import java.util.Collection;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;

public class People {

	private Model model;
	
	public People(Model model) {
		this.model = model;
	}
	
	public Collection<Person> list(){
		Collection<Person> personList = new ArrayList<Person>();
	
		StmtIterator stmtIterator = model.listStatements(null, RDF.type, FOAF.Person);
		while(stmtIterator.hasNext()){
			Statement stmt = stmtIterator.next();
			Resource personResource = stmt.getSubject();
			Person person = new Person(personResource);
			personList.add(person);
		}
		return personList;
	}
	
}
