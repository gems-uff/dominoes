package com.josericardojunior.gitdataminer;

import java.sql.SQLException;

import org.apache.commons.lang.time.StopWatch;

import com.josericardojunior.gitdataminer.Analyzer.Grain;

public class TestBatch {

	public static void main(String[] args) {
		String projName = "Math-Game";
		String repoPath = "/Users/josericardo/Documents/UNLResearch/temp/" 
		+ projName + "/";
		

		RepositoryNode.SaveToDatabase(repoPath, projName);
		
		// Statistics
		Database.Open();
	
		
		/*try {
			System.out.println("Cubic matrix");
			Matrix3D cubic =
					Database.ExtractDevArtifactCube(Grain.FILE, projName, 0,
							Database.NumCommits(projName));
			cubic.Debug();
			
			// Extract support and confidence
			Matrix2D ca = Database.ExtractCommitArtifactMatrix(Grain.FILE, projName);
			
			System.out.println("Commit x Artifact");
			ca.Debug();
		
			// Support
			Matrix2D support = ca.CPUMultiplication(ca.Transpose());
			System.out.println("support");
			support.Debug();
			
			Matrix2D conf = Confidence.Process(support);
			System.out.println("confidence");
			conf.Debug();
			
			Matrix2D lift = Lift.Process(ca.getMatrixDescriptor().getNumCols(), support, conf);
			System.out.println("Lift");
			lift.Debug();
			
			Matrix2D sScore = StandardScore.Process(support);
			System.out.println("Standard Score");
			sScore.Debug();
			
			// Support
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		/*Matrix2D subMatrix = Database.ExtractCommitSubMatrix(Grain.FILE,
				"Math-Game", 0,  Database.NumCommits(projName)
				).Transpose();
		
		subMatrix.Debug();*/
		
		/*System.out.println("Matrix: " + 
				subMatrix.getMatrixDescriptor().getNumRows() + "X" +
				subMatrix.getMatrixDescriptor().getNumCols());
		System.out.println("Elements: " + 
				subMatrix.getMatrixDescriptor().getNumRows() *
				subMatrix.getMatrixDescriptor().getNumCols());
		// Meansure time
		System.out.println("Beggining GPU");
		StopWatch timer = new StopWatch();
		timer.start();
		Matrix2D resGPU = subMatrix.GPUMultiplication(subMatrix.Transpose());
		timer.stop();
		System.out.println("GPU Time (ms): " + timer.getTime());
		resGPU.Debug();
		
	/*	System.out.println("Beggining CPU");
		timer.reset();
		timer.start();
		Matrix2D resCPU = subMatrix.CPUMultiplication(subMatrix.Transpose());
		timer.stop();
		System.out.println("CPU Time (ms): " + timer.getTime());*/
		

		
				
		//supportMat = Analyzer.ExtractSupportTimeSeries(subMatrix, (float)sldMinSupport.getValue() / 100.0f );
		//confidenceMat = Analyzer.ExtractConfidenceTimeSeries(supportMat, (float) sldMinConfidence.getValue() / 100.0f);
		
		/*VolumeChart fileConfidence = new VolumeChart("FileConf");
		Matrix2D _data = confidenceMat.GetSlice(confidenceMat.getMatrixDescriptor().getDepthAt(
				Integer.parseInt((String)cbWindowSize.getSelectedItem())-1));
		fileConfidence.setData(_data);
		fileConfidence.setBorders(GetBorderClass(_data));
		fileConfidence.renderBorder(true);
		chart.setChart(fileConfidence);*/
		

	}

}
