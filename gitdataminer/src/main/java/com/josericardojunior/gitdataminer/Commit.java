package com.josericardojunior.gitdataminer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class Commit {
	public String hash;
	public String user;
	public Date date;
	
	public Commit(String _hash, String _user){
		hash = _hash;
		user = _user;
		
	}
	
	public  Map<String, FileArch> files = new HashMap<String, FileArch>();
}
