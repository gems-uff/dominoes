package com.josericardojunior.gitdataminer;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Panel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.SliderUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextPane;
import javax.swing.JButton;

import charts3D.HeighmapChart;
import charts3D.VolumeChart;

import com.josericardojunior.gitdataminer.Analyzer.Grain;
import com.josericardojunior.gitdataminer.Analyzer.InvalidMatrix;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JScrollBar;

import org.apache.commons.lang.time.StopWatch;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;







import org.lwjgl.util.vector.Vector3f;


//import javax.vecmath.*;
//import javax.media.j3d.*;
import javax.swing.JComboBox;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JRadioButton;

import java.awt.Font;

import javax.swing.JList;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;


public class MainApp extends JFrame {

	private JPanel contentPane;
	private JTable dep;
	private JTable confidence;
	JSlider slStartCommit;
	JScrollPane scrollPane_1;
	JScrollPane scrollPane;
	Panel3D chart;
	//JPanel chart;
	
	Matrix3D supportMat;
	Matrix3D confidenceMat;
	private JLabel lblStartCommit;
	private JRadioButton rdbtnFile;
	private JRadioButton rdbtnClass;
	private JRadioButton rdbtnMethod;
	private JLabel lblProject;
	private JComboBox cbProjectSel;
	private JComboBox cbWindowSize;
	private Grain currentGrain = Grain.FILE;
	private JSlider sldMinSupport;
	private JSlider sldMinConfidence;

	
	
	
public class MatrixTable extends AbstractTableModel {
		
		Matrix2D mat;
		
		public MatrixTable(Matrix2D _mat) {
			mat = _mat;
		}
		
		public String getColumnName(int column){
			if (column == 0)
				return "";
			
			return mat.getMatrixDescriptor().getColumnAt(column-1);
		}

		public int getRowCount() {
			MatrixDescriptor desc = mat.getMatrixDescriptor();
			return desc.getNumRows();
		}

		public int getColumnCount() {
			MatrixDescriptor desc = mat.getMatrixDescriptor();
			return desc.getNumCols() + 1;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0){
				MatrixDescriptor desc = mat.getMatrixDescriptor();
				return desc.getRowAt(rowIndex);
			}
			
			return  mat.GetElement(rowIndex, columnIndex-1);
		}
		
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		String projName = "derby";
		String repoPath = "/Users/josericardo/tmp/repos/" 
				+ projName + "/";
				

		RepositoryNode.SaveToDatabase(repoPath, projName);
		
