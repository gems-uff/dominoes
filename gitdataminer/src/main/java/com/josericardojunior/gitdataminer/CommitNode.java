package com.josericardojunior.gitdataminer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

/* Teste
 * 
 */
 public class CommitNode {

	private CommitNode parent = null;
	private UserNode user;
	private String logMessage;
	
	public java.util.Date getDate() {
		return date;
	}

	private String id;
	private java.util.Date date;
	private List<FileNode> files = new ArrayList<FileNode>();
	
	public CommitNode(CommitNode _parent){
		parent = _parent;
	}
	
	public CommitNode(){
		
	}
	
	public void Debug(){
		System.out.println("Commit Message: " + logMessage + " User: " + user.getName());
		
		for (FileNode f : files){
			f.Debug();
		}
	}
	
	final public static List<CommitNode> ExtractCommit(Repository _repo){
		List<CommitNode> commits = new ArrayList<CommitNode>();
		
		RevWalk rw = new RevWalk(_repo);
		AnyObjectId id;
		
		try {
			id = _repo.resolve(Constants.HEAD);
			RevCommit root = rw.parseCommit(id);
			rw.sort(RevSort.REVERSE);
			rw.markStart(root);
			
			RevCommit c = null;
			while ((c = rw.next()) != null){
				CommitNode commit = Parse(c, _repo, null);
				for (FileNode f : commit.files){
					f.Parse(_repo);
				}
				
				commit.user = UserNode.AddOrRetrieveUser(c.getAuthorIdent().getName());
				commit.logMessage = c.getFullMessage();
				commit.id = c.getId().toString();
				commits.add(commit);
			}
			
		} catch (AmbiguousObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return commits;
	}
	
	
	
	private static CommitNode Parse(RevCommit _revCommit, Repository _repo, Date later){
		
		return ExtractCommitInfo(_revCommit, _repo, later);
	}
	
	private static CommitNode ExtractCommitInfo(RevCommit _revCommit, Repository _repo, Date later) {
		
		CommitNode c = new CommitNode(null);

		
		RevWalk rw = new RevWalk(_repo);
		try {
			/*if (commit == null) {
				ObjectId object = getDefaultBranch(repository);
				commit = rw.parseCommit(object);
			}*/

			if (_revCommit.getParentCount() == 0) {
				TreeWalk tw = new TreeWalk(_repo);
				tw.reset();
				tw.setRecursive(true);
				tw.addTree(_revCommit.getTree());
				c.date = _revCommit.getAuthorIdent().getWhen();
				
				if ( (later != null) && c.date.before(later)){
					return null;
				}
				
				while (tw.next()) {
					
					if (isJavaFile(tw.getPathString())){
						c.files.add(new FileNode(tw.getPathString(), tw.getPathString(),
								tw.getObjectId(0), null, ChangeType.ADD));
					}
				}
				tw.release();
			} else {
				RevCommit parent = rw.parseCommit(_revCommit.getParent(0).getId());
				DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
				df.setRepository(_repo);	
				df.setDiffComparator(RawTextComparator.DEFAULT);
				df.setDetectRenames(true);
				List<DiffEntry> diffs = df.scan(parent.getTree(), _revCommit.getTree());
				c.date = _revCommit.getAuthorIdent().getWhen();
				
				if ((later != null) && c.date.before(later)){
					return null;
				}
				
				for (DiffEntry diff : diffs) {
					
					if (diff.getChangeType().equals(ChangeType.ADD)) {
						if (isJavaFile(diff.getNewPath())){
							c.files.add(new FileNode(diff.getNewPath(), null,
								diff.getNewId().toObjectId(), null, diff.getChangeType()));
						}
					} else if (diff.getChangeType().equals(ChangeType.DELETE)) {
						if (isJavaFile(diff.getOldPath())){
							c.files.add(new FileNode(null, diff.getOldPath(),
								null, diff.getOldId().toObjectId(), diff.getChangeType()));
						}
					} else if (diff.getChangeType().equals(ChangeType.RENAME)) {
						if (isJavaFile(diff.getNewPath())){
							c.files.add(new FileNode(diff.getNewPath(), diff.getOldPath(),
								diff.getNewId().toObjectId(), diff.getOldId().toObjectId(), diff.getChangeType()));
						}
					} else if (diff.getChangeType().equals(ChangeType.MODIFY)){
						if (isJavaFile(diff.getNewPath())){
							c.files.add(new FileNode(diff.getNewPath(), null,
								diff.getNewId().toObjectId(), diff.getOldId().toObjectId(), diff.getChangeType()));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			rw.dispose();
		}
		
		return c;
	}
	
	private static boolean isJavaFile(String file){

        int lastSlash = file.lastIndexOf(File.separatorChar);
        String filename = file.substring(lastSlash + 1);
        int last_dot = filename.lastIndexOf('.');
        String extension = filename.substring(last_dot + 1);

        if (extension.compareTo("java") == 0) {
            return true;
        }
        
        return false;
	}

	public UserNode getUser() {
		return user;
	}

	public String getLogMessage() {
		return logMessage;
	}

	public String getId() {
		return id;
	}

	public List<FileNode> getFiles() {
		return files;
	}

	public static void SaveToDatabase(RepositoryNode repoNode,
			Date lastCommitDate) {
		
		RevWalk rw = new RevWalk(repoNode.getRepository());
		AnyObjectId id;
		int counter = 0;
		
		try {
			id = repoNode.getRepository().resolve(Constants.HEAD);
			RevCommit root = rw.parseCommit(id);
			rw.sort(RevSort.REVERSE);
			rw.markStart(root);
			
			RevCommit c = null;
			//Map<String, String> v = new HashMap<String, String>();
			
			while ((c = rw.next()) != null){
				CommitNode commit = Parse(c, 
						repoNode.getRepository(), lastCommitDate);
				commit.id = c.getId().toString();
				
				if (commit != null){
					for (FileNode f : commit.files){
						f.Parse(repoNode.getRepository());
					}
				
					
					commit.user = UserNode.AddOrRetrieveUser(c.getAuthorIdent().getName());
					commit.logMessage = c.getFullMessage();
					
					System.out.println("Processados: " + counter);
					Database.AddCommit(commit, repoNode);
					counter++;
					
				//	if (!v.containsKey(commit.user.name)){
					//	v.put(commit.user.name, c.getCommitterIdent().getEmailAddress());
					//}
				}
			}
			
			//for (String key : v.keySet()){
				//System.out.println(key + " - " + v.get(key));
			//}
			
		} catch (AmbiguousObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

 

