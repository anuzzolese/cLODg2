package org.w3id.scholarlydata.clodg.dogfood;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3id.scholarlydata.clodg.dogfood.arq.EventTypeBinder;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Events {

	private Model model;
	private Model swdfOnt;
	private ConferenceEvent conferenceEvent;
	private Map<Resource, Resource> swdf2confMapping;
	
	public static int anonEventCounter;
	
	private static final String SWC_ONTOLOGY = "ontologies/swc_2009-05-09.rdf";
	
	public Events(Model model) {
		this.model = model;
		this.swdfOnt = ModelFactory.createDefaultModel().read(getClass().getClassLoader().getResourceAsStream(SWC_ONTOLOGY), null, "RDF/XML");
		//this.swdfOnt = FileManager.get().loadModel("ontologies/swc_2009-05-09.rdf");
		this.conferenceEvent = new ConferenceEvent(model);
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
				+ "FILTER(?type != <" + SWC.TalkEvent.getURI() + ">)"
				+ "}";
		
		Model unionModel = ModelFactory.createDefaultModel();
		unionModel.add(model);
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
					subEvent = new Event(sub, "conference");
				else if(subType != null && subType.equals(SWC.TalkEvent)) subEvent = new Talk(sub);
				else {
					Node nodeType = EventTypeBinder.getBinding(eventTypeRes.asNode());
					subEvent = new Event(sub, nodeType.getLocalName().toLowerCase());
				}
			}
			if(superE != null) {
				if(superE.getURI().equals(conferenceEvent.getResource().getURI()))
					superEvent = new Event(superE, "conference");
				else if(superEType != null && superEType.equals(SWC.TalkEvent)) superEvent = new Talk(superE);
				else {
					Node nodeType = EventTypeBinder.getBinding(eventTypeRes.asNode());
					subEvent = new Event(superE, nodeType.getLocalName().toLowerCase());
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
					subEvent = new Event(sub, "conference");
				else subEvent = new Event(sub, "event");
			}
			if(superE != null) {
				if(superE.getURI().equals(conferenceEvent.getResource().getURI()))
					superEvent = new Event(superE, "conference");
				else superEvent = new Event(superE, "event");
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
