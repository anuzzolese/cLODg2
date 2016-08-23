package org.w3id.scholarlydata.clodg.dogfood;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

import au.com.bytecode.opencsv.CSVReader;

public class RolesTDBScript {

	
	public static void main(String[] args) {
		//Dataset dataset = TDBFactory.createDataset("role_mappings");
		RoleMappings roleMappings = RoleMappings.getInstance();
		Model model = ModelFactory.createDefaultModel();
		for(Resource dogFoodRole : roleMappings.getDogFoodRoles()){
			
			Resource confRole = roleMappings.getConfRole(dogFoodRole);
			model.add(dogFoodRole, ModelFactory.createDefaultModel().createProperty("http://www.w3.org/2004/02/skos/core#closeMatch"), confRole);
			
			//model.write(System.out);
		}
		
		try {
			CSVReader reader = new CSVReader(new FileReader("ontologies/roles.csv"), ';');
			String[] row = null;
			while((row = reader.readNext()) != null){
				String role = row[0];
				String roleType1 = row[3];
				String roleType2 = row[4];
				
				
				
				
				
				
				if(roleType1 != null){
					Resource rt1 = model.createResource(roleType1);
					if(roleType1.trim().isEmpty()) rt1 = model.createResource("http://data.semanticweb.org/ns/swc/ontology#Role");
					else rt1 = model.createResource(roleType1);
					Resource r = model.createResource(role, rt1);
					
					if(roleType2 != null && !roleType2.trim().isEmpty()){
						r.addProperty(RDF.type, model.createResource(roleType2));
					}
				}
				
			
			}
			
			reader.close();
			
			Model confData = FileManager.get().loadModel("test/eswc2016.rdf");
			String sparql = "PREFIX swc: <http://data.semanticweb.org/ns/swc/ontology#> "
					+ "SELECT ?role ?roletype "
					+ "WHERE{?role a swc:Chair . ?role a ?roletype . filter(?roletype != swc:Chair)}";
			Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
			QueryExecution queryExecution = QueryExecutionFactory.create(query, confData);
			ResultSet resultSet = queryExecution.execSelect();
			
			//ResultSetFormatter.out(System.out, resultSet);
			
			while(resultSet.hasNext()){
				QuerySolution querySolution = resultSet.next();
				Resource role = querySolution.getResource("role");
				Resource roleType = querySolution.getResource("roletype");
				
				
				System.out.println(role + " - " + roleType);
				model.add(role, RDF.type, roleType);
				
				
			}
			
			
			model.write(new FileOutputStream("rolesModel.rdf"));
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
