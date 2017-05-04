package org.w3id.scholarlydata.clodg.dogfood;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class Role {

	private Resource resource;
	
	public Role(Resource resource) {
		this.resource = resource;
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public Set<Resource> getTypes(){
		
		RoleKB roleKB = RoleKB.getInstance();
		
		//return (Resource) resource.getProperty(RDF.type).getObject();
		
		StmtIterator stmtIterator = resource.listProperties(RDF.type);
		boolean found = false;
		Resource roleClass = null;
		while(!found && stmtIterator.hasNext()){
			Statement stmt = stmtIterator.next();
			Resource roleClassTmp = (Resource) stmt.getObject();
			if(!roleClassTmp.equals(SWC.Chair)){
				found = true;
				roleClass = roleClassTmp;
			}
		}
		return roleKB.getConfRoleFromSWDFRole(roleClass);
	}
	
}
