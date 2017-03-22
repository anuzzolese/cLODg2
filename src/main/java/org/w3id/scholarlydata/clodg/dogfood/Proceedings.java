package org.w3id.scholarlydata.clodg.dogfood;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Proceedings {

	
	private Model model;
	private List<Proc> proceedingsVolumes;
	
	public Proceedings(Model model) {
		this.model = model;
		this.proceedingsVolumes = new ArrayList<Proc>();
		String sparql = "PREFIX rdfs: <" + RDFS.getURI() + "> "
				+ "SELECT DISTINCT ?proceedings ?label "
				+ "WHERE {"
				+ "	{?inproceedings <" + SWC.isPartOf + "> ?proceedings} "
				+ "	UNION "
				+ "	{?inproceedings <" + SWC.NS + "partOf" + "> ?proceedings} "
				+ "	?inproceedings a <http://swrc.ontoware.org/ontology#InProceedings> . "
				+ " ?proceedings rdfs:label ?label "
				+ "}";
		Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
		ResultSet resultSet = queryExecution.execSelect();
		while(resultSet.hasNext()){
			QuerySolution querySolution = resultSet.next();
			Resource proceedings = querySolution.getResource("proceedings");
			Literal label = querySolution.getLiteral("label");
			this.proceedingsVolumes.add(new Proc(proceedings, label));
		}
	}
	
	public Model getModel() {
		return model;
	}
	
	public Collection<InProceedings> list(){
		//Collection<InProceedings> inProceedingsList = new ArrayList<InProceedings>();
	
		StmtIterator stmtIterator = model.listStatements(null, RDF.type, ModelFactory.createDefaultModel().createResource("http://swrc.ontoware.org/ontology#InProceedings"));
		Stream<Statement> stmtStream = stmtIterator.toList().stream();
		
		Stream<Resource> resourceStream = stmtStream.map(stmt -> {
			Resource inProceedingsResource = stmt.getSubject();
			return inProceedingsResource;
		});
		
		resourceStream = resourceStream.distinct();
		Stream<InProceedings> inProcStream = resourceStream.map(resource -> {
			InProceedings inProceedings = new InProceedings(resource);
			return inProceedings;
		});
		
		
		
		return inProcStream.collect(Collectors.toList());
		
		/*
		while(stmtIterator.hasNext()){
			Statement stmt = stmtIterator.next();
			Resource inProceedingsResource = stmt.getSubject();
			InProceedings inProceedings = new InProceedings(inProceedingsResource);
			inProceedingsList.add(inProceedings);
		}
		return inProceedingsList;
		*/
	}
	
	public List<Resource> asConfResource(Model model){
		
		List<Resource> volumes = proceedingsVolumes.stream().map(proc -> {
			Resource proceedings = proc.getProc();
			Resource confProceedings = model.createResource(proceedings.getURI().replace("http://data.semanticweb.org/", ConferenceOntology.RESOURCE_NS), ConferenceOntology.Proceedings);
			confProceedings.addLiteral(RDFS.label, proc.getLabel());
			return confProceedings;
		}).collect(Collectors.toList());
		
		return volumes;
		
	}
	
	private class Proc{
		private Resource proc;
		private Literal label;
		
		public Proc(Resource proc, Literal label) {
			this.proc = proc;
			this.label = label;
		}
		
		public Resource getProc() {
			return proc;
		}
		
		public Literal getLabel() {
			return label;
		}
		
		
	}
}
