package com.josericardojunior.RepositoryImporter;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.time.StopWatch;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;


public class RepositoryNode {
	
	public enum Detail {
		File,
		Class,
		Method
	}
	
	public String getName() {
		return name;
	}

	public List<CommitNode> getCommits() {
		return commits;
	}
	
	public String getLocation(){
		return gitRepositoryPath;
	}
	
	public String getBugPrefix(){
		return bugPrefix;
	}

	Git git;
	Repository gitRepository;
	String gitRepositoryPath;
	String name;
	String bugPrefix;
	List<CommitNode> commits = new ArrayList<CommitNode>();
	Date lastCommitDate = null;
	int lastCommitId = -1;
	private RevWalk rw;
	

	public Date getLastCommitDate(){
		return lastCommitDate;
	}
	
	public int getLastCommitId(){
		return lastCommitId;
	}
	
	
	Repository getRepository(){ 
		return gitRepository;
	}
	
	
	public RepositoryNode(File _repo, String bugPrefix) {
		String _location = _repo.getAbsolutePath();
		name = _location.substring(_location.lastIndexOf(File.separatorChar) + 1,
				_location.length());
		gitRepositoryPath = _location;
		this.bugPrefix = bugPrefix;
		
		try {
			gitRepository = new FileRepository(new File(gitRepositoryPath + File.separatorChar + ".git"));
			git = new Git(gitRepository);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public RepositoryNode(String _name, String _location, int _lastCommitId, String bugPrefix,
			Date lastCommitDate) {
		name = _name;
		gitRepositoryPath = _location;
		lastCommitId = _lastCommitId;
		this.bugPrefix = bugPrefix;
		this.lastCommitDate = lastCommitDate;
		
		try {
			gitRepository = new FileRepository(new File(gitRepositoryPath + File.separatorChar + ".git"));
			git = new Git(gitRepository);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getTotalCommits(){
	    Iterable<RevCommit> commits = null;
	    
		try {
			commits = git.log().call();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int count = 0;
		
		for (RevCommit commit : commits){
			if (getLastCommitDate() == null)
				count++;
			else if (commit.getAuthorIdent().getWhen().after(getLastCommitDate()))
				count++;
		}
		
		return count;
	}
	
	public void Debug(){
		System.out.println("Repository: " + name + " Path: " + gitRepositoryPath);
		
		for (CommitNode c : commits)
			c.Debug();
	}
	
	
	public CommitNode getNextCommit() {
		CommitNode commitNode = null;
		RevCommit c = null;
		
		try {
			if ( (c = rw.next()) != null){
				commitNode = new CommitNode(c);
			}
		} catch (MissingObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return commitNode;		
	}
	
	public void setLastCommit(CommitNode commitNode){
		// Retrieve the ID of the last commit
		this.lastCommitDate = commitNode.getDate();
		this.lastCommitId = commitNode.getDatabaseId();
	}
	
	public void update(){
		rw = new RevWalk(getRepository());
		AnyObjectId id;
		
		 if (getLastCommitDate() != null){
		    	RevFilter filter = CommitTimeRevFilter.after(getLastCommitDate());
		    	rw.setRevFilter(filter);
		 }
		
		try {
			id = getRepository().resolve(Constants.HEAD);
			RevCommit root = rw.parseCommit(id);
			rw.sort(RevSort.REVERSE);
			rw.markStart(root);	
		} catch (AmbiguousObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
