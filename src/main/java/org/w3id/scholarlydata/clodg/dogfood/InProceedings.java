package org.w3id.scholarlydata.clodg.dogfood;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDFS;

public class InProceedings {
	
	private Resource resource;
	private ConferenceEvent conferenceEvent;
	
	public InProceedings(Resource resource) {
		this.resource = resource;
		conferenceEvent = new ConferenceEvent(resource.getModel());
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public String getURI(){
		return resource.getURI();
	}
	
	private void addAuthorList(Resource confInProceedings, Model model){
		
		Resource authorList = model.createResource(confInProceedings.getURI().replace("/inproceedings/", "/authorlist/"), ConferenceOntology.List);
		confInProceedings.addProperty(ConferenceOntology.hasAuthorList, authorList);
		
		String sparql = 
				"SELECT ?member ?pos ?title "
				+ "WHERE { "
				+ "{<" + resource.getURI() + "> <http://www.cs.vu.nl/~mcaklein/onto/swrc_ext/2005/05#authorList> ?authorList} "
				+ "UNION "
				+ "{<" + resource.getURI() + "> <http://purl.org/ontology/bibo/authorList> ?authorList} "
				+ "?authorList ?p ?member . FILTER(REGEX(STR(?p), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#_\")) . "
				+ "BIND(REPLACE(STR(?p), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#_\", \"\") AS ?pos)"
				+ "OPTIONAL { <" + resource.getURI() + "> <" + DC_11.title + "> ?title } "
				+ "} "
				+ "ORDER BY ?pos";
		
		
		Model modelIn = resource.getModel();
		
		ResultSet resultSet = QueryExecutor.execSelect(modelIn, sparql);
		
		//ResultSetFormatter.out(System.out, resultSet);
		
		int itemCounter = 0;
		Resource previousAuthorListItem = null;
		Resource authorListItem = null;
		
		while(resultSet.hasNext()){
			if(authorListItem != null)
				previousAuthorListItem = authorListItem;
			
			QuerySolution querySolution = resultSet.next();
			Resource person = querySolution.getResource("member");
			Literal pos = querySolution.getLiteral("pos");
			Literal titleLiteral = querySolution.getLiteral("title");
			String title = titleLiteral != null ? " of the paper \"" + titleLiteral.getLexicalForm() + "\"": "";
			
			authorListItem = model.createResource(confInProceedings.getURI().replace("/inproceedings/", "/authorlistitem/") + "-item-" + pos.getLexicalForm(), ConferenceOntology.ListItem);
			authorListItem.addProperty(ConferenceOntology.hasContent, ModelFactory.createDefaultModel().createResource(person.getURI().replace("http://data.semanticweb.org/", ConferenceOntology.RESOURCE_NS)));	
			
			authorList.addProperty(ConferenceOntology.hasItem, authorListItem);
			authorList.addLiteral(RDFS.label, "Authors list" + title);
			
			if(itemCounter == 0){
				authorList.addProperty(ConferenceOntology.hasFirstItem, authorListItem);
			}
			else{
				authorListItem.addProperty(ConferenceOntology.previous, previousAuthorListItem);
				previousAuthorListItem.addProperty(ConferenceOntology.next, authorListItem);
			}
			
			itemCounter += 1;
			
		}
		
		if(authorListItem != null)
			authorList.addProperty(ConferenceOntology.hasLastItem, authorListItem);
		
	}
	
	
	/*
	 * Author list props: swrc-ext:authorList, bibo:authorList
	 */
	public Resource asConfResource(Model model){
		String inProcURI = resource.getURI();
		inProcURI = inProcURI.replace("http://data.semanticweb.org/conference/", "");
		int index = inProcURI.indexOf("/");
		index = inProcURI.indexOf("/", index+1);
		
		inProcURI = inProcURI.substring(index+1);
		if(inProcURI.startsWith("workshop/")) {
			inProcURI.replace("workshop/", "");
			index = inProcURI.indexOf("/", index+1);
			inProcURI = inProcURI.substring(index+1);
		}
		
		/*
		inProcURI = inProcURI.replaceAll("paper", "");
		inProcURI = inProcURI.replaceAll(Config.CONF_ACRONYM.toLowerCase(), "");
		inProcURI = inProcURI.replaceAll(Config.YEAR.toLowerCase(), "");
		inProcURI = inProcURI.replaceAll("(\\--)+", "");
		inProcURI = inProcURI.replaceAll("^\\-", "");
		inProcURI = inProcURI.replaceAll("\\-$", "");
		*/
		
		String confAcronym = conferenceEvent.getAcronym();
		confAcronym = confAcronym.toLowerCase().replace(" ", "");
		String localName = confAcronym + "/" + inProcURI;
		
		Resource inProceedings = model.createResource(ConferenceOntology.RESOURCE_NS + "inproceedings/" + inProcURI);
		
		Model modelIn = resource.getModel();
		
		String sparql = 
				"CONSTRUCT {"
				+ "<" + inProceedings.getURI() + "> a <" + ConferenceOntology.InProceedings.getURI() + "> . "
				+ "<" + inProceedings.getURI() + "> <" + RDFS.label + "> ?label . "
				+ "<" + inProceedings.getURI() + "> <" + DC_11.creator + "> ?author . "
				+ "<" + inProceedings.getURI() + "> <" + ConferenceOntology.title + "> ?title . "
				+ "<" + inProceedings.getURI() + "> <" + ConferenceOntology.abstract_ + "> ?abstract . "
				+ "<" + inProceedings.getURI() + "> <" + ConferenceOntology.keyword + "> ?subject . "
				+ "<" + inProceedings.getURI() + "> <" + ConferenceOntology.hasTopic + "> ?topic . "
				+ "<" + inProceedings.getURI() + "> <" + ConferenceOntology.isPartOf + "> ?proceedings . "
				+ "<" + inProceedings.getURI() + "> <" + OWL2.sameAs + "> <" + resource.getURI() + "> . "
				+ "?proceedings <" + ConferenceOntology.hasPart + "> <" + inProceedings.getURI() + "> "
				+ "}"
				+ "WHERE{ "
				+ "<" + resource.getURI() + "> <" + RDFS.label + "> ?label . "
				+ "OPTIONAL{"
				+ "{<" + resource.getURI() + "> <" + DC_11.creator + "> ?creator} UNION {<" + resource.getURI() + "> <" + DCTerms.creator + "> ?creator} . "
				+ "BIND(IRI(REPLACE(STR(?creator), \"http://data.semanticweb.org/\", \"" + ConferenceOntology.RESOURCE_NS + "\")) AS ?author)"
				+ "}"
				+ "OPTIONAL{{<" + resource.getURI() + "> <" + SWC.isPartOf + "> ?proc} UNION {<" + resource.getURI() + "> <" + SWC.NS + "partOf" + "> ?proc} BIND(IRI(REPLACE(STR(?proc), \"http://data.semanticweb.org/\", \"" + ConferenceOntology.RESOURCE_NS + "\")) AS ?proceedings)} "
				+ "{<" + resource.getURI() + "> <" + DC_11.title + "> ?title} UNION {<" + resource.getURI() + "> <" + DCTerms.title + "> ?title} . "
				+ "OPTIONAL {<" + resource.getURI() + "> <http://swrc.ontoware.org/ontology#abstract> ?abstract } "
				+ "OPTIONAL {{<" + resource.getURI() + "> <" + FOAF.topic + "> ?topic} UNION {<" + resource.getURI() + "> <" + SWC.hasTopic + "> ?topic}}"
				+ "OPTIONAL {{<" + resource.getURI() + "> <http://swrc.ontoware.org/ontology#keywords> ?subject} UNION {<" + resource.getURI() + "> <" + DC_11.subject+ "> ?subject} UNION {<" + resource.getURI() + "> <" + DCTerms.subject+ "> ?subject}}"
				+ "}";
		
		model.add(QueryExecutor.execConstruct(modelIn, sparql));
		
		addAuthorList(inProceedings, model);
		
		return inProceedings;
		
	}
	
	
	public static void main(String[] args) {
		Model model = FileManager.get().loadModel("eswc2016_clodg/dogfood.ttl");
		
		String sparql = 
				"SELECT ?member ?pos "
				+ "WHERE { "
				+ "{<http://data.semanticweb.org/conference/eswc/2016/paper/applications/application-10> <http://www.cs.vu.nl/~mcaklein/onto/swrc_ext/2005/05#authorList> ?authorList} "
				+ "UNION "
				+ "{<http://data.semanticweb.org/conference/eswc/2016/paper/applications/application-10> <http://purl.org/ontology/bibo/authorList> ?authorList} "
				+ "?authorList ?p ?member . FILTER(REGEX(STR(?p), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#_\")) . "
				+ "BIND(REPLACE(STR(?p), \"http://www.w3.org/1999/02/22-rdf-syntax-ns#_\", \"\") AS ?pos)" 
				+ "} "
				+ "ORDER BY ?pos";
		
		
		ResultSet resultSet = QueryExecutor.execSelect(model, sparql);
		

		ResultSetFormatter.out(System.out, resultSet);
	}
	
	
	
}
