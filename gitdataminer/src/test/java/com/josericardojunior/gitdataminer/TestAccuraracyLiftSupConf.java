package com.josericardojunior.gitdataminer;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.apache.commons.lang.time.StopWatch;

import com.josericardojunior.gitdataminer.Analyzer.Grain;

public class TestAccuraracyLiftSupConf {
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		String projName = "derby";
		String repoPath = "//home/josericardo/Projects/" 
				+ projName + "/";
		
		boolean usingMethod = false;


		// Meansure time
		StopWatch timer = new StopWatch();
		timer.start();
		//RepositoryNode.SaveToDatabase(repoPath, projName);
		//timer.stop();
		Database.Open();
		
		//System.out.println("Time (ms): " + timer.getTime() );
		
		System.out.println("--------- Statistics --------");
		System.out.println("Repository: " + projName);
		System.out.println("Commits: " +  Database.NumCommits(projName));
		//System.out.println("Time (ms): " + timer.getTime() );
		
		try {
			//Matrix2D FCM = Database.ExtractFileClassMethodComposition(projName);
			Matrix2D CA = Database.ExtractCommitArtifactMatrix(Grain.FILE, projName, null);
			System.out.println("CA " + 
					(usingMethod ? "(Method)" : "(File)") +
					": rows" + CA.getMatrixDescriptor().getNumRows() + " Cols: " + CA.getMatrixDescriptor().getNumCols());
			
			
			System.out.println("calculating support...");
			Matrix2D support = CA.Transpose().CPUMultiplication(CA);
			
						
			//System.out.println("exporting support...");
			//support.SaveToDatabase("tmp1", "DerbySup");
			System.out.println("calculating confidence...");
			Matrix2D confidence =  Confidence.Process(support);
			//System.out.println("exporting confidence...");
			//support.SaveToDatabase("tmp1", "DerbyConf");
			System.out.println("calculating lift...");
			//Matrix2D lift = Lift.Process(CA.getMatrixDescriptor().getNumRows(), support, confidence);
			//System.out.println("exporting lift...");
		//	lift.SaveToDatabase("tmp1", "DerbyLift");
			
			
			if (usingMethod){
			// 
				/*System.out.println("Calculating support based on methods...");
				Matrix2D supFlFromMethos = FCM.GPUMultiplication(support).GPUMultiplication(FCM.Transpose());
				
				System.out.println("Calculating confidence based on methods...");
				Matrix2D confFlFromMethos = FCM.GPUMultiplication(support).GPUMultiplication(FCM.Transpose());
				
				System.out.println("Calculating lift based on methods...");
				Matrix2D liftFlFromMethos = FCM.GPUMultiplication(support).GPUMultiplication(FCM.Transpose());
				
				// extract top values
				System.out.println("saving top support...");
				//SaveTopValues(supFlFromMethos, "supportM_max.txt");
				System.out.println("saving top confidence...");
				//SaveTopValues(confFlFromMethos, "confidenceM_max.txt");
				System.out.println("saving top lift..");
				//SaveTopValues(liftFlFromMethos, "liftM_max.txt");*/
			} else {
				// extract top values
				System.out.println("saving top support...");
				SaveTopValues(support, confidence, "supportM4_max.txt", 1, 0);
				System.out.println("saving top confidence...");
				SaveTopValues(support, confidence, "confidenceM3_max.txt", 2, 10);
				//System.out.println("saving top lift..");
				//SaveTopValues(support, confidence, lift, "liftM_max.txt", 3);
				
			}
			Database.Close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void SaveTopValues(Matrix2D support, Matrix2D confidence, 
			/*Matrix2D lift,*/ String filename, int type, int supthreshold) throws FileNotFoundException, UnsupportedEncodingException {
		
		PrintWriter _file = new PrintWriter(filename, "UTF-8");
		
		Matrix2D _main;
		Matrix2D _aux1;
		//Matrix2D _aux2;
				
		if (type == 1){
			_main = support;
			_aux1 = confidence;
		//	_aux2 = lift;
			_file.println("artifact;support1;support2;confidence1;confidence2;artifact");
			
		} else/* if (type == 2)*/{
			_main = confidence;
			_aux1 = support;
			//_aux2 = lift;
			_file.println("artifact;confidence1;confidence2;support1;support2;artifact");
		} /*else {
			_main = lift;
			_aux1 = support;
			_aux2 = confidence;
			_file.println("artifact;lift;support;confidence;artifact");
		}*/
		MatrixDescriptor _mainDesc = _main.getMatrixDescriptor();
		MatrixDescriptor _aux1Desc = _aux1.getMatrixDescriptor();
		//MatrixDescriptor _aux2Desc = _aux2.getMatrixDescriptor();
		
		
		for (int i = 0; i < _mainDesc.getNumRows(); i++){
			
			String current = _mainDesc.getRowAt(i);
			//String current_max = "";
			//float max = -1;
			
			for (int j = i+1; j < _mainDesc.getNumCols(); j++){
								
				
				float _v = _main.GetElement(i, j);
								
				if (_v > supthreshold){
					String other = _mainDesc.getColumnAt(j);
					float _aux2_v = _aux1.GetElement(other, current);					
					float aux1_v = _aux1.GetElement(current, other);
					float max_2 = _main.GetElement(other, current);
					_file.println(current + ";" + _v + ";" + max_2 + ";" + aux1_v + ";" + _aux2_v + ";" + other);
				}
			}
			
			//float aux1_v = _aux1.GetElement(current, current_max);
			//float _aux2_v = _aux1.GetElement(current_max, current);
			//float aux2_v = _aux2.GetElement(current, current_max);
			//float max_2 = _main.GetElement(current_max, current);
			
			//_file.println(current + ";" + max + ";" + max_2 + ";" + aux1_v + ";" + _aux2_v + ";" + current_max);
						
		}
		
		_file.close();
	}

}