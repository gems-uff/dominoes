package com.josericardojunior.RepositoryImporter;

import java.util.HashMap;
import java.util.Map;

public class UserNode {

	String name;
	
	static Map<String, UserNode> users = new HashMap<String, UserNode>();
	
	public static Map<String, UserNode> getUsers() {
		return users;
	}

	public String getName(){ return name; }
	
	private UserNode(String _name){
		name = _name;
	}
	
	public static UserNode AddOrRetrieveUser(String id){
		
		if (users.containsKey(id))
			return users.get(id);
		else{
			UserNode un = new UserNode(id);
			users.put(id, un);
			
			return un;
		}
	}
}
