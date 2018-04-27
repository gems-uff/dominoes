package com.josericardojunior.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.filechooser.FileFilter;

public class JGitDirectoryFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()){
			return true;
		}
		
		return false;
	}
		

	@Override
	public String getDescription() {
		return "Git Repository";
	}

}
