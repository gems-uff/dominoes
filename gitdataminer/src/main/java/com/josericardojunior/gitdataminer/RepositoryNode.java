package com.josericardojunior.gitdataminer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;

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

	Git git;
	Repository gitRepository;
	String gitRepositoryPath;
	String name;
	List<CommitNode> commits = new ArrayList<CommitNode>();
	
	Repository getRepository(){ 
		return gitRepository;
	}
	
	
	public RepositoryNode(String _name, String _location) {
		name = _name;
		gitRepositoryPath = _location;
		
		try {
			gitRepository = new FileRepository(new File(gitRepositoryPath + ".git"));
			git = new Git(gitRepository);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Parse(){
		commits = CommitNode.ExtractCommit(gitRepository);
	}
	
	public void Debug(){
		System.out.println("Repository: " + name + " Path: " + gitRepositoryPath);
		
		for (CommitNode c : commits)
			c.Debug();
	}
	
	public static void SaveToDatabase(String repoPath, String repoName){
		RepositoryNode repoNode = new RepositoryNode(repoName, repoPath);
		
		
		Database.Open();
		Date lastCommitDate = Database.AddRepository(repoNode);
		CommitNode.SaveToDatabase(repoNode, lastCommitDate);
		
		try {
			Database.MineBugs("derby-", repoNode);
		} catch (SQLException ex){
			ex.printStackTrace();
		}
		Database.UpdateRepoToLastCommit(repoNode);
		
		Database.Close();
		
	}
}
