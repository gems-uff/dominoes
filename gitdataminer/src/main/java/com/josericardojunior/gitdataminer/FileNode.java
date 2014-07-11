package com.josericardojunior.gitdataminer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

public class FileNode extends PatchInfo {

	List<ClassNode> classes = new ArrayList<ClassNode>();
	
	public List<ClassNode> getClasses(){
		return classes;
	}
	
	public FileNode(String _newName, String _oldName, ObjectId _newObjId, ObjectId _oldObjId,
			ChangeType _changeType) {
		super(_newName, _oldName, _newObjId, _oldObjId, _changeType);
	}
	
	public void Parse(Repository _repo){
		
		try {
			classes = ClassNode.Parse(this, _repo);
		} catch (MissingObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void Debug(){
		System.out.println("Name: " + newName);
		System.out.println("--Modification: " + changeType.toString());
		
		for (ClassNode cl : classes){
			cl.Debug();
		}
	}
}
