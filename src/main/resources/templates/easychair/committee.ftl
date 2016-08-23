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

# Committee Organization

map:CommitteeOrganization a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	#d2rq:uriSqlExpression "CONCAT('${baseURI}organization/', LOWER(REPLACE(COMMITTEE.organization, ' ', '-')))";
	d2rq:uriPattern "${baseURI}organization/@@COMMITTEE.organization@@";
	d2rq:translateWith map:UriTranslator;
	d2rq:join "COMMITTEE.# = PCM.#";
	d2rq:alias "COMMITTEE as PCM";
	d2rq:class foaf:Organization .
	
map:organization_comm_name a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:CommitteeOrganization;
	d2rq:property rdfs:label;
    d2rq:column "COMMITTEE.organization" .
    
map:organization_comm_foaf_name a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:CommitteeOrganization;
	d2rq:property foaf:name;
    d2rq:column "COMMITTEE.organization" .
    
map:organization_comm_member a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:CommitteeOrganization;
	d2rq:property foaf:member;
	#d2rq:uriSqlExpression "LOWER(CONCAT('${baseURI}person/', REPLACE(COMMITTEE.`first name`, ' ', '-'), '-', REPLACE(COMMITTEE.`last name`, ' ', '-')))" .
	d2rq:uriPattern "${baseURI}person/@@COMMITTEE.first name@@-@@COMMITTEE.last name@@";
	d2rq:translateWith map:UriTranslator .
	
map:organization_comm_based_near  a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:CommitteeOrganization;
	d2rq:property foaf:based_near;
	d2rq:column "COMMITTEE.country"
	
# Committee

map:Committee a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	#d2rq:uriSqlExpression "LOWER(CONCAT('${baseURI}person/', REPLACE(COMMITTEE.`first name`, ' ', '-'), '-', REPLACE(COMMITTEE.`last name`, ' ', '-')))";
	d2rq:uriPattern "${baseURI}person/@@COMMITTEE.first name@@-@@COMMITTEE.last name@@";
	d2rq:translateWith map:UriTranslator ;
	d2rq:join "COMMITTEE.# = PCM.#";
	d2rq:alias "COMMITTEE as PCM";
	d2rq:join "COMMITTEE.track # = TRACK.#";
	d2rq:class foaf:Person .
	
map:committee_given_name a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Committee;
	d2rq:property foaf:givenName;
	d2rq:column "COMMITTEE.first name" .
	
map:committee_family_name a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Committee;
	d2rq:property foaf:familyName;
	d2rq:column "COMMITTEE.last name" .
	
map:committee_name a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Committee;
	d2rq:property foaf:name;
	d2rq:property rdfs:label;
	d2rq:pattern "@@COMMITTEE.first name@@ @@COMMITTEE.last name@@" .

map:committee_affiliation a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Committee;
	d2rq:property swrc:affiliation;
	#d2rq:uriSqlExpression "CONCAT('${baseURI}organization/', LOWER(REPLACE(COMMITTEE.organization, ' ', '-')))" .
	d2rq:uriPattern "${baseURI}organization/@@COMMITTEE.organization@@";
	d2rq:translateWith map:UriTranslator .

map:committee_homepage a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Committee;
	d2rq:uriColumn "COMMITTEE.Web site";
	d2rq:condition "COMMITTEE.`Web site` <> ''";
	d2rq:property foaf:homepage .
	
map:committee_email a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Committee;
	d2rq:uriPattern "mailto:@@COMMITTEE.email@@";
	d2rq:condition "COMMITTEE.email <> ''";
	d2rq:property foaf:mbox .
	
map:committee_email_sha1 a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Committee;
	d2rq:sqlExpression "SHA1(CONCAT('mailto:', COMMITTEE.email))";
	d2rq:condition "COMMITTEE.email <> ''";
	d2rq:property foaf:mbox_sha1sum .
	
map:committee_role a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:Committee;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/@@TRACK.name@@/program-committee-member";
	d2rq:property swc:holdsRole .
	
# PSM Roles
map:PCM a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	d2rq:uriPattern "${baseURI}conference/${confAcronym?lower_case}/${year}/@@TRACK.name@@/program-committee-member";
	d2rq:join "COMMITTEE.# = PCM.#";
	d2rq:alias "COMMITTEE as PCM";
	d2rq:join "COMMITTEE.track # = TRACK.#";
	d2rq:condition "COMMITTEE.`#` <> ''";
	d2rq:class swc:ProgrammeCommitteeMember .

map:pcm_label a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:PCM;
	d2rq:pattern "Program committee member of the @@TRACK.name@@ track";
	d2rq:property rdfs:label .
	
map:pcm_role_at a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:PCM;
	d2rq:constantValue <${baseURI}conference/${confAcronym?lower_case}/${year}>;
	d2rq:property swc:isRoleAt .

map:pcm_held_by a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:PCM;
	#d2rq:uriSqlExpression "LOWER(CONCAT('${baseURI}person/', REPLACE(COMMITTEE.`first name`, ' ', '-'), '-', REPLACE(COMMITTEE.`last name`, ' ', '-')))";
	d2rq:uriPattern "${baseURI}person/@@COMMITTEE.first name@@-@@COMMITTEE.last name@@";
	d2rq:translateWith map:UriTranslator ;
	d2rq:property swc:heldBy .