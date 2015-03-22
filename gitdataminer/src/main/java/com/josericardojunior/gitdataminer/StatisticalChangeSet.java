package com.josericardojunior.gitdataminer;

public class StatisticalChangeSet {
	int id;
	int numTrullyChangedFiles;
	int numOccurrences;
	
	public StatisticalChangeSet(int _id){
		id = _id;
	}
	
	public void addOccurence(int _trullyChanged){
		numTrullyChangedFiles += _trullyChanged;
		numOccurrences++;
	}
	
	public float median(){
		return (float)numTrullyChangedFiles / numOccurrences;
	}
	
	public int getNumTrullyChangedFiles(){
		return numTrullyChangedFiles;
	}
	
}
