package org.w3id.scholarlydata.clodg.dogfood;

import org.w3id.scholarlydata.clodg.Config;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Event {

	private Resource swdfEvent;
	private Resource confEvent;
	
	private Event subEvent, superEvent;
	
	public Event(Resource swdfEvent, String type) {
		this.swdfEvent = swdfEvent;
		
		//String eventURI = ConferenceOntology.RESOURCE_NS + "/event/" + Config.CONF_ACRONYM.toLowerCase() + Config.YEAR + "/";
		String eventURI = ConferenceOntology.RESOURCE_NS + "event/";
		
		if(swdfEvent.isURIResource()){
			String eventResURI = swdfEvent.getURI();
			String localName = null;
			if(eventResURI.startsWith("http://data.semanticweb.org/")){
				localName = eventResURI.replace("http://data.semanticweb.org/", "").replace("conference/", "").replace("workshop/", "");
				eventURI += localName;   
			}
			else{
				Events.anonEventCounter += 1;
				eventURI += "event" + Events.anonEventCounter;
			}
		}
		else{
			Events.anonEventCounter += 1;
			eventURI += "event" + Events.anonEventCounter;
		}
		
		this.confEvent = ModelFactory.createDefaultModel().createResource(eventURI);
	}
	
	public Event(Resource swdfEvent, String type, Event subEvent, Event superEvent) {
		this(swdfEvent, type);
		
		this.subEvent = subEvent;
		this.superEvent = superEvent;
		
	}
	
	public Resource getSwdfEvent() {
		return swdfEvent;
	}
	
	public Resource asConfResource(){
		return confEvent;
	}
	
	public Resource asConfResource(Model model){
		Model modelIn = swdfEvent.getModel();
		
		
		
		String sparql = "PREFIX cofunc: <" + ConferenceOntology.NS + "> "
				+ "CONSTRUCT {"
				+ "<" + confEvent.getURI() + "> a ?confEventType . "
				+ "<" + confEvent.getURI() + "> <" + RDFS.label + "> ?label . "
				+ "<" + confEvent.getURI() + "> <" + ConferenceOntology.name + "> ?name . "
				+ "<" + confEvent.getURI() + "> <" + ConferenceOntology.startDate + "> ?start . "
				+ "<" + confEvent.getURI() + "> <" + ConferenceOntology.endDate + "> ?end . "
				+ "<" + confEvent.getURI() + "> <" + ConferenceOntology.description + "> ?description . "
				+ "<" + confEvent.getURI() + "> <" + OWL2.sameAs + "> <" + swdfEvent.getURI() + "> . "
				+ "?paperIRI <" + ConferenceOntology.relatesToEvent + "> <" + confEvent.getURI() + "> . "
				+ "<" + confEvent.getURI() + "> <" + ConferenceOntology.isEventRelatedTo + "> ?paperIRI . "
				+ "<" + confEvent.getURI() + "> a ?talkType "
				+ "}"
				+ "WHERE{ "
				+ "OPTIONAL{<" + swdfEvent.getURI() + "> <" + RDFS.label + "> ?label } "
				+ "OPTIONAL {<" + swdfEvent.getURI() + "> <http://www.w3.org/2002/12/cal/icaltzd#dtstart> ?start} "
				+ "OPTIONAL {<" + swdfEvent.getURI() + "> <http://www.w3.org/2002/12/cal/icaltzd#dtend> ?end} "
				+ "OPTIONAL {<" + swdfEvent.getURI() + "> <http://www.w3.org/2002/12/cal/icaltzd#description> ?description}"
				+ "OPTIONAL {<" + swdfEvent.getURI() + "> <http://www.w3.org/2002/12/cal/icaltzd#description> ?description}"
				+ "OPTIONAL {?paper <" + SWC.relatedToEvent + "> <" + swdfEvent.getURI() + "> . "
				+ "BIND(IRI(REPLACE(STR(?paper), 'http://data.semanticweb.org/conference/', 'https://w3id.org/scholarlydata/inproceedings/')) AS ?paperIRI) "
				+ "BIND(IRI('" + ConferenceOntology.NS + "Talk') AS ?talkType) } "
				+ "BIND(cofunc:eventTypeBind(?eventType) AS ?confEventType) "
				+ "}";
		
		try{
			model.add(QueryExecutor.execConstruct(modelIn, sparql));
			
			if(subEvent != null) {
				model.add(confEvent, ConferenceOntology.hasSubEvent, subEvent.confEvent);
				model.add(subEvent.confEvent, ConferenceOntology.isSubEventOf, confEvent);
			}
			
			if(superEvent != null) {
				model.add(confEvent, ConferenceOntology.isSubEventOf, superEvent.confEvent);
				model.add(superEvent.confEvent, ConferenceOntology.hasSubEvent, confEvent);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return confEvent;
		
	}
	
}
