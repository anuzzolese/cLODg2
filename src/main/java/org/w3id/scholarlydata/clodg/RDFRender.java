package org.w3id.scholarlydata.clodg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.hp.hpl.jena.util.FileManager;

public class RDFRender {

	
	public static void rdfXml(String in, String out){
		try {
			FileManager.get().loadModel(in).write(new FileOutputStream(new File(out)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			FileManager.get().loadModel("eswc2016_all.ttl").write(new FileOutputStream(new File("eswc2016.rdf")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
