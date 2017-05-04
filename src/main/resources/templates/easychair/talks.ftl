@prefix : <${baseURI}> .
@prefix swc: <http://data.semanticweb.org/ns/swc/ontology#> . 
@prefix swrc: <http://swrc.ontoware.org/ontology#> . 
@prefix map: <file:/Users/andrea/Desktop/${confAcronym?upper_case}${year}data/D2RQ/mapping-${confAcronym?lower_case}.ttl#> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix d2rq: <http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#> .
@prefix dc: <http://purl.org/dc/elements/1.1/> .
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix vcard: <http://www.w3.org/2001/vcard-rdf/3.0#> .
@prefix jdbc: <http://d2rq.org/terms/jdbc/> .
@prefix icaltzd: <http://www.w3.org/2002/12/cal/icaltzd#> .

# Paper related to talks
map:PaperTalk a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/@@TRACK.name@@/@@SUBMISSION.#@@";
	d2rq:join "SUBMISSION.# = TALK.paper id";
	d2rq:join "TALK.session id = SESSION.id";
	d2rq:join "SUBMISSION.track # = TRACK.#";
	d2rq:translateWith map:UriTranslator;
	d2rq:class swrc:InProceedings .

map:papertalk_relation a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:PaperTalk;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/talk/@@TALK.paper id@@";
	d2rq:property swc:relatedToEvent .
	
# Talks

map:Talk a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	d2rq:join "TALK.paper id = SUBMISSION.#";
	d2rq:join "TALK.session id = SESSION.id";
	d2rq:join "TRACK.# = SUBMISSION.track #";
	d2rq:condition "${acceptWordings}";
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/talk/@@TALK.paper id@@";
	d2rq:class icaltzd:Vevent .
	
map:talk_vevent a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Talk;
	d2rq:property rdf:type;
	d2rq:constantValue <http://data.semanticweb.org/ns/swc/ontology#TalkEvent> .
	
map:talk_start a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Talk;
	d2rq:property icaltzd:dtstart;
	d2rq:pattern "@@SESSION.date@@T@@TALK.start@@" .
	
map:talk_end a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Talk;
	d2rq:property icaltzd:dtend;
	d2rq:pattern "@@SESSION.date@@T@@TALK.end@@" .
	
map:talk_title a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Talk;
	d2rq:property icaltzd:description;
	d2rq:property rdfs:label;
	d2rq:pattern "Talk: @@SUBMISSION.title@@" .
	
map:talk_summary a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Talk;
	d2rq:property icaltzd:summary;
	d2rq:property rdfs:label;
	d2rq:pattern "@@SUBMISSION.authors@@" .
	
map:talk_session a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Talk;
	d2rq:condition "SESSION.track <> ''";
	d2rq:property swc:isSubEventOf;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/session/@@SESSION.id@@" .
	
map:talk_track a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Talk;
	d2rq:condition "SESSION.track <> ''";
	d2rq:property swc:isSubEventOf;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/@@TRACK.name@@";
	d2rq:translateWith map:UriTranslator .