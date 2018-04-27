package com.josericardojunior.RepositoryImporter;

import java.util.HashMap;
import java.util.Map;

public class FileArch {
	public String filename;

	public Map<String, FileClass> classes = new HashMap<String, FileClass>();

	public FileArch(String _filename) {
		filename = _filename;
	}
}
