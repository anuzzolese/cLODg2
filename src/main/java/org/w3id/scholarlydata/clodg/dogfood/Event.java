package org.w3id.scholarlydata.clodg.dogfood;

import org.w3id.scholarlydata.clodg.Config;
import org.w3id.scholarlydata.clodg.Urifier;
import org.w3id.scholarlydata.clodg.dogfood.arq.EventTypeBinder;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Event {

	protected Resource swdfEvent;
	protected Resource confEvent;
	
	protected Event subEvent, superEvent;
	
	public Event() {
		
	}
	
	public Event(Resource swdfEvent, String type) {
		this.swdfEvent = swdfEvent;
		
		//String eventURI = ConferenceOntology.RESOURCE_NS + "/event/" + Config.CONF_ACRONYM.toLowerCase() + Config.YEAR + "/";
		String eventURI = ConferenceOntology.RESOURCE_NS;
		
		if(swdfEvent.isURIResource()){
			String eventResURI = swdfEvent.getURI();
			String localName = null;
			if(eventResURI.startsWith("http://data.semanticweb.org/")){
				localName = eventResURI.replace("http://data.semanticweb.org/", "").replace("conference/", "").replace("workshop/", "");
				//localName = localName.replace(Config.CONF_ACRONYM.toLowerCase() + "/" + Config, newChar)
				eventURI += type + "/" + Urifier.toURI(localName);   
			}
			else{
				Events.anonEventCounter += 1;
				eventURI += "event/event" + Events.anonEventCounter;
			}
		}
		else{
			Events.anonEventCounter += 1;
			eventURI += "event/event" + Events.anonEventCounter;
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
				+ "<" + confEvent.getURI() + "> <" + ConferenceOntology.name + "> ?name . "
				+ "<" + confEvent.getURI() + "> <" + ConferenceOntology.startDate + "> ?start . "
				+ "<" + confEvent.getURI() + "> <" + ConferenceOntology.endDate + "> ?end . "
				+ "<" + confEvent.getURI() + "> <" + ConferenceOntology.description + "> ?description . "
				+ "<" + confEvent.getURI() + "> <" + RDFS.label + "> ?description . "
				+ "<" + confEvent.getURI() + "> <" + OWL2.sameAs + "> <" + swdfEvent.getURI() + "> "
				+ "}"
				+ "WHERE{ "
				+ "<" + swdfEvent.getURI() + "> a ?eventType . "
				+ "OPTIONAL{<" + swdfEvent.getURI() + "> <" + RDFS.label + "> ?label } "
				+ "OPTIONAL {<" + swdfEvent.getURI() + "> <http://www.w3.org/2002/12/cal/icaltzd#dtstart> ?start} "
				+ "OPTIONAL {<" + swdfEvent.getURI() + "> <http://www.w3.org/2002/12/cal/icaltzd#dtend> ?end} "
				+ "OPTIONAL {<" + swdfEvent.getURI() + "> <http://www.w3.org/2002/12/cal/icaltzd#description> ?description} "
				+ "BIND(cofunc:eventTypeBind(?eventType) AS ?confEventType) "
				+ "}";
		
		try{
			
			model.add(QueryExecutor.execConstruct(modelIn, sparql));
			
			if(subEvent != null) {
				
				Resource subEventResource = subEvent.asConfResource();
				
				if(subEventResource.getURI().equals(ConferenceOntology.RESOURCE_NS + "event/" + Config.CONF_ACRONYM.toLowerCase() + "/" + Config.YEAR))
					subEventResource = ResourceFactory.createResource(ConferenceOntology.RESOURCE_NS + "conference/" + Config.CONF_ACRONYM.toLowerCase() + Config.YEAR);
				
				Resource eventResource = confEvent;
				if(eventResource.getURI().equals(ConferenceOntology.RESOURCE_NS + "event/" + Config.CONF_ACRONYM.toLowerCase() + "/" + Config.YEAR))
					eventResource = ResourceFactory.createResource(ConferenceOntology.RESOURCE_NS + "conference/" + Config.CONF_ACRONYM.toLowerCase() + Config.YEAR);
				
				model.add(eventResource, ConferenceOntology.hasSubEvent, subEventResource);
				model.add(subEventResource, ConferenceOntology.isSubEventOf, eventResource);
			}
			
			if(superEvent != null) {
				Resource superEventResource = superEvent.confEvent;
				if(superEventResource.getURI().equals(ConferenceOntology.RESOURCE_NS + "event/" + Config.CONF_ACRONYM.toLowerCase() + "/" + Config.YEAR))
					superEventResource = ResourceFactory.createResource(ConferenceOntology.RESOURCE_NS + "conference/" + Config.CONF_ACRONYM.toLowerCase() + Config.YEAR);
				
				Resource eventResource = confEvent;
				if(eventResource.getURI().equals(ConferenceOntology.RESOURCE_NS + "event/" + Config.CONF_ACRONYM.toLowerCase() + "/" + Config.YEAR))
					eventResource = ResourceFactory.createResource(ConferenceOntology.RESOURCE_NS + "conference/" + Config.CONF_ACRONYM.toLowerCase() + Config.YEAR);
				
				model.add(eventResource, ConferenceOntology.isSubEventOf, superEventResource);
				model.add(superEventResource, ConferenceOntology.hasSubEvent, eventResource);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return confEvent;
		
	}
	
}
