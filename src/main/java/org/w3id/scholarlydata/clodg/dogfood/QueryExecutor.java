package org.w3id.scholarlydata.clodg.dogfood;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;

public class QueryExecutor {

	
	private static QueryExecution createQueryExecution(Model model, String sparql){
		Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
		return QueryExecutionFactory.create(query, model);
	}

	public static ResultSet execSelect(Model model, String sparql){
		QueryExecution queryExecution = createQueryExecution(model, sparql);
		return queryExecution.execSelect();
	}
	
	public static ResultSet execRemoteSelect(String serviceURI, String sparql){
		
		QueryExecution queryExecution = QueryExecutionFactory.createServiceRequest(serviceURI, QueryFactory.create(sparql, Syntax.syntaxSPARQL_11));
		return queryExecution.execSelect();
	}
	
	public static Model execConstruct(Model model, String sparql){
		QueryExecution queryExecution = createQueryExecution(model, sparql);
		return queryExecution.execConstruct();
	}

}
