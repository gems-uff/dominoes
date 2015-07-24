package boundary;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import dao.DominoesSQLDao;
import RepositoryImporter.CommitNode;
import RepositoryImporter.RepositoryNode;


public class ImporterWorker extends SwingWorker<String, String> {
	private JTextPane textArea;
	RepositoryNode repoNode;

	@Override
	protected String doInBackground() throws Exception {
		
		repoNode.update();
		CommitNode commitNode = null;
		int total = repoNode.getTotalCommits();
		int count = 1;
		
		
		while ( (commitNode = repoNode.getNextCommit()) != null){
			if (repoNode.getLastCommitDate() != null){
				if (commitNode.getDate().after(repoNode.getLastCommitDate())){
					publish("\nProcessing commit " + count + " of " + total);
					commitNode.parse(repoNode);
					DominoesSQLDao.addCommit(commitNode, repoNode);
					count++;
				}
			} else {
				publish("\nProcessing commit " + count + " of " + total);
				commitNode.parse(repoNode);
				DominoesSQLDao.addCommit(commitNode, repoNode);
				count++;
			}
					
			Thread.sleep(1000);
		}
		
		if (repoNode.getBugPrefix() != null && repoNode.getBugPrefix().length() > 0){
			publish("\nMining bug...");
			Thread.sleep(1000);
			DominoesSQLDao.MineBugs(repoNode);
		}
		
		CommitNode firstCommit = DominoesSQLDao.getFirstCommit(repoNode);
		CommitNode lastNode = DominoesSQLDao.getLastCommit(repoNode);
		repoNode.setLastCommit(lastNode);
		
		DominoesSQLDao.updateRepot(repoNode);
		publish("\nFirst commit: " + firstCommit.getDate());
		publish("\nLast commit: " + lastNode.getDate());
		
		return null;
	}
	
	public ImporterWorker(JTextPane textArea, RepositoryNode repoNode){
		this.textArea = textArea;
		this.repoNode = repoNode;
	}
	
	@Override
	protected void done(){
		textArea.setText(textArea.getText() + "\nFinished");
	}
	
	@Override
	protected void process(List<String> chunks){
		for (String chunk : chunks)
			textArea.setText(textArea.getText() + chunk);
	}


}
