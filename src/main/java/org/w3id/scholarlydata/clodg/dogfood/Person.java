package org.w3id.scholarlydata.clodg.dogfood;

import java.util.HashSet;
import java.util.Set;

import org.w3id.scholarlydata.clodg.Config;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
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
		conferenceEvent = ConferenceEvent.getInstance(resource.getModel());
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
	
	private Set<Resource> createRoles(Resource confPerson, Model model){
		Set<Resource> confRoles = new HashSet<Resource>();
		Set<Role> roles = holdsRole();
		
		Literal personName = confPerson.getProperty(ConferenceOntology.name).getObject().asLiteral();
		
		String conferenceAcronym = conferenceEvent.getAcronym();
		
		conferenceAcronym = conferenceAcronym.toLowerCase().replace(" ", "");
		String roleNS = ConferenceOntology.RESOURCE_NS + "role-during-event/" + conferenceAcronym;
		for(Role role : roles){
			Resource swdfRole = role.getResource();
			if(swdfRole != null){
				Statement stmt = swdfRole.getProperty(SWC.isRoleAt);
				if(stmt != null){
					Resource swdfEvent = (Resource)stmt.getObject();
					//Event event = new Event(swdfEvent, "conference");
					//ConferenceEvent conferenceEvent = new ConferenceEvent(model); 
					
					for(Resource confRole : role.getTypes()){
						
						String roleId = confRole.getLocalName();
						String roleURI = roleNS + "-" + roleId + "-" + resource.getLocalName();
						
						Resource roleDuringEvent = model.createResource(roleURI, ConferenceOntology.RoleDuringEvent);
						
						Statement roleStmt = ConferenceOntology.getModel().getResource(confRole.getURI()).getProperty(RDFS.label);
						
						String roleLabel = "";
						if(roleStmt != null){
							Literal label = (Literal)roleStmt.getObject();
							model.add(confRole, RDFS.label, label);
							
							roleLabel = label.getLexicalForm();
						}
						else{
							roleLabel = "";
							String localRoleLabel = "";
							String[] roleLabelParts = confRole.getLocalName().split("\\-");
							for(String roleLabelPart : roleLabelParts){
								if(!roleLabel.isEmpty()) {
									roleLabel += " ";
									localRoleLabel += " ";
								}
								roleLabel += roleLabelPart;
								localRoleLabel += roleLabelPart.substring(0,1).toUpperCase() + roleLabelPart.substring(1);
							}
							model.add(confRole, RDFS.label, localRoleLabel);
						}
						
						roleDuringEvent.addProperty(ConferenceOntology.withRole, confRole);
						roleDuringEvent.addProperty(ConferenceOntology.during, conferenceEvent.asConfResource(model));
						
						if(!roleLabel.isEmpty()) roleLabel = "of " + roleLabel;
						roleDuringEvent.addLiteral(RDFS.label, "Role " + roleLabel + " held by " + personName.getLexicalForm() + " during " + Config.CONF_ACRONYM + Config.YEAR);
						
						confRoles.add(roleDuringEvent);
						
					}
				}
			}
			
			
			/*
			 * Author role
			 */
			
			
			StmtIterator madesIt = resource.listProperties(FOAF.made);
			while(madesIt.hasNext()){
				Statement madeStmt = madesIt.next();
				RDFNode object = madeStmt.getObject();
				if(object.isResource()){
					Resource paper = object.asResource();
					
					InProceedings inProceedings = new InProceedings(paper);
					Resource inProceedingsResource = inProceedings.asConfResource(model);
					String inProceedingsLocalName = inProceedingsResource.getLocalName();
					String paperTitle = inProceedingsResource.getProperty(ConferenceOntology.title).getObject().asLiteral().getLexicalForm();
					
					String roleURI = roleNS + "-author-" + resource.getLocalName() + "-" + inProceedingsLocalName;
					Resource roleDuringEvent = model.createResource(roleURI, ConferenceOntology.RoleDuringEvent);
					roleDuringEvent.addProperty(ConferenceOntology.withRole, ConferenceOntology.author);
					roleDuringEvent.addProperty(ConferenceOntology.during, conferenceEvent.asConfResource(model));
					roleDuringEvent.addProperty(ConferenceOntology.withDocument, new InProceedings(paper).asConfResource(model));
					
					roleDuringEvent.addLiteral(RDFS.label, "Role of Author of held by " + personName.getLexicalForm() + " during " + Config.CONF_ACRONYM + Config.YEAR + " for paper titled: \"" + paperTitle + "\".");
					
					confRoles.add(roleDuringEvent);
					
					if(!model.contains(ConferenceOntology.authorLabelStmt))
						model.add(ConferenceOntology.authorLabelStmt);
				}
			}
			
		}
		return confRoles;
	}
	
	
	private Set<Resource> createAffiliations(Resource confPerson, Model model){
		
		Set<Resource> confAffiliations = new HashSet<Resource>(); 
		
		Literal personName = confPerson.getProperty(ConferenceOntology.name).getObject().asLiteral();
		Set<Organisation> orgs = swdfAffiliations();
		String conferenceAcronym = conferenceEvent.getAcronym();
		conferenceAcronym = conferenceAcronym.toLowerCase().replace(" ", "");
		String organisationNS = ConferenceOntology.RESOURCE_NS + "affiliation-during-event/" + conferenceAcronym;
		for(Organisation organisation : orgs){
			String orgId = organisation.getLocalName();
			String organisationURI = organisationNS + "-" + orgId + "-" + resource.getLocalName();
			
			Literal organisationName = organisation.getResource().getProperty(FOAF.name).getObject().asLiteral();
			
			//Event event = new Event(conferenceEvent.getResource(), "conference");
			
			Resource confOrganisation = organisation.asConfResource();
			Resource affiliationDuringEvent = model.createResource(organisationURI, ConferenceOntology.AffiliationDuringEvent);
			affiliationDuringEvent.addProperty(ConferenceOntology.during, conferenceEvent.asConfResource(model));
			affiliationDuringEvent.addProperty(ConferenceOntology.withOrganisation, confOrganisation);
			
			String label = "Affiliation held by " + personName.getLexicalForm() + " with " + organisationName.getLexicalForm() + " during " + Config.CONF_ACRONYM + Config.YEAR;
			affiliationDuringEvent.addLiteral(RDFS.label, label);
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
				//+ "<" + person.getURI() + "> <" + OWL2.sameAs + "> <" + resource.getURI() + "> "
				+ "}"
				+ "WHERE{ "
				+ "<" + resource.getURI() + "> <" + RDFS.label + "> ?label . "
				+ "<" + resource.getURI() + "> <" + FOAF.name + "> ?name . "
				+ "OPTIONAL {<" + resource.getURI() + "> <" + FOAF.firstName + "> ?firstName}"
				+ "OPTIONAL {<" + resource.getURI() + "> <http://xmlns.com/foaf/0.1/lastName> ?lastName}"
				+ "OPTIONAL {<" + resource.getURI() + "> <" + FOAF.mbox_sha1sum + "> ?mbox_sha1sum}"
				+ "}";
		
		model.add(QueryExecutor.execConstruct(modelIn, sparql));
		
		
		Set<Resource> timeIndexedSituations = createRoles(person, model);
		for(Resource roleDuringEvent : timeIndexedSituations){
			person.addProperty(ConferenceOntology.holdsRole, roleDuringEvent);
			roleDuringEvent.addProperty(ConferenceOntology.isHeldBy, person);
		}
		
		timeIndexedSituations = createAffiliations(person, model);
		for(Resource affiliationDuringEvent : timeIndexedSituations){
			person.addProperty(ConferenceOntology.hasAffiliation, affiliationDuringEvent);
			affiliationDuringEvent.addProperty(ConferenceOntology.isAffiliationOf, person);
		}
		
		return person;
		
	}
}
