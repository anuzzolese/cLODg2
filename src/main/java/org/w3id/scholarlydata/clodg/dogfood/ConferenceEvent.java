package org.w3id.scholarlydata.clodg.dogfood;

import org.w3id.scholarlydata.clodg.Config;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ConferenceEvent {
	
	private Resource resource;
	private Literal acronym;
	
	private static Resource conference;
	
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
	
	public Resource asConfResource(Model model){
		if(conference == null){
			String confLocalName = Config.CONF_ACRONYM.toLowerCase() + Config.YEAR;
			String confUri = ConferenceOntology.RESOURCE_NS + "conference/" + confLocalName;
			String confSeriesUri = ConferenceOntology.RESOURCE_NS + "/conferenceseries/" + Config.CONF_ACRONYM.toLowerCase();
			String sparql = 
					"PREFIX rdfs: <" + RDFS.getURI() + "> "
					+ "PREFIX conf: <" + ConferenceOntology.NS + "> "
					+ "PREFIX swc: <http://data.semanticweb.org/ns/swc/ontology#> "
					+ "PREFIX icaltzd: <http://www.w3.org/2002/12/cal/icaltzd#> "
					+ "CONSTRUCT {"
					+ "	<" + confUri + "> a <" + ConferenceOntology.Conference.getURI() + "> . "
					+ "	<" + confUri + "> rdfs:label ?label . "
					+ "	<" + confUri + "> conf:acronym ?acronym . "
					+ "	<" + confUri + "> icaltzd:dtstart ?dtstart . "
					+ "	<" + confUri + "> icaltzd:dtend ?dtend . "
					+ "	<" + confUri + "> icaltzd:location ?location . "
					+ "	<" + confUri + "> conf:hasSeries  <" + confSeriesUri + "> . "
					+ "	<" + confSeriesUri + "> conf:isSeriesOf  <" + confUri + "> . "
					+ "	<" + confSeriesUri + "> rdfs:label  \"" + Config.CONF_ACRONYM + "\" "
					+ "} "
					+ "WHERE { "
					+ "	<" + resource.getURI() + "> rdfs:label ?label . "
					+ "	<" + resource.getURI() + "> swc:hasAcronym ?acronym . "
					+ "	<" + resource.getURI() + "> icaltzd:dtstart ?dtstart . "
					+ "	<" + resource.getURI() + "> icaltzd:dtend ?dtend . "
					+ "	<" + resource.getURI() + "> icaltzd:location ?location "
					+ "}";
			
			Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
			QueryExecution queryExecution = QueryExecutionFactory.create(query, resource.getModel());
			Model tmp = queryExecution.execConstruct();
			model.add(tmp);
			
			conference = model.createResource(confUri);
		}
		return conference;
	}

}
