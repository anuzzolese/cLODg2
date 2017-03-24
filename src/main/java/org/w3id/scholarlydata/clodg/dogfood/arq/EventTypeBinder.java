package org.w3id.scholarlydata.clodg.dogfood.arq;

import org.w3id.scholarlydata.clodg.dogfood.ConferenceOntology;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class EventTypeBinder extends FunctionBase1 {

	public static final String IRI = ConferenceOntology.NS + "eventTypeBind";
	
	@Override
	public NodeValue exec(NodeValue nodeValue) {
		if(nodeValue == null)
			return NodeValue.makeNode(ConferenceOntology.OrganisedEvent.asNode());
		else if(nodeValue.isIRI()){
			return NodeValue.makeNode(getBinding(nodeValue.asNode()));
		}
		else return NodeValue.makeNode(ConferenceOntology.OrganisedEvent.asNode());
	}
	
	public static Node getBinding(Node node){
		String localNameEventEnding = node.getLocalName();
		String localNameNotEventEnding = localNameEventEnding.replaceAll("Event$", "");
		
		Model ontology = ConferenceOntology.getModel();
		
		Resource termEventEnding = ModelFactory.createDefaultModel().createResource(ConferenceOntology.NS + localNameEventEnding);
		Resource termNotEventEnding = ModelFactory.createDefaultModel().createResource(ConferenceOntology.NS + localNameNotEventEnding);
		
		if(ontology.containsResource(termEventEnding))
			return termEventEnding.asNode();
		else if(ontology.containsResource(termNotEventEnding)) return termNotEventEnding.asNode();
		else return ConferenceOntology.OrganisedEvent.asNode();
	}

}
