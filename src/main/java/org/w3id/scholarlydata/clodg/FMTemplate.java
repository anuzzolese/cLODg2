package org.w3id.scholarlydata.clodg;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class FMTemplate {

	private final int majorVersion = 2;
	private final int minorVersion = 3;
	private final int microVersion = 23;
	
	private final String templateLocation = "/templates/easychair";
	private final String templateName = "d2rq_mapping_hsqldb.ftl";
	
	private Properties configuration;
	public FMTemplate(Properties configuration) {
		this.configuration = configuration;
		System.out.println(configuration.get("baseURI"));
	}
	
	public Model generateMapping(){
		Model model = ModelFactory.createDefaultModel();
		
		Configuration cfg = new Configuration(new Version(majorVersion, minorVersion, microVersion));
		
		TemplateLoader loader = new ClassTemplateLoader(this.getClass(), templateLocation);
		cfg.setTemplateLoader(loader);
	    cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        
		
		try {
			Template template = cfg.getTemplate(templateName);
			
			Writer writer = new StringWriter();
			template.process(configuration, writer);
			Reader reader = new StringReader(writer.toString());
			
			model.read(reader, null, "TURTLE");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return model;
	}
	
	
	public static void main(String[] args) {
		//Map<String, Object> configuration = new HashMap<String, Object>();
		Properties configuration = new Properties();
		configuration.put("dbAddress", "localhost");
		configuration.put("dbPort", "8889");
		configuration.put("dbName", "eswc2016");
		configuration.put("dbUser", "root");
		configuration.put("dbPass", "root");
		
		FMTemplate fmTemplate = new FMTemplate(configuration);
		fmTemplate.generateMapping();
	}
}
