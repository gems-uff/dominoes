package com.josericardojunior.gitdataminer;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;

public class TestChangeSets {

	public static void main(String[] args) {
		
		
		// TODO Auto-generated method stub
		String projName = "derby";
		String repoPath = "//home/josericardo/Projects/" 
				+ projName + "/";


		// Meansure time
		StopWatch timer = new StopWatch();
		timer.start();
		RepositoryNode.SaveToDatabase(repoPath, projName);
		timer.stop();
		Database.Open();
		
		System.out.println("Time (ms): " + timer.getTime() );
		
		System.out.println("--------- Statistics --------");
		System.out.println("Repository: " + projName);
		System.out.println("Commits: " +  Database.NumCommits(projName));
		System.out.println("Time (ms): " + timer.getTime() );
		
		
		
		StringBuffer outresume = new StringBuffer();
		outresume.append("changeid,date,time,changesetsize,trullychanged,classeschanged,methodschanged\n");
		
		try {
			Map<String, Commit> res = Database.Unserialize(projName);
			
			int commit = 0;
			for (Map.Entry<String,Commit> _commit : res.entrySet() ) {
				String _com = _commit.getKey() + " - " ;
				
				int numFiles = 0;
				int numClasses = 0;
				int numFunctions = 0;
				int trullyChanged = 0;
				
				System.out.println("Processing commit " + commit);
				
				
				
				for (Map.Entry<String,FileArch> _file : _commit.getValue().files.entrySet()){
					_com += _file.getKey() + " - ";
					
					numFiles++;
					
					if (_file.getValue().classes.size() > 0) 
						trullyChanged++;
					
					for (Map.Entry<String,FileClass> _fileClass : _file.getValue().classes.entrySet() ){
						_com += _fileClass.getKey() + " - ";
						
						numClasses++;
						
						for (String function : _fileClass.getValue().functions){
							numFunctions++;
						}
					}													
				}
				
				commit++;
					
				
				
				outresume.append(_commit.getKey() + ",");
				outresume.append(new SimpleDateFormat( "dd-MM-yyyy" ).format( _commit.getValue().date ) + ",");
				outresume.append(new SimpleDateFormat( "HH:mm:ss" ).format( _commit.getValue().date ) + ",");
				outresume.append(numFiles + "," );
				outresume.append(trullyChanged + "," );
				outresume.append(numClasses + "," );
				outresume.append(numFunctions + "\n" );				
			}
			
			
			
			
			System.out.println(outresume);
			
			// Export whole system ED and EDB
			PrintWriter wholeSystemDeveloperED_EBD = new PrintWriter("ChangeSetStatistics.txt", "UTF-8");
			wholeSystemDeveloperED_EBD.println(outresume.toString());
			wholeSystemDeveloperED_EBD.close();
			
			System.out.println("Finish");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
