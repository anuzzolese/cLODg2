package org.w3id.scholarlydata.clodg.dogfood;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import au.com.bytecode.opencsv.CSVReader;



public class RoleMappings {

	private static RoleMappings instance;
	
	private Map<Resource, Resource> map;
	
	private RoleMappings(){
		map = new HashMap<Resource, Resource>();
		try {
			CSVReader reader = new CSVReader(new FileReader(new File("ontologies/mappings.csv")), ';');
			String[] row = null;
			while((row = reader.readNext()) != null){
				String dogFoodRoleURI = row[0];
				String confRoleURI = row[1];
				Resource dogFoodRole = ModelFactory.createDefaultModel().createResource(dogFoodRoleURI);
				Resource confRole = ModelFactory.createDefaultModel().createResource(confRoleURI);
				map.put(dogFoodRole, confRole);
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static RoleMappings getInstance(){
		if(instance == null) instance = new RoleMappings();
		return instance;
	}
	
	public Resource getConfRole(Resource dogFoodRole){
		return map.get(dogFoodRole);
	}
	
	public Set<Resource> getDogFoodRoles(){
		return map.keySet();
	}
	
	public Collection<Resource> getConfRoles(){
		return map.values();
	}
	
}
