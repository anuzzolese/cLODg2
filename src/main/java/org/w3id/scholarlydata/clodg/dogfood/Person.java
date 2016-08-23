package org.w3id.scholarlydata.clodg.dogfood;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Person {

	private Resource resource;
	private ConferenceEvent conferenceEvent;
	
	public Person(Resource resource) {
		this.resource = resource;
		conferenceEvent = new ConferenceEvent(resource.getModel());
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public String getURI(){
		return resource.getURI();
	}
	
	public String getFamilyName(){
		Statement stmt = resource.getProperty(ModelFactory.createDefaultModel().createProperty("http://xmlns.com/foaf/0.1/lastName"));
		return ((Literal)stmt.getObject()).getLexicalForm();
	}
	
	public String getFirstName(){
		Statement stmt = resource.getProperty(FOAF.firstName);
		return ((Literal)stmt.getObject()).getLexicalForm();
	}
	
	public String getName(){
		Statement stmt = resource.getProperty(FOAF.name);
		return ((Literal)stmt.getObject()).getLexicalForm();
	}
	
	public Set<Role> holdsRole(){
		Set<Role> roles = new HashSet<Role>();
		StmtIterator iterator = resource.listProperties(ModelFactory.createDefaultModel().createProperty("http://data.semanticweb.org/ns/swc/ontology#holdsRole"));
		while(iterator.hasNext()){
			Statement stmt = iterator.next();
			Resource roleResource = ((Resource)stmt.getObject());
			roles.add(new Role(roleResource));
		}
		
		return roles;
	}
	
	public Set<Organisation> swdfAffiliations(){
		Set<Organisation> orgs = new HashSet<Organisation>();
		StmtIterator iterator = resource.listProperties(ModelFactory.createDefaultModel().createProperty("http://swrc.ontoware.org/ontology#affiliation"));
		while(iterator.hasNext()){
			Statement stmt = iterator.next();
			Resource organisationResource = ((Resource)stmt.getObject());
			orgs.add(new Organisation(organisationResource));
		}
		
		return orgs;
	}
	
	public Set<Resource> createRoles(Model model){
		Set<Resource> confRoles = new HashSet<Resource>();
		Set<Role> roles = holdsRole();
		
		
		String conferenceAcronym = conferenceEvent.getAcronym();
		conferenceAcronym = conferenceAcronym.toLowerCase().replace(" ", "");
		String roleNS = ConferenceOntology.RESOURCE_NS + "role-during-event/" + conferenceAcronym;
		for(Role role : roles){
			System.out.println("Role " + role.getResource());
			Resource swdfRole = role.getResource();
			if(swdfRole != null){
				Statement stmt = swdfRole.getProperty(SWC.isRoleAt);
				System.out.println("     stmt : " + stmt);
				if(stmt != null){
					Resource swdfEvent = (Resource)stmt.getObject();
					Event event = new Event(swdfEvent, conferenceAcronym);
					
					
					
					System.out.println("     roles types : " + role.getTypes());
					for(Resource confRole : role.getTypes()){
						
						String roleId = confRole.getLocalName();
						String roleURI = roleNS + "-" + roleId + "-" + resource.getLocalName();
						
						Resource roleDuringEvent = model.createResource(roleURI, ConferenceOntology.RoleDuringEvent);
						
						roleDuringEvent.addProperty(ConferenceOntology.withRole, confRole);
						roleDuringEvent.addProperty(ConferenceOntology.during, event.asConfResource());
						
						confRoles.add(roleDuringEvent);
						
					}
				}
			}
			
			
			
			
		}
		return confRoles;
	}
	
	
	public Set<Resource> createAffiliations(Model model){
		
		Set<Resource> confAffiliations = new HashSet<Resource>(); 
		
		Set<Organisation> orgs = swdfAffiliations();
		String conferenceAcronym = conferenceEvent.getAcronym();
		conferenceAcronym = conferenceAcronym.toLowerCase().replace(" ", "");
		String organisationNS = ConferenceOntology.RESOURCE_NS + "affiliation-during-event/" + conferenceAcronym;
		for(Organisation organisation : orgs){
			String orgId = organisation.getLocalName();
			String organisationURI = organisationNS + "-" + orgId + "-" + resource.getLocalName();
			
			Event event = new Event(conferenceEvent.getResource(), conferenceEvent.getAcronym());
			
			Resource confOrganisation = organisation.asConfResource();
			Resource affiliationDuringEvent = model.createResource(organisationURI, ConferenceOntology.AffiliationDuringEvent);
			affiliationDuringEvent.addProperty(ConferenceOntology.during, event.asConfResource());
			affiliationDuringEvent.addProperty(ConferenceOntology.withOrganisation, confOrganisation);
			confOrganisation.addProperty(ConferenceOntology.inAffiliationDuringEvent, affiliationDuringEvent);
			
			confAffiliations.add(affiliationDuringEvent);
		}
		
		return confAffiliations;
	}
	
	public Resource asConfResource(Model model){
		String localName = resource.getLocalName();
		
		Resource person = model.createResource(ConferenceOntology.RESOURCE_NS + "person/" + localName);
		
		Model modelIn = resource.getModel();
		
		String sparql = 
				"CONSTRUCT {"
				+ "<" + person.getURI() + "> a <" + ConferenceOntology.Person.getURI() + "> . "
				+ "<" + person.getURI() + "> <" + RDFS.label + "> ?label . "
				+ "<" + person.getURI() + "> <" + ConferenceOntology.name + "> ?name . "
				+ "<" + person.getURI() + "> <" + ConferenceOntology.givenName + "> ?firstName . "
				+ "<" + person.getURI() + "> <" + ConferenceOntology.familyName + "> ?lastName . "
				+ "<" + person.getURI() + "> <" + FOAF.mbox_sha1sum + "> ?mbox_sha1sum . "
				+ "<" + person.getURI() + "> <" + OWL2.sameAs + "> <" + resource.getURI() + "> "
				+ "}"
				+ "WHERE{ "
				+ "<" + resource.getURI() + "> <" + RDFS.label + "> ?label . "
				+ "<" + resource.getURI() + "> <" + FOAF.name + "> ?name . "
				+ "OPTIONAL {<" + resource.getURI() + "> <" + FOAF.firstName + "> ?firstName}"
				+ "OPTIONAL {<" + resource.getURI() + "> <http://xmlns.com/foaf/0.1/lastName> ?lastName}"
				+ "OPTIONAL {<" + resource.getURI() + "> <" + FOAF.mbox_sha1sum + "> ?mbox_sha1sum}"
				+ "}";
		
		model.add(QueryExecutor.execConstruct(modelIn, sparql));
		
		
		System.out.println("create roles");
		Set<Resource> timeIndexedSituations = createRoles(model);
		for(Resource roleDuringEvent : timeIndexedSituations){
			System.out.println("     " + roleDuringEvent);
			
			
			person.addProperty(ConferenceOntology.holdsRole, roleDuringEvent);
			roleDuringEvent.addProperty(ConferenceOntology.isHeldBy, person);
		}
		
		timeIndexedSituations = createAffiliations(model);
		for(Resource affiliationDuringEvent : timeIndexedSituations){
			person.addProperty(ConferenceOntology.hasAffiliation, affiliationDuringEvent);
			affiliationDuringEvent.addProperty(ConferenceOntology.isAffiliationOf, person);
		}
		
		return person;
		
	}
}
