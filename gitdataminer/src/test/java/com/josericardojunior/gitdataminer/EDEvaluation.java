package com.josericardojunior.gitdataminer;

import java.sql.SQLException;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.time.StopWatch;

import com.josericardojunior.gitdataminer.Analyzer.Grain;
import com.josericardojunior.gitdataminer.Analyzer.InfoType;
import com.josericardojunior.gitdataminer.Database.CubeOptions;
import com.josericardojunior.gitdataminer.Definitions.Algorithm;

public class EDEvaluation {

	public static void main(String[] args) {
		
		String projName = "derby";
		String repoPath = "/home/josericardo/Projects/dominoes/" 
				+ projName + "/";
		String fileName = "MaxMonthEDFile.txt";
		StopWatch timer = new StopWatch();
		
		Database.Open();
		try {
			System.out.println("------ Cube Time Experiment --------");
						
			System.out.println("**Extracting ED cube at file level**");
			timer.reset();
			timer.start();
			Matrix3D cubeED = Database.ExtractDevArtifactSupportCube(Grain.FILE, projName, null, 
					CubeOptions.Max_Commit_Month, 10, 1);
			timer.stop();	
			System.out.println("---Time (ms): " + timer.getTime());

			
			System.out.println("**Processing ED Cube z-score**");
			timer.reset();
			timer.start();			
			cubeED = StandardScore.Process(cubeED, Algorithm.GPU);
			timer.stop();	
			System.out.println("---Time (ms): " + timer.getTime());
			
			// Flatten the cube
			System.out.println("Flattening ED Cube");
			timer.reset();
			timer.start();		
			Matrix2D cubeEDfirst = cubeED.getLayer(Database.CUBE_DEPTH_PREFIX + 0);
			MatrixDescriptor cubeEDfirstDesc = cubeEDfirst.getMatrixDescriptor();
			MatrixDescriptor cubeEDflattenDesc = new MatrixDescriptor(InfoType.USER, InfoType.ARTIFACT);
						
			for (int i = 0; i < cubeEDfirstDesc.getNumRows(); i++)
				cubeEDflattenDesc.AddRowDesc(cubeEDfirstDesc.getRowAt(i));
						
			for (int i = 0; i < cubeED.getNumLayers(); i++)
				cubeEDflattenDesc.AddColDesc(Database.CUBE_DEPTH_PREFIX + i);
						
			Matrix2D cubeEDflatten = new Matrix2D(cubeEDflattenDesc);
						
									
			Set<Entry<String, Matrix2D>> cubeEDLayers = cubeED.getIterator();
			for (Entry<String, Matrix2D> entry : cubeEDLayers ){
				Matrix2D _mat = entry.getValue();
				MatrixDescriptor _matDesc = _mat.getMatrixDescriptor();
							
				for (int row = 0; row < _matDesc.getNumRows(); row++ ){
					float count = _mat.SumRow(row);
								
					cubeEDflatten.SetElement(_matDesc.getRowAt(row), entry.getKey(), count);
				}				
			}
			timer.stop();	
			System.out.println("---Time (ms): " + timer.getTime());
			
			
			System.out.println("**Exporting to file**");
			timer.reset();
			timer.start();	
			cubeEDflatten.ExportCSV("MaxByMonthEDile.txt"); 
			timer.stop();	
			System.out.println("---Time (ms): " + timer.getTime());
			System.out.println("Finish");
			
			// Find a 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
