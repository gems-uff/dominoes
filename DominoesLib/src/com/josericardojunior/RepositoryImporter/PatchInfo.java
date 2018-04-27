package com.josericardojunior.RepositoryImporter;


import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.ObjectId;

public class PatchInfo {
		
	public String newName;
	public String oldName;
	public ObjectId newObjId;
	public ObjectId oldObjId;
	public ChangeType changeType;
	public String data;
	
	public PatchInfo(String _newName, String _oldName, ObjectId _newObjId, ObjectId _oldObjId, ChangeType _changeType){
		newName = _newName;
		oldName = _oldName;
		newObjId = _newObjId;
		oldObjId = _oldObjId;
		changeType = _changeType;
	}
}
