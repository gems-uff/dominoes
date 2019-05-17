package com.josericardojunior.RepositoryImporter;

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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import com.josericardojunior.dao.DominoesSQLDao;

/* Teste
 * 
 */
 public class CommitNode {

	private CommitNode parent = null;
	private UserNode user;
	private String logMessage;
	private RevCommit revCommit = null;
	private int id = -1;
	
	public int getDatabaseId(){
		return id;
	}
	
	public Date getDate() {
		return date;
	}

	private String hashCode;
	private java.util.Date date;
	private List<FileNode> files = new ArrayList<FileNode>();
	
	public CommitNode(CommitNode _parent, String id, String logMessage, UserNode user, Date date){
		this(id, logMessage, user, date);
		parent = _parent;
	}
	
	public CommitNode(String id, String logMessage, UserNode user, Date date){
		this.hashCode = id;
		this.logMessage = logMessage;
		this.user = user;
		this.date = date;
	}
	
	public CommitNode(String id, String logMessage, UserNode user, Date date, int databaseId){
		this.hashCode = id;
		this.logMessage = logMessage;
		this.user = user;
		this.date = date;
		this.id = databaseId;
	}
	
	public CommitNode(RevCommit revCommit){
		this.revCommit = revCommit;
		this.hashCode = revCommit.getId().toString();
		this.user = UserNode.AddOrRetrieveUser(revCommit.getAuthorIdent().getName());
		this.logMessage = revCommit.getFullMessage();
		this.date = revCommit.getAuthorIdent().getWhen();
	}
	
	public void Debug(){
		System.out.println("Commit Message: " + logMessage + " User: " + user.getName());
		
		for (FileNode f : files){
			f.Debug();
		}
	}
	
	
	/*final public static List<CommitNode> ExtractCommit(Repository _repo){
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
	}*/
	
	
	
	public void parse(RepositoryNode _repo) {
		
		RevWalk rw = new RevWalk(_repo.getRepository());
		try {
			/*if (commit == null) {
				ObjectId object = getDefaultBranch(repository);
				commit = rw.parseCommit(object);
			}*/

			if (revCommit.getParentCount() == 0) {
				TreeWalk tw = new TreeWalk(_repo.getRepository());
				tw.reset();
				tw.setRecursive(true);
				tw.addTree(revCommit.getTree());

				
				while (tw.next()) {
					
					if (isJavaFile(tw.getPathString())){
						files.add(new FileNode(tw.getPathString(), tw.getPathString(),
							tw.getObjectId(0), null, ChangeType.ADD));
					}
				}
				//tw.release();
			} else {
				RevCommit parent = rw.parseCommit(revCommit.getParent(0).getId());
				DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
				df.setRepository(_repo.getRepository());	
				df.setDiffComparator(RawTextComparator.DEFAULT);
				df.setDetectRenames(true);
				List<DiffEntry> diffs = df.scan(parent.getTree(), revCommit.getTree());
				
				for (DiffEntry diff : diffs) {
					
					if (diff.getChangeType().equals(ChangeType.ADD)) {
						if (isJavaFile(diff.getNewPath())){
							files.add(new FileNode(diff.getNewPath(), null,
								diff.getNewId().toObjectId(), null, diff.getChangeType()));
						}
					} else if (diff.getChangeType().equals(ChangeType.DELETE)) {
						if (isJavaFile(diff.getOldPath())){
							files.add(new FileNode(null, diff.getOldPath(),
								null, diff.getOldId().toObjectId(), diff.getChangeType()));
						}
					} else if (diff.getChangeType().equals(ChangeType.RENAME)) {
						if (isJavaFile(diff.getNewPath())){
							files.add(new FileNode(diff.getNewPath(), diff.getOldPath(),
								diff.getNewId().toObjectId(), diff.getOldId().toObjectId(), diff.getChangeType()));
						}
					} else if (diff.getChangeType().equals(ChangeType.MODIFY)){
						if (isJavaFile(diff.getNewPath())){
							files.add(new FileNode(diff.getNewPath(), null,
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
		
		
		for (FileNode f : files){
			f.Parse(_repo.getRepository());
		}
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

	public String getHashCode() {
		return hashCode;
	}

	public List<FileNode> getFiles() {
		return files;
	}
}

 

