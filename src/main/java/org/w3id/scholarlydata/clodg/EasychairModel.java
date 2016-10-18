package org.w3id.scholarlydata.clodg;

import java.io.OutputStream;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;

import de.fuberlin.wiwiss.d2rq.jena.ModelD2RQ;

public class EasychairModel extends ModelD2RQ {

	private static final String BASE_DATA_URI = "http://data.semanticweb.org/";
	
	private Model adds;
	private Model removes;
	
	public EasychairModel(String mapURL) {
		super(mapURL);
		adds = ModelFactory.createDefaultModel();
		removes = ModelFactory.createDefaultModel();
		addAuthorLists();
		
	}
	
	public EasychairModel(Model mapping) {
		super(mapping, BASE_DATA_URI);
		adds = ModelFactory.createDefaultModel();
		removes = ModelFactory.createDefaultModel();
		addAuthorLists();
	}
	
	private void addAuthorLists(){
		String sparql = "SELECT ?author ?paper "
						+ "WHERE{"
						+ "?author <" + FOAF.made + "> ?paper"
						+ "}";
		
		Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, this);
		ResultSet resultSet = queryExecution.execSelect();
		
		Model authorListModel = ModelFactory.createDefaultModel();
		Property biboAuthorList = authorListModel.createProperty("http://purl.org/ontology/bibo/authorList");
		
		String previousAuthorListUri = null;
		int authorCounter = 1;
		while(resultSet.hasNext()){
			QuerySolution solution = resultSet.next();
			Resource author = solution.getResource("author");
			Resource paper = solution.getResource("paper");
			
			String authorListUri = paper.getURI() + "/authorList";
			
			if(!authorListUri.equals(previousAuthorListUri)) {
				authorCounter = 1;
				previousAuthorListUri = authorListUri;
			}
			
			
			Resource authorTmp = authorListModel.createResource(author.getURI());
			Resource paperTmp = authorListModel.createResource(paper.getURI());
			Resource authorList = authorListModel.createResource(authorListUri);
			paperTmp.addProperty(biboAuthorList, authorList);
			
			Property containerMember = authorListModel.createProperty(RDF.getURI() + "_" + authorCounter);
			authorList.addProperty(containerMember, authorTmp);
			
			authorCounter += 1;
			
		}
		this.add(authorListModel);
	}

	
	@Override
	public Model write(OutputStream writer, String lang) {
		Model model = ModelFactory.createDefaultModel();
		model.add(this);
		model.add(adds);
		model.remove(removes);
		return model.write(writer, lang);
	}
	
	@Override
	public Model add(Model m) {
		adds.add(m);
		return this;
	}
	
	@Override
	public Model remove(Model m) {
		removes.add(m);
		return this;
	}
	
	@Override
	public Model remove(List<Statement> statements) {
		removes.add(statements);
		return this;
	}
	
	@Override
	public Model remove(Resource subject, Property predicate, RDFNode object) {
		removes.add(new StatementImpl(subject, predicate, object));
		return this;
	}
	
	@Override
	public Model removeAll(Resource subject, Property predicate, RDFNode object) {
		this.listStatements(subject, predicate, object).forEachRemaining(stmt -> {
			removes.add(stmt);	
		});
		
		return this;
	}
	
	@Override
	public Model add(Resource subject, Property predicate, RDFNode object) {
		adds.add(subject, predicate, object);
		return this;
	}
	
	@Override
	public Model add(List<Statement> stmts) {
		stmts.forEach(stmt -> {
			adds.add(stmt);
		});
		
		return this;
	}
	
	public Model materialiseAll(){
		Model model = ModelFactory.createDefaultModel();
		model.add(this);
		model.add(adds);
		model.remove(removes);
		return model;
	}
}
