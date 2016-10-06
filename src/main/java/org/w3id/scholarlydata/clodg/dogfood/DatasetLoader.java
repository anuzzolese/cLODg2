package org.w3id.scholarlydata.clodg.dogfood;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class DatasetLoader {

	public static Model load(File file){
		Model model = ModelFactory.createDefaultModel();
		
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
			model.read(inputStream, null, "RDF/XML");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model;
	}
	
}
