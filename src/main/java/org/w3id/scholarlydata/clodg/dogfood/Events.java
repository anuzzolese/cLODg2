package org.w3id.scholarlydata.clodg.dogfood;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3id.scholarlydata.clodg.dogfood.arq.EventTypeBinder;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Events {

	private Model modelIn;
	private Model swdfOnt;
	private ConferenceEvent conferenceEvent;
	private Map<Resource, Resource> swdf2confMapping;
	
	public static int anonEventCounter;
	
	private static final String SWC_ONTOLOGY = "ontologies/swc_2009-05-09.rdf";
	
	private final String sparqlForEventTypes = 
			"PREFIX rdfs: <" + RDFS.getURI() + "> "
			+ "SELECT ?type1 "
			+ "WHERE { "
			+ "	%ent% a ?type1 . "
			+ " ?type2 rdfs:subClassOf* ?type1 . "
			+ "	FILTER(?type1 != <http://www.w3.org/2002/12/cal/icaltzd#Vevent>) "
			+ "}";
	
	public Events(Model modelIn) {
		
		this.modelIn = modelIn;
		this.swdfOnt = ModelFactory.createDefaultModel().read(getClass().getClassLoader().getResourceAsStream(SWC_ONTOLOGY), null, "RDF/XML");
		this.conferenceEvent = ConferenceEvent.getInstance(modelIn);
		this.swdf2confMapping = new HashMap<Resource, Resource>();
		
		anonEventCounter = 0;
	}
	
	public Collection<Event> list(){
		
		Collection<Event> events = new ArrayList<Event>();
		
		String sparql = "SELECT DISTINCT ?event ?type ?super ?supertype ?sub ?subtype "
				+ "WHERE { "
				+ "{"
				+ "		?event <" + SWC.isSuperEventOf + "> ?sub "
				+ "		OPTIONAL{?sub a ?subtype . FILTER(?subtype != <http://www.w3.org/2002/12/cal/icaltzd#Vevent>)}"
				+ "} UNION "
				+ "{"
				+ "		?event <" + SWC.isSubEventOf + "> ?super "
				+ "		OPTIONAL{?super a ?supertype . FILTER(?supertype != <http://www.w3.org/2002/12/cal/icaltzd#Vevent>)}"
				+ "} "
				+ "?event <" + RDF.type + "> ?type . "
				+ "?type <" + RDFS.subClassOf + ">+ <" + SWC.OrganisedEvent + "> "
				+ "FILTER(?type != <" + SWC.TalkEvent.getURI() + ">) "
				+ "FILTER(?type != <" + SWC.ConferenceEvent.getURI() + ">) "
				+ "}";
		
		Model unionModel = ModelFactory.createDefaultModel();
		unionModel.add(modelIn);
		unionModel.add(swdfOnt);
		
		
		ResultSet resultSet = QueryExecutor.execSelect(unionModel, sparql);
		
		String confAcronym = conferenceEvent.getAcronym();
		confAcronym = confAcronym.toLowerCase().replaceAll(" ", "");
		
		while(resultSet.hasNext()){
			QuerySolution querySolution = resultSet.next();
			Resource eventRes = querySolution.getResource("event");
			Resource eventTypeRes = querySolution.getResource("type");
			Resource sub = querySolution.getResource("sub");
			Resource subType = querySolution.getResource("subtype");
			Resource superE = querySolution.getResource("super");
			Resource superEType = querySolution.getResource("supertype");
			
			if(eventTypeRes == null) eventTypeRes = ConferenceOntology.OrganisedEvent;
			
			Event subEvent = null;
			Event superEvent = null;
			if(sub != null) {
				
				if(sub.getURI().equals(conferenceEvent.getResource().getURI()))	
					subEvent = conferenceEvent;
				else if(subType != null && subType.equals(SWC.TalkEvent)) subEvent = new Talk(sub);
				else {
					Node nodeType = EventTypeBinder.getBinding(subType.asNode());
					subEvent = new Event(sub, nodeType.getLocalName().toLowerCase());
				}
			}
			if(superE != null) {
				if(superE.getURI().equals(conferenceEvent.getResource().getURI()))
					superEvent = conferenceEvent;
				else if(superEType != null && superEType.equals(SWC.TalkEvent)) superEvent = new Talk(superE);
				else {
					Node nodeType = EventTypeBinder.getBinding(superEType.asNode());
					superEvent = new Event(superE, nodeType.getLocalName().toLowerCase());
				}
			}
			Node eventTypeNode = EventTypeBinder.getBinding(eventTypeRes.asNode());
			
			String eventTypeLabel;
			if(eventTypeNode != null) eventTypeLabel = eventTypeNode.getLocalName().toLowerCase();
			else eventTypeLabel = ConferenceOntology.OrganisedEvent.getLocalName().toLowerCase();
			Event event = new Event(eventRes, eventTypeLabel, subEvent, superEvent);
			swdf2confMapping.put(eventRes, event.asConfResource());
			
			events.add(event);
				
		}
		
		sparql = "SELECT DISTINCT ?talk ?super ?sub "
				+ "WHERE { "
				+ "{?talk <" + SWC.isSuperEventOf + "> ?sub} UNION "
				+ "{?talk <" + SWC.isSubEventOf + "> ?super} "
				+ "?talk <" + RDF.type + "> <" + SWC.TalkEvent.getURI() + ">"
				+ "}";
		
		resultSet = QueryExecutor.execSelect(unionModel, sparql);
		
		
		while(resultSet.hasNext()){
			QuerySolution querySolution = resultSet.next();
			Resource talkRes = querySolution.getResource("talk");
			Resource sub = querySolution.getResource("sub");
			Resource superE = querySolution.getResource("super");
			
			Event subEvent = null;
			Event superEvent = null;
			if(sub != null) {
				if(sub.getURI().equals(conferenceEvent.getResource().getURI()))	
					subEvent = conferenceEvent;
				else subEvent = new Event(sub, "event");
			}
			if(superE != null) {
				String sparqlFET = sparqlForEventTypes.replaceAll("\\%ent\\%", "<" + superE.getURI() + ">");
				Query query = QueryFactory.create(sparqlFET, Syntax.syntaxARQ);
				QueryExecution queryExecution = QueryExecutionFactory.create(query, ConferenceOntology.getModel().union(modelIn));
				ResultSet rs = queryExecution.execSelect();
				
				while(rs.hasNext()){
					QuerySolution qs = rs.next();
					Resource type = qs.getResource("type1");
					
					String typeLabel = type.getURI().replace(SWC.NS, "").replaceAll("Event$", "").toLowerCase();
					if(superE.getURI().equals(conferenceEvent.getResource().getURI()))
						subEvent = conferenceEvent;
					else superEvent = new Event(superE, typeLabel);
				}
				
				
			}
			
			Talk talk = new Talk(talkRes, subEvent, superEvent);
			swdf2confMapping.put(talkRes, talk.asConfResource());
			
			events.add(talk);
				
		}
		
		return events;
	}
	
	public Map<Resource, Resource> getSwdf2confMapping() {
		return swdf2confMapping;
	}
	
}
