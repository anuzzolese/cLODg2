package org.w3id.scholarlydata.clodg.dogfood;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Event {

	private Resource swdfEvent;
	private Resource confEvent;
	
	public Event(Resource swdfEvent, String confAcronym) {
		this.swdfEvent = swdfEvent;
		
		String eventURI = ConferenceOntology.RESOURCE_NS + "event/" + confAcronym + "/";
		
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
				+ "<" + confEvent.getURI() + "> <" + OWL2.sameAs + "> <" + swdfEvent.getURI() + "> "
				+ "}"
				+ "WHERE{ "
				+ "OPTIONAL{<" + swdfEvent.getURI() + "> <" + RDFS.label + "> ?label } "
				+ "OPTIONAL {<" + swdfEvent.getURI() + "> <http://www.w3.org/2002/12/cal/icaltzd#dtstart> ?start} "
				+ "OPTIONAL {<" + swdfEvent.getURI() + "> <http://www.w3.org/2002/12/cal/icaltzd#dtend> ?end} "
				+ "OPTIONAL {<" + swdfEvent.getURI() + "> <http://www.w3.org/2002/12/cal/icaltzd#description> ?description}"
				+ "OPTIONAL {<" + swdfEvent.getURI() + "> a ?eventType} "
				+ "BIND(cofunc:eventTypeBind(?eventType) AS ?confEventType) "
				+ "}";
		
		try{
			model.add(QueryExecutor.execConstruct(modelIn, sparql));
		} catch (Exception e){
			System.out.println("Stocazzo " + sparql);
		}
		
		return confEvent;
		
	}
	
}
