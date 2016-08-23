package org.w3id.scholarlydata.clodg.dogfood;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

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
	
	
	public static void main(String[] args) {
		File dir = new File("tost/alignments");
		for(File f : dir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".rdf");
			}
		})){
			Model model = FileManager.get().loadModel(f.getAbsolutePath());
			try {
				model.write(new FileOutputStream(new File(f.getAbsolutePath().replace(".rdf", ".ttl"))), "N-TRIPLES");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
