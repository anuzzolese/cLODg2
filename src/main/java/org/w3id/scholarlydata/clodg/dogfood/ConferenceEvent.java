package org.w3id.scholarlydata.clodg.dogfood;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class ConferenceEvent {
	
	private Resource resource;
	private Literal acronym;
	public ConferenceEvent(Model model) {
		
		
		String sparql = "PREFIX swc: <" + SWC.NS + "> "
				+ "select ?conference ?acronym where {?conference a <" + SWC.ConferenceEvent.getURI() + "> . ?conference swc:hasAcronym ?acronym }";
		
		ResultSet rs = QueryExecutor.execSelect(model, sparql);
		if(rs.hasNext()){
			QuerySolution querySolution = rs.next();
			resource = querySolution.getResource("conference");
			acronym = querySolution.getLiteral("acronym");
		}
		/*
		ResIterator resIt = model.listResourcesWithProperty(RDF.type, SWC.ConferenceEvent);
		//ResIterator resIt = model.listResourcesWithProperty(RDF.type, SWC.WorkshopEvent);
		if(resIt.hasNext()){
			resource = resIt.next();
		}
		*/
	}
	
	public String getAcronym(){
		//return resource.getProperty(ModelFactory.createDefaultModel().createProperty(SWC.NS + "hasAcronym")).getObject().toString();
		return acronym.getLexicalForm();
	}
	
	public Resource getResource() {
		return resource;
	}

}
