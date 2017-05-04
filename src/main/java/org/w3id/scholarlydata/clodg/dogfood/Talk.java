package org.w3id.scholarlydata.clodg.dogfood;

import org.w3id.scholarlydata.clodg.Config;
import org.w3id.scholarlydata.clodg.Urifier;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Talk extends Event {

	public Talk(Resource swdfEvent) {
		super.swdfEvent = swdfEvent;
		
		//String eventURI = ConferenceOntology.RESOURCE_NS + "/event/" + Config.CONF_ACRONYM.toLowerCase() + Config.YEAR + "/";
		String eventURI = ConferenceOntology.RESOURCE_NS + "talk/";
		
		if(swdfEvent.isURIResource()){
			String eventResURI = swdfEvent.getURI();
			String localName = null;
			if(eventResURI.startsWith("http://data.semanticweb.org/")){
				localName = eventResURI.replace("http://data.semanticweb.org/", "").replace("conference/", "").replace("workshop/", "").replace("talk/", "");
				eventURI += Urifier.toURI(localName);   
			}
			else{
				Events.anonEventCounter += 1;
				eventURI += "talk-" + Events.anonEventCounter;
			}
		}
		else{
			Events.anonEventCounter += 1;
			eventURI += "talk-" + Events.anonEventCounter;
		}
		
		super.confEvent = ModelFactory.createDefaultModel().createResource(eventURI);
	}
	
	public Talk(Resource swdfEvent, Event subEvent, Event superEvent) {
		this(swdfEvent);
		
		super.subEvent = subEvent;
		super.superEvent = superEvent;
		
	}
	
	@Override
	public Resource asConfResource(Model model){
		Model modelIn = super.swdfEvent.getModel();
		
		
		
		String sparql = "PREFIX cofunc: <" + ConferenceOntology.NS + "> "
				+ "CONSTRUCT {"
				+ "<" + confEvent.getURI() + "> <" + ConferenceOntology.name + "> ?name . "
				+ "<" + confEvent.getURI() + "> <" + ConferenceOntology.startDate + "> ?start . "
				+ "<" + confEvent.getURI() + "> <" + ConferenceOntology.endDate + "> ?end . "
				+ "<" + confEvent.getURI() + "> <" + ConferenceOntology.description + "> ?description . "
				+ "<" + confEvent.getURI() + "> <" + RDFS.label + "> ?description . "
				//+ "<" + confEvent.getURI() + "> <" + OWL2.sameAs + "> <" + swdfEvent.getURI() + "> . "
				+ "?paperIRI <" + ConferenceOntology.relatesToEvent + "> <" + confEvent.getURI() + "> . "
				+ "<" + confEvent.getURI() + "> <" + ConferenceOntology.isEventRelatedTo + "> ?paperIRI . "
				+ "<" + confEvent.getURI() + "> a ?talkType "
				+ "}"
				+ "WHERE{ "
				+ "OPTIONAL{<" + swdfEvent.getURI() + "> <" + RDFS.label + "> ?label } "
				+ "OPTIONAL {<" + swdfEvent.getURI() + "> <http://www.w3.org/2002/12/cal/icaltzd#dtstart> ?start} "
				+ "OPTIONAL {<" + swdfEvent.getURI() + "> <http://www.w3.org/2002/12/cal/icaltzd#dtend> ?end} "
				+ "OPTIONAL {<" + swdfEvent.getURI() + "> <http://www.w3.org/2002/12/cal/icaltzd#description> ?description}"
				+ "?paper <" + SWC.relatedToEvent + "> <" + swdfEvent.getURI() + "> . "
				+ "?paper <" + DC_11.title + "> ?title . "
				+ "BIND(IRI(REPLACE(STR(?paper), 'http://data.semanticweb.org/conference/', 'https://w3id.org/scholarlydata/inproceedings/')) AS ?paperIRI) "
				+ "BIND(IRI('" + ConferenceOntology.NS + "Talk') AS ?talkType) "
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
				Resource superEventResource = superEvent.asConfResource();
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
