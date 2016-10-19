package org.w3id.scholarlydata.clodg;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class TurtleConverter {

public static void main(String[] args) {
		
		File folder = new File("/Library/WebServer/Documents/scholarlydata.org/website/dumps/conferences/temp");
		File outFolder = new File("/Library/WebServer/Documents/scholarlydata.org/website/dumps/conferences/temp/ttl");
		if(!outFolder.exists()) outFolder.mkdirs();
		for (File file : folder.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".rdf") || pathname.getName().endsWith(".owl");
			}
		})){
			Model model = FileManager.get().loadModel(file.getAbsolutePath());
			try {
				Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outFolder, file.getName().replace(".rdf", ".ttl").replace(".owl", ".ttl"))), "UTF-8");
				model.write(writer, "N-TRIPLES");
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
}
