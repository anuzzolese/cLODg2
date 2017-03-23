package org.w3id.scholarlydata.clodg;

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
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.RDF;

public class LDGenerator {
	
	private static LDGenerator instance;
	
	private LDGenerator() {
		
	}
	
	public static LDGenerator getInstance(){
		if(instance == null) instance = new LDGenerator();
		return instance;
	}

	public Model generate(Properties properties, InputCSVFiles inputCSVFiles){
		
		Model model = null;
		
		if(!properties.isEmpty()){
			
			FMTemplate fmTemplate = new FMTemplate("main.ftl", properties);
			Model d2rqMapping = fmTemplate.generateMapping();
			
			// add committee if the corresponding CSV is available
			if(inputCSVFiles.hasCommittee()){
				fmTemplate = new FMTemplate("committee.ftl", properties);
				d2rqMapping.add(fmTemplate.generateMapping());
			}
			
			// add keynotes if the corresponding CSV is available
			if(inputCSVFiles.hasKeynote()){
				fmTemplate = new FMTemplate("keynotes.ftl", properties);
				d2rqMapping.add(fmTemplate.generateMapping());
			}
			
			// add organising members and their associated roles if the corresponding CSV is available
			if(inputCSVFiles.hasOrganising()){
				fmTemplate = new FMTemplate("organising.ftl", properties);
				d2rqMapping.add(fmTemplate.generateMapping());
			}
			
			// add sessions if the corresponding CSV is available
			if(inputCSVFiles.hasSession()){
				fmTemplate = new FMTemplate("sessions.ftl", properties);
				d2rqMapping.add(fmTemplate.generateMapping());
			}
			
			// add talks if the corresponding CSV is available
			if(inputCSVFiles.hasTalk()){
				fmTemplate = new FMTemplate("talks.ftl", properties);
				d2rqMapping.add(fmTemplate.generateMapping());
			}
			
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
			 * Add keywords
			 * 
			 */
			sparql = 
					"PREFIX swrc: <http://swrc.ontoware.org/ontology#> "
					+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
					+ "SELECT ?paper ?keywords "
					+ "WHERE{ "
					+ "?paper a swrc:InProceedings . "
					+ "?paper dc:subject ?keywords "		
					+ "}";
			query = QueryFactory.create(sparql, Syntax.syntaxARQ);
			queryExecution = QueryExecutionFactory.create(query, ((EasychairModel)model).materialiseAll());
			
			List<Statement> stmts = new ArrayList<Statement>();
			rs = queryExecution.execSelect();
			rs.forEachRemaining(querySolution -> {
				Resource paper = querySolution.getResource("paper");
				String keywords = querySolution.getLiteral("keywords").getLexicalForm();
				String[] keywordsArray = keywords.split("\n");
				for(String keyword : keywordsArray){
					stmts.add(new StatementImpl(paper, DC_11.subject, ModelFactory.createDefaultModel().createTypedLiteral(keyword)));
				}
			});
			
			model.removeAll(null, DC_11.subject, (RDFNode)null);
			model.add(stmts);
			
			
			/*
			
			try {
				model.write(new FileOutputStream(new File("test.ttl")), "TURTLE");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			model.close();
			*/
			
			((EasychairModel)model).addAuthorLists(model);
			
		}
		
		return model;
	}
	
	
}
