package org.w3id.scholarlydata.clodg.dogfood.arq;

import org.w3id.scholarlydata.clodg.dogfood.ConferenceOntology;

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
			String localName = nodeValue.asNode().getLocalName();
			
			Model ontology = ConferenceOntology.getModel();
			
			Resource term = ModelFactory.createDefaultModel().createResource(ConferenceOntology.NS + localName);
			if(ontology.containsResource(term))
				return NodeValue.makeNode(term.asNode());
			else return NodeValue.makeNode(ConferenceOntology.OrganisedEvent.asNode());
			
		}
		else return NodeValue.makeNode(ConferenceOntology.OrganisedEvent.asNode());
	}

}
