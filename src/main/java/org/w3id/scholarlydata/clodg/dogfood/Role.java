package org.w3id.scholarlydata.clodg.dogfood;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;

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
		return roleKB.getConfRoleFromDFInstance(resource);
	}
	
}
