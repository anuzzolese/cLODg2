package org.w3id.scholarlydata.clodg.dogfood;

import java.util.ArrayList;
import java.util.Collection;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;

public class Organisations {

	private Model model;
	public Organisations(Model model) {
		this.model = model;
	}
	
	public Collection<Organisation> list(){
		Collection<Organisation> organisationList = new ArrayList<Organisation>();
	
		StmtIterator stmtIterator = model.listStatements(null, RDF.type, FOAF.Organization);
		while(stmtIterator.hasNext()){
			Statement stmt = stmtIterator.next();
			Resource organisationResource = stmt.getSubject();
			Organisation organisation = new Organisation(organisationResource);
			organisationList.add(organisation);
		}
		return organisationList;
	}
}
