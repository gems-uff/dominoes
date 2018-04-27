package com.josericardojunior.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class JSQLiteFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()){
			return false;
		}
		
		String extension = FileUtil.getExtension(f);
		
		if (extension != null && extension.equals("sqlite")){
			return true;
		}
		
		return false;
	}

	@Override
	public String getDescription() {
		return "SQLite database";
	}

}
