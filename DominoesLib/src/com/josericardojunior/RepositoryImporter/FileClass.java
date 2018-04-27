package com.josericardojunior.RepositoryImporter;

import java.util.ArrayList;
import java.util.List;

public class FileClass {
	public String className;
	public List<String> functions = new ArrayList<String>();
	
	public FileClass(String _className){
		className = _className;
	}
}
