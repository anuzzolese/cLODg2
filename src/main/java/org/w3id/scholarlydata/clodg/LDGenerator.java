package org.w3id.scholarlydata.clodg;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.vocabulary.RDF;

public class LDGenerator {
	
	private static LDGenerator instance;
	
	private LDGenerator() {
		
	}
	
	public static LDGenerator getInstance(){
		if(instance == null) instance = new LDGenerator();
		return instance;
	}

	public Model generate(String configuration){
		
		
		Model model = null;
		
		Properties properties = new Properties();
		
		try {
			InputStream confInputStream = new FileInputStream(configuration);
			properties.load(confInputStream);
			confInputStream.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(!properties.isEmpty()){
			
			FMTemplate fmTemplate = new FMTemplate(properties);
			Model d2rqMapping = fmTemplate.generateMapping();
			
			model = new EasychairModel(d2rqMapping);
			
			String sparql = 
					"CONSTRUCT {"
					+ "?subevent1 <http://data.semanticweb.org/ns/swc/ontology#isSubEventOf> ?superevent1 . "
					+ "?superevent2 <http://data.semanticweb.org/ns/swc/ontology#isSuperEventOf> ?subevent2 . "
					+ "?event <http://data.semanticweb.org/ns/swc/ontology#hasRelatedArtefact> ?paper "
					+ "} "
					+ "WHERE{ "
					+ "OPTIONAL{?superevent1 <http://data.semanticweb.org/ns/swc/ontology#isSuperEventOf> ?subevent1 }"
					+ "OPTIONAL{?subevent2 <http://data.semanticweb.org/ns/swc/ontology#isSubEventOf> ?superevent2} "
					+ "OPTIONAL{?paper <http://data.semanticweb.org/ns/swc/ontology#relatedToEvent> ?event} "
					+ "}";
			
			Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
			QueryExecution queryExecution = QueryExecutionFactory.create(query, ((EasychairModel)model).materialiseAll());
			Model add = queryExecution.execConstruct();
			
			model = model.add(add);
			
			//model.write(System.out, "N-TRIPLES");
			Resource talkEvent = model.createResource("http://data.semanticweb.org/ns/swc/ontology#TalkEvent");
			Resource demoEvent = model.createResource("http://data.semanticweb.org/ns/swc/ontology#DemoEvent");
			Resource posterEvent = model.createResource("http://data.semanticweb.org/ns/swc/ontology#PosterEvent");
			sparql = 
					"SELECT ?event "
					+ "WHERE{ "
					+ "?event a <" + talkEvent.getURI() + "> . "
					+ "?event <http://data.semanticweb.org/ns/swc/ontology#hasRelatedArtefact> ?document . "
					+ "FILTER(REGEX(STR(?document), \"/paper/demo/\"))"		
					+ "}";
			query = QueryFactory.create(sparql, Syntax.syntaxARQ);
			queryExecution = QueryExecutionFactory.create(query, ((EasychairModel)model).materialiseAll());
			ResultSet rs = queryExecution.execSelect();
			
			List<Statement> statementsToRemove = new ArrayList<Statement>();
			while(rs.hasNext()){
				QuerySolution solution = rs.next();
				Resource event = solution.getResource("event");
				model.add(event, RDF.type, demoEvent);
				statementsToRemove.add(new StatementImpl(event, RDF.type, talkEvent));
			}
			
			sparql = 
					"SELECT ?event "
					+ "WHERE{ "
					+ "?event a <" + talkEvent.getURI() + "> . "
					+ "?event <http://data.semanticweb.org/ns/swc/ontology#hasRelatedArtefact> ?document . "
					+ "FILTER(REGEX(STR(?document), \"/paper/poster/\"))"		
					+ "}";
			query = QueryFactory.create(sparql, Syntax.syntaxARQ);
			queryExecution = QueryExecutionFactory.create(query, ((EasychairModel)model).materialiseAll());
			rs = queryExecution.execSelect();
			
			while(rs.hasNext()){
				QuerySolution solution = rs.next();
				Resource event = solution.getResource("event");
				model.add(event, RDF.type, posterEvent);
				statementsToRemove.add(new StatementImpl(event, RDF.type, talkEvent));
			}
			
			model = model.remove(statementsToRemove);
			
			sparql = 
					"CONSTRUCT {"
					+ "?session <http://data.semanticweb.org/ns/swc/ontology#isSubEventOf> ?conference . "
					+ "?conference <http://data.semanticweb.org/ns/swc/ontology#isSuperEventOf> ?session "
					+ "} "
					+ "WHERE{ "
					+ "?session a <http://data.semanticweb.org/ns/swc/ontology#SessionEvent> . "
					+ "?conference a <http://data.semanticweb.org/ns/swc/ontology#ConferenceEvent> "
					+ "}";
			
			query = QueryFactory.create(sparql, Syntax.syntaxARQ);
			queryExecution = QueryExecutionFactory.create(query, ((EasychairModel)model).materialiseAll());
			add = queryExecution.execConstruct();
			
			model = model.add(add);
			
			
			/*
			 * Add tutorial and event into schedule
			 */
			sparql = 
					"CONSTRUCT {"
					+ "?tutorial <http://data.semanticweb.org/ns/swc/ontology#isSubEventOf> ?conference . "
					+ "?conference <http://data.semanticweb.org/ns/swc/ontology#isSuperEventOf> ?tutorial . "
					+ "?workshop <http://data.semanticweb.org/ns/swc/ontology#isSubEventOf> ?conference . "
					+ "?conference <http://data.semanticweb.org/ns/swc/ontology#isSuperEventOf> ?workshop "
					+ "} "
					+ "WHERE{ "
					+ "?tutorial <http://data.semanticweb.org/ns/swc/ontology#isSubEventOf> <http://data.semanticweb.org/conference/eswc/2016/Tutorial> . "
					+ "?workshop <http://data.semanticweb.org/ns/swc/ontology#isSubEventOf> <http://data.semanticweb.org/conference/eswc/2016/Workshop> . " 
					+ "?conference a <http://data.semanticweb.org/ns/swc/ontology#ConferenceEvent> "
					+ "}";
			
			query = QueryFactory.create(sparql, Syntax.syntaxARQ);
			queryExecution = QueryExecutionFactory.create(query, ((EasychairModel)model).materialiseAll());
			add = queryExecution.execConstruct();
			
			model = model.add(add);
			
			sparql = 
					"CONSTRUCT {"
					+ "?tutorial <http://data.semanticweb.org/ns/swc/ontology#isSubEventOf> ?conference . "
					+ "?conference <http://data.semanticweb.org/ns/swc/ontology#isSuperEventOf> ?tutorial . "
					+ "?workshop <http://data.semanticweb.org/ns/swc/ontology#isSubEventOf> ?conference . "
					+ "?conference <http://data.semanticweb.org/ns/swc/ontology#isSuperEventOf> ?workshop "
					+ "} "
					+ "WHERE{ "
					+ "?tutorial <http://data.semanticweb.org/ns/swc/ontology#isSubEventOf> <http://data.semanticweb.org/conference/eswc/2016/Tutorial> . "
					+ "?workshop <http://data.semanticweb.org/ns/swc/ontology#isSubEventOf> <http://data.semanticweb.org/conference/eswc/2016/Workshop> . " 
					+ "?conference a <http://data.semanticweb.org/ns/swc/ontology#ConferenceEvent> "
					+ "}";
			
			query = QueryFactory.create(sparql, Syntax.syntaxARQ);
			queryExecution = QueryExecutionFactory.create(query, ((EasychairModel)model).materialiseAll());
			add = queryExecution.execConstruct();
			
			model = model.add(add);
			
			
			/*
			
			try {
				model.write(new FileOutputStream(new File("test.ttl")), "TURTLE");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			model.close();
			*/
		}
		
		return model;
	}
	
	public static void main(String[] args) {
		new LDGenerator().generate(null);
	}
}
