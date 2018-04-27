package com.josericardojunior.util;

import java.io.File;

public class FileUtil {

	public static final String getExtension(File f){
		String ext = null;
		
		String s = f.getName();
		int i = s.lastIndexOf('.');
		
		if (i > 0 && i < s.length() - 1){
			ext = s.substring(i + 1).toLowerCase();
		}
		
		return ext;
	}
}
