package org.w3id.scholarlydata.clodg.dogfood;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class RoleKB {
	
	private static final String DATASET_LOCATION = "/ontologies/rolesModel.rdf";
	
	private static RoleKB instance;
	
	private Model rolesModel;
	
	private RoleKB(){
		rolesModel = ModelFactory.createDefaultModel().read(getClass().getResourceAsStream(DATASET_LOCATION), null, "RDF/XML");
	}
	
	public static RoleKB getInstance(){
		if(instance == null) instance = new RoleKB();
		return instance;
	}
	
	public Set<Resource> getDogFoodRole(Resource roleInstance){
		Set<Resource> dogFoodRoles = new HashSet<Resource>();
		
		String sparql =   "SELECT DISTINCT ?role"
						+ "WHERE{ "
						+ "<" + roleInstance.getURI() + "> a ?role "
						+ "}";
		ResultSet resultSet = executesQuery(sparql);
		
		resultSet.forEachRemaining(querySolution -> {
			dogFoodRoles.add(querySolution.getResource("role"));
		});
		
		return dogFoodRoles;
	}
	
	public Set<Resource> getConfRoleFromSWDFRole(Resource swdfRoleClass){
		Set<Resource> confRoles = new HashSet<Resource>();
		
		/*
		 * FIXME
		 * 
		 * This is only a patch to allow cLODg solving roles.
		 */
		//String uri = swdfRoleClass.getURI().replace("iswc-", "eswc-");
		
		
		String sparql =   "SELECT DISTINCT ?confRole "
						+ "WHERE{ "
						+ "<" + swdfRoleClass.getURI() + "> <http://www.w3.org/2004/02/skos/core#closeMatch> ?confRole "
						+ "}";
		
		ResultSet resultSet = executesQuery(sparql);
		
		resultSet.forEachRemaining(querySolution -> {
			confRoles.add(querySolution.getResource("confRole"));
		});
		
		return confRoles;
	}
	
	public void addRolesFromModel(Model model){
		String sparql = "PREFIX swc: <http://data.semanticweb.org/ns/swc/ontology#> "
				+ "SELECT ?role ?roletype "
				+ "WHERE{?role a swc:Chair . ?role a ?roletype . filter(?roletype != swc:Chair)}";
		Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
		ResultSet resultSet = queryExecution.execSelect();
		
		Model tmp = ModelFactory.createDefaultModel();
		resultSet.forEachRemaining(querySolution -> {
			Resource role = querySolution.getResource("role");
			Resource roleType = querySolution.getResource("roletype");
			tmp.add(role, RDF.type, roleType);
		});
		
	}
	
	
	private ResultSet executesQuery(String sparql){
		Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, rolesModel);
		return queryExecution.execSelect();
		
	}

}