		// Statistics
		Database.Open();
		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainApp frame = new MainApp();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		//Database.Close();
	}

	/**
	 * Create the frame.
	 */
	public MainApp() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 976, 809);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblSupport = new JLabel("Dep");
		lblSupport.setBounds(452, 45, 61, 16);
		contentPane.add(lblSupport);
		
		JLabel lblConfidence = new JLabel("Confidence");
		lblConfidence.setBounds(16, 45, 80, 16);
		contentPane.add(lblConfidence);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(452, 79, 363, 95);
		contentPane.add(scrollPane);
		
		dep = new JTable();
		dep.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrollPane.setViewportView(dep);
		
		slStartCommit = new JSlider();
		slStartCommit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				slStartCommit.setToolTipText("Teste");
			}
		});
		slStartCommit.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider _slider = (JSlider) e.getSource();
				
				if (_slider.getValueIsAdjusting()){
					_slider.setToolTipText(Integer.toString(_slider.getValue()));
					UpdateWindowSize((_slider.getMaximum()-_slider.getValue()) + 1);					
				}
			}
		});
		slStartCommit.setSnapToTicks(true);
		slStartCommit.setPaintTicks(true);
		slStartCommit.setMinorTickSpacing(1);
		slStartCommit.setMajorTickSpacing(1);
		slStartCommit.setBounds(16, 217, 190, 29);
		contentPane.add(slStartCommit);
		
		JButton btnUpdate = new JButton("Update");
		btnUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				UpdateVisualization();
				//scrollPane.setViewportView(support);
			}
		});
		btnUpdate.setBounds(655, 338, 117, 29);
		contentPane.add(btnUpdate);
		
		scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(16, 79, 382, 95);
		contentPane.add(scrollPane_1);
		
		confidence = new JTable();
		confidence.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrollPane_1.setViewportView(confidence);
		
		lblStartCommit = new JLabel("Start Commit");
		lblStartCommit.setBounds(16, 196, 106, 16);
		contentPane.add(lblStartCommit);
		
		chart = new Panel3D();
		//chart = new JPanel();
		chart.setBounds(41, 418, 774, 345);

		contentPane.add(chart);
		
		JLabel lblMinSupport = new JLabel("Min. Support (%)");
		lblMinSupport.setBounds(338, 196, 106, 16);
		contentPane.add(lblMinSupport);
		
		sldMinSupport = new JSlider();
		sldMinSupport.setValue(1);
		sldMinSupport.setMinorTickSpacing(1);
		sldMinSupport.setMajorTickSpacing(1);
		sldMinSupport.setBounds(338, 217, 190, 29);
		contentPane.add(sldMinSupport);
		
		JLabel lblMinConfidence = new JLabel("Min. Confidence (%)");
		lblMinConfidence.setBounds(540, 196, 136, 16);
		contentPane.add(lblMinConfidence);
		
		sldMinConfidence = new JSlider();
		sldMinConfidence.setValue(1);
		sldMinConfidence.setMinorTickSpacing(1);
		sldMinConfidence.setMajorTickSpacing(1);
		sldMinConfidence.setBounds(540, 217, 190, 29);
		contentPane.add(sldMinConfidence);
		
		JPanel pnlArtifact = new JPanel();
		pnlArtifact.setBackground(Color.LIGHT_GRAY);
		pnlArtifact.setBounds(16, 286, 94, 120);
		contentPane.add(pnlArtifact);
		pnlArtifact.setLayout(null);
		
		JLabel lblGrain = new JLabel("Grain");
		lblGrain.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblGrain.setBounds(27, 6, 61, 16);
		pnlArtifact.add(lblGrain);
		
		rdbtnFile = new JRadioButton("File");
		rdbtnFile.setSelected(true);
		rdbtnFile.setBounds(6, 34, 97, 23);
		pnlArtifact.add(rdbtnFile);
		
		rdbtnClass = new JRadioButton("Class");
		rdbtnClass.setBounds(6, 62, 141, 23);
		pnlArtifact.add(rdbtnClass);
		
		rdbtnMethod = new JRadioButton("Method");
		rdbtnMethod.setBounds(6, 90, 141, 23);
		pnlArtifact.add(rdbtnMethod);
		
		ButtonGroup bgArtifact = new ButtonGroup();
		bgArtifact.add(rdbtnFile);
		bgArtifact.add(rdbtnClass);
		bgArtifact.add(rdbtnMethod);
		
		ActionListener rdbFine = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rdbtnFile.equals(e.getSource()))
					currentGrain = Grain.FILE;
				if (rdbtnClass.equals(e.getSource()))
					currentGrain = Grain.CLASS;
				if (rdbtnMethod.equals(e.getSource()))
					currentGrain = Grain.METHOD;
			}
		};
		rdbtnFile.addActionListener(rdbFine);
		rdbtnClass.addActionListener(rdbFine);
		rdbtnMethod.addActionListener(rdbFine);
		
		lblProject = new JLabel("Project");
		lblProject.setBounds(16, 17, 61, 16);
		contentPane.add(lblProject);
		
		try {
			// Recover all available projects
			String projects[];
			projects = Database.AvailableProjects();
			cbProjectSel = new JComboBox(projects);
			cbProjectSel.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent event) {					
					
					if (event.getStateChange() == ItemEvent.SELECTED){
						FillWindowSize((String) event.getItem());
					}
					
				}
			});
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		cbProjectSel.setBounds(66, 13, 332, 27);
		contentPane.add(cbProjectSel);
		
		cbWindowSize = new JComboBox(new Object[]{});
		cbWindowSize.setBounds(209, 217, 106, 27);
		contentPane.add(cbWindowSize);
		
		JLabel lblWindowSize = new JLabel("Window Size");
		lblWindowSize.setBounds(206, 196, 109, 16);
		contentPane.add(lblWindowSize);
		
		FillWindowSize((String)cbProjectSel.getSelectedItem());
	}
		
	
	private void FillWindowSize(String projName){
		
		int numCommits = Database.NumCommits(projName);
		
		slStartCommit.setMinimum(1);
		slStartCommit.setMaximum(numCommits-1);
		slStartCommit.setValue(1);
		
		UpdateWindowSize(numCommits);

	}
	
	private void UpdateWindowSize(int maxSize){
		String winSizes[] = new String[maxSize];
		
		for (int i = 0; i < maxSize; i++)
			winSizes[i] = Integer.toString(i+1);
		
		cbWindowSize.setModel(new JComboBox(winSizes).getModel());
	}

	private void UpdateVisualization(){
		try {
			
			Matrix2D subMatrix = Database.ExtractCommitSubMatrix(currentGrain,
					(String)cbProjectSel.getSelectedItem(),
					slStartCommit.getValue()-1, 
					Integer.parseInt((String)cbWindowSize.getSelectedItem())
					).Transpose();
			
			
			System.out.println("Matrix: " + 
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
			
			
			
			
			Matrix2D taskDep = null;
			try {
				/*taskDep = Analyzer.ExtractTaskDepedency(
								confidenceMat.GetSlice(supportMat.getMatrixDescriptor().getDepthAt(
										Integer.parseInt((String)cbWindowSize.getSelectedItem())-1)), m);
				
				dep.setModel(new MatrixTable(taskDep));*/
				
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
									
			
			//confidence.setModel(new MatrixTable(
			//		confidenceMat.GetSlice(confidenceMat.getMatrixDescriptor().getDepthAt(
			//				Integer.parseInt((String)cbWindowSize.getSelectedItem())-1))));
			
		
		} catch (InvalidMatrix im){
			im.printStackTrace();
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}

	private Vector3f[] GetBorderClass(Matrix2D _mat) {
		
		String _classes[] = getClasses(_mat);
		List<Vector3f> minMaxPair = new ArrayList<Vector3f>();
		
		for (int i = 0; i < _classes.length; i++){
			
			int idxStart = getClassIdxStart(_classes[i], _mat);
			int idxEnd = getClassIdxEnd(_classes[i], _mat, idxStart);
			
			minMaxPair.add(new Vector3f(idxStart, 0, idxStart));
			minMaxPair.add(new Vector3f(idxEnd, 1, idxEnd));
		}
		
		return minMaxPair.toArray(new Vector3f[minMaxPair.size()]);
	}

	private int getClassIdxStart(String string, Matrix2D _mat) {
		
		int idxStart = 0;
		MatrixDescriptor desc = _mat.getMatrixDescriptor();
		for (int i = 0; i < desc.getNumRows(); i++){
			
			String className =  desc.getRowAt(i).substring(0, desc.getRowAt(i).lastIndexOf("."));
			if (className.equals(string)){
				idxStart = i;
				break;
			}
		}
		
		return idxStart;
	}

	private int getClassIdxEnd(String string, Matrix2D _mat, int idxStart) {
		int idxEnd = idxStart;
		
		MatrixDescriptor desc = _mat.getMatrixDescriptor();
		for (int i = idxStart+1; i < desc.getNumRows(); i++){
			
			String className = desc.getRowAt(i).substring(0, desc.getRowAt(i).lastIndexOf("."));
			if (!className.equals(string)){
				break;
			}
			
			idxEnd++;
		}
		
		return idxEnd;
	}

	private String[] getClasses(Matrix2D _mat) {
		List<String> _classes = new ArrayList<String>();
		
		String lastClass = "";
		MatrixDescriptor desc = _mat.getMatrixDescriptor();
		
		for (int i = 0; i < desc.getNumRows(); i++){
			String className = desc.getRowAt(i).substring(0, desc.getRowAt(i).lastIndexOf("."));
					
			if (!lastClass.equals(className)){
				lastClass = className;
				_classes.add(lastClass);
			}
		}
		 return _classes.toArray(new String[_classes.size()]);
	}
}
