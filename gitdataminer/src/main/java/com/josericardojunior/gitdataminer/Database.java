package com.josericardojunior.gitdataminer;

///TODO Verificar se existe algum problema para matriz de composicao que possuem o mesmo metodo em varias classes (o mesmo vale para todas as composicoes)
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.crypto.Data;

import org.eclipse.jdt.core.dom.ThrowStatement;

import com.josericardojunior.gitdataminer.Analyzer.Grain;
import com.josericardojunior.gitdataminer.Analyzer.InfoType;

public class Database {
	
	public static String database_file = "data/gitdataminer.sqlite";
	
	public enum CubeOptions {
		All,		
		Day,
		Week,
		Month,
		Year,
		Max_Commit_Month // each layer is composoded by the maximum number of commits in a month
	}
	
	private static Connection conn;
	public static String CUBE_DEPTH_PREFIX = "RANGE";		
	
	
	
	public static void Open(){
		
		File f = new File(database_file);
		boolean createStructure = false;
		
		if (!f.exists())
			createStructure = true;
			
		
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + database_file);
			
			if (createStructure)
				CreateGitMinerRepoStructure();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		

	}
	
	public static void Close(){
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static Map<String,Commit>  Unserialize(String _repository) throws SQLException{
		
		Map<String, Commit> resultSet = new HashMap<String, Commit>();
		
		String sql;
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
					
				
		// Get all commits
		sql = "SELECT TC.date, TC.hashcode, TU.name AS UserName, Tc.message, TFile.newName AS FileName, "
				+ "TClass.name AS ClassName, TFunction.name AS FuncName "
				+ "FROM TCommit TC "
				+ "INNER JOIN TRepository TR ON TC.RepoId = TR.id "
				+ "INNER JOIN TUser TU ON TC.userid = TU.id "
				+ "LEFT JOIN TFile ON TFile.commitid = TC.id "
				+ "LEFT JOIN TClass on TClass.fileid = TFile.id "
				+ "LEFT JOIN TFunction ON TFunction.classid = TClass.id "
				+ "WHERE TR.Name = '" + _repository + "' "
				+ "ORDER BY TC.date; ";
		
				

		rs = smt.executeQuery(sql);
		
		while (rs.next()){
								
			Commit currentCommit = resultSet.get(rs.getString("HashCode"));
			
			if (currentCommit == null){
				currentCommit = new Commit(rs.getString("HashCode"),
						rs.getString("UserName"));
				try {
					currentCommit.date = sdf.parse(rs.getString("date"));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				resultSet.put(currentCommit.hash, currentCommit);
			}
			
			String _fileName = rs.getString("FileName");
			if (_fileName != null){
				FileArch currentFile = currentCommit.files.get(_fileName);
			
				if (currentFile == null){
					currentFile =  new FileArch(_fileName);
				
					currentCommit.files.put(currentFile.filename, currentFile);
				
					String _className = rs.getString("ClassName");
					if (_className != null){
						
						FileClass currentClass = 
								currentFile.classes.get(_className);
				
						if (currentClass == null){
							currentClass = new FileClass(_className);
					
							currentFile.classes.put(currentClass.className, currentClass);
						}
						
						String _funcName = rs.getString("FuncName");
						if (_funcName != null){
							
							currentClass.functions.add(_funcName);
						}
					}
				}
			}
			
		}
						
		rs.close();
		smt.close();
		
		return resultSet;
		
	}
	
	public static Date AddRepository(RepositoryNode repo){
		
		Statement smt;
		String sql;
		Date lastCommitDate = null;

		try {
			smt = conn.createStatement();
			
			
			lastCommitDate = GetLastCommitDate(repo.getName());

			// First check if repo exists
			if (lastCommitDate == null){
				// Save repository info
				sql = "INSERT INTO TREPOSITORY(Name) VALUES ('" + 
						repo.getName() + "');";
				smt.executeUpdate(sql);
				smt.close();
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return lastCommitDate;	
	}
		
	public static void Serialize(RepositoryNode repo){
		
		Statement smt;
		String sql;
		try {
			smt = conn.createStatement();
						
			Date lastCommitDate = AddRepository(repo);
			
			// Insert users if do not exist
			InsertUsers();

			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			List<CommitNode> commits = repo.getCommits();

			for (CommitNode commit : commits) {

				if (lastCommitDate == null || 
						(lastCommitDate != null && commit.getDate().after(lastCommitDate))) {

					sql = "INSERT INTO TCOMMIT (RepoId, UserId, HashCode, Date, Message)"
							+ " VALUES ("
							+ "(SELECT id FROM TREPOSITORY TR WHERE TR.Name = '"
							+ repo.getName()
							+ "'),"
							+ "(SELECT id FROM TUSER TU WHERE TU.Name = '"
							+ commit.getUser().getName()
							+ "'),"
							+ "'"
							+ commit.getId()
							+ "',"
							+ "'"
							+ sdf.format(commit.getDate())
							+ "',"
							+ "'"
							+ commit.getLogMessage().replaceAll("'", "''")
							+ "');";

					for (FileNode fileNode : commit.getFiles()) {
						sql += "INSERT INTO TFILE (CommitId, NewName, OldName, NewObjId, ChangeType)"
								+ " VALUES ("
								+ "(SELECT id FROM TCOMMIT TC WHERE TC.HashCode = '"
								+ commit.getId()
								+ "'),"
								+ "'"
								+ fileNode.newName
								+ "',"
								+ "'"
								+ fileNode.oldName
								+ "',"
								+ "'"
								+ fileNode.newObjId
								+ "',"
								+ "'"
								+ fileNode.changeType + "');";

						for (ClassNode classNode : fileNode.getClasses()) {
							sql += "INSERT INTO TCLASS (FileId, Name, LineStart, LineEnd, ChangeType)"
									+ " VALUES ("
									+ "(SELECT id FROM TFILE TF WHERE TF.NewObjId = '"
									+ fileNode.newObjId
									+ "'),"
									+ "'"
									+ classNode.getName()
									+ "',"
									+ classNode.getLineStart()
									+ ","
									+ classNode.getLineEnd()
									+ ","
									+ "'"
									+ classNode.getChangeType() + "');";

							for (FunctionNode functionNode : classNode
									.getFunctions()) {
								sql += "INSERT INTO TFUNCTION (ClassId, Name, LineStart, LineEnd, ChangeType)"
										+ " VALUES ("
										+ "(SELECT TC.id FROM TCLASS TC, TFILE TF WHERE TC.Name = '"
										+ classNode.getName()
										+ "' AND "
										+ "TF.NewObjId = '"
										+ fileNode.newObjId
										+ "' AND TC.FileId = TF.id),"
										+ "'"
										+ functionNode.getName()
										+ "',"
										+ functionNode.getLineStart()
										+ ","
										+ functionNode.getLineEnd()
										+ ","
										+ "'"
										+ functionNode.getChangeType() + "');";
							}
						}
					}

					// Update the repository with the last commit
					sql += "UPDATE TREPOSITORY SET LastCommitId = "
							+ "(SELECT distinct(id) FROM TCOMMIT where date = (select max(date) from TCOMMIT))"
							+ "WHERE Name = '" + repo.getName() + "';";
					smt.executeUpdate(sql);
				}
			}
			smt.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Date GetLastCommitDate(String name) {
		
		Date result = null;
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		
		String sql = "SELECT TC.Date FROM TCOMMIT TC, TREPOSITORY TR "
				+ "WHERE TR.LastCommitId = TC.id AND " 
					+ "TR.Name = '" + name + "';";
		
		Statement stm;
		ResultSet rs;
		try {
			stm = conn.createStatement();
			rs = stm.executeQuery(sql);
			
			while (rs.next()){
				try {
					result = sdf.parse(rs.getString("date"));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	private static void InsertUsers() throws SQLException {
		
		Map<String, UserNode> users = UserNode.getUsers();
		Statement smt = conn.createStatement();
		
		for (UserNode user : users.values()){
			String sql = "SELECT * FROM TUSER WHERE name = '" + user.getName() + "';";
			
			ResultSet rs = smt.executeQuery(sql);
			
			if (!rs.next()){
				sql = "INSERT INTO TUSER (Name) VALUES ('" + user.getName() + "');";
				smt.executeUpdate(sql);
			}
			
			rs.close();
		}
		smt.close();
	}
	
	public static int getUserId(String name) {
		
		name = name.replaceAll("'", "''");
		
		try {	
			Statement smt = conn.createStatement();
			
			String sql = "SELECT * FROM TUSER WHERE name = '" + name + "';";			
			ResultSet rs = smt.executeQuery(sql);
			
			if (!rs.next()){
				sql = "INSERT INTO TUSER (Name) VALUES ('" + name + "');";
				smt.executeUpdate(sql);
			} else {
				return rs.getInt("id");
			}
			
			
			sql = "SELECT * FROM TUSER WHERE name = '" + name + "';";			
			rs = smt.executeQuery(sql);
			
			if (rs.next())			
				return rs.getInt("id");
		
			smt.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		// Error
		return -1;
	}
	

	private static void CreateGitMinerRepoStructure() {
		
		try {			
						Statement smt = conn.createStatement();

			String sql = 
					"CREATE TABLE TREPOSITORY(" +
							"id INTEGER PRIMARY KEY AUTOINCREMENT," +
							"Name STRING UNIQUE NOT NULL," + 
							"LastCommitId INTEGER REFERENCES TCOMMIT(id));" + 	
					
					"CREATE TABLE TUSER(" +
							"id INTEGER PRIMARY KEY AUTOINCREMENT," +
							"Name STRING NOT NULL);" + 
					
					"CREATE TABLE TCOMMIT(" +
							"id INTEGER PRIMARY KEY AUTOINCREMENT," +
							"RepoId INTEGER REFERENCES TREPOSITORY(id) NOT NULL," +
							"UserID INTEGER REFERENCES TUSER(id) NOT NULL," +
							"HashCode STRING NOT NULL," +
							"Date STRING NOT NULL," +
							"Message STRING NOT NULL);" +
					
					"CREATE TABLE TFILE(" +
							"id INTEGER PRIMARY KEY AUTOINCREMENT," +
							"CommitId INTEGER REFERENCES TCOMMIT(id),"+
							"NewName STRING NOT NULL," +
							"OldName STRING NOT NULL," +
							"NewObjId STRING NOT NULL," +
							"PackageName STRING NOT NULL," +
							"ChangeType STRING NOT NULL);" +
					
					"CREATE TABLE TCLASS(" +
							"id INTEGER PRIMARY KEY AUTOINCREMENT," +
							"FileId INTEGER REFERENCES TFILE(id),"+
							"Name STRING NOT NULL," +
							"LineStart INT NOT NULL," +
							"LineEnd INT NOT NULL," +
							"ChangeType STRING NOT NULL);" +
					
					"CREATE TABLE TFUNCTION(" +
							"id INTEGER PRIMARY KEY AUTOINCREMENT," +
							"ClassId INTEGER REFERENCES TCLASS(id),"+
							"Name STRING NOT NULL," +
							"LineStart INT NOT NULL," +
							"LineEnd INT NOT NULL," +
							"ChangeType STRING NOT NULL);" +
							
							
					"CREATE TABLE TBUG(" +
						"id STRING NOT NULL," +
						"CommitID STRING NOT NULL);";
			
			
			smt.executeUpdate(sql);
			
			
			smt.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * @param grain
	 * @param _repository
	 * @return A 2D Matrix containing all artifact modified on each commit. However, it join class with method for low grain. This is not
	 * thre right way to do it. Use the ____ instead and multiply with compositions matrix in order to navigate from coarse to fine grain
	 * @throws SQLException
	 */
	public static Matrix2D ExtractCommitArtifactMatrix(Analyzer.Grain grain, 
			String _repository, String filename) throws SQLException{
		
		String sql;
		MatrixDescriptor descriptor = new MatrixDescriptor(InfoType.COMMIT, InfoType.ARTIFACT);
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		// Get all commits
		sql = "SELECT TC.HashCode, TC.Date FROM TCOMMIT TC, TREPOSITORY TR, TFILE TF "
				+ "WHERE TC.RepoId = TR.id AND TR.Name = '" + _repository + "' ";
				
		if (filename != null)
			sql = sql.concat("AND TF.commitid = TC.id AND TF.newName = '" + filename + "' ");
		
		sql = sql.concat("ORDER BY TC.Date, TC.HashCode;");
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{
			descriptor.AddRowDesc(rs.getString("HashCode"));
		}
		
		// Get all elements
		switch (grain){
		case FILE:
			sql = "SELECT Distinct(TF.NewName) FROM TFILE TF, TCOMMIT TC, TREPOSITORY TR " +
					"WHERE TF.CommitId = TC.id AND TC.RepoId = TR.id AND TF.NewName NOT LIKE 'null' ";
					
			if (filename != null)
				sql = sql.concat("AND TF.newName = '" + filename + "' ");
			
			sql = sql.concat("AND TR.name = '" + _repository + "';");					
			
					
			rs = smt.executeQuery(sql);
			
			while (rs.next())
				descriptor.AddColDesc(rs.getString("NewName"));
					
			rs.close();
			break;
			
		case METHOD:
			sql = "SELECT DISTINCT TFL.NewName, TCL.name as ClassName, TF.name as FuncName " + 
						"FROM TFunction TF, TCLASS TCL, TFILE TFL, TCOMMIT TC, TREPOSITORY TR " +
					"WHERE TF.classid = TCL.id AND TCL.fileid = TFL.id AND TFL.CommitID = TC.id " + 
						"AND TFL.NewName NOT LIKE 'null' AND TC.RepoId = TR.id ";
					
			if (filename != null)
				sql = sql.concat("AND TFL.newName = '" + filename + "' ");
			
			sql = sql.concat("AND TR.name = '" + _repository + "' "); 
			sql = sql.concat("GROUP BY TFL.NewName, TCL.name, TF.name;");
			
			rs = smt.executeQuery(sql);
			
			while (rs.next())
				descriptor.AddColDesc(rs.getString("NewName") +
						"$" + rs.getString("ClassName") + 
						"." + rs.getString("FuncName"));
					
			rs.close();
			break;
		}
		
		// Build Matrix
		Matrix2D mat = new Matrix2D(descriptor);
		
		
		switch (grain){
		case FILE:
			sql = "SELECT TC.HashCode, TF.NewName FROM TFILE TF, TCOMMIT TC, TREPOSITORY TR " +
					"WHERE TF.CommitId = TC.id AND TC.RepoId = TR.id AND TF.NewName NOT LIKE 'null' " +
						"AND TR.name = '" + _repository + "' ";
					
			if (filename != null)
				sql = sql.concat("AND TF.newName = '" + filename + "' ");
			
			sql = sql.concat("ORDER BY TC.Date, TF.NewName;");
			
			rs = smt.executeQuery(sql);
			
			int count = 0;
			while (rs.next()){
				count++;
				mat.SetElement(rs.getString("HashCode"), 
						rs.getString("NewName") , 1);
			}
			System.out.println(count);
			
			rs.close();
			break;
			
		case METHOD:
			sql = "SELECT TC.hashcode, TFL.NewName, TCL.name as ClassName, TF.name as FuncName from TFunction TF, " +
						"TCLASS TCL, TFILE TFL, TCOMMIT TC, TREPOSITORY TR " +
					"WHERE TF.classid = TCL.id AND TCL.fileid = TFL.id AND TFL.CommitID = TC.id AND " +
						"TFL.NewName NOT LIKE 'null' AND TC.RepoId = TR.id "; 
					
			if (filename != null)
				sql = sql.concat("AND TFL.newName = '" + filename + "' ");
			
			sql = sql.concat("AND TR.name = '" + _repository + "' "); 
			sql = sql.concat("ORDER BY TC.Date, TFL.newname, TCL.name, TF.name;");
			
			
			rs = smt.executeQuery(sql);
			
			while (rs.next()){	
				mat.SetElement(rs.getString("HashCode"), 
						rs.getString("NewName") +
						"$" + rs.getString("ClassName") + 
						"." + rs.getString("FuncName"), 
						 1);
			}
			
			rs.close();
			break;
		}

		smt.close();
		
		return mat;
	}
	

	
	/***
	 * Returns the composition between class and methods
	 * @param _repository
	 * @return
	 * @throws SQLException
	 */
	public static Matrix2D ExtractFileClassMethodComposition(String _repository) throws SQLException{
		// Retrieve files x method composition
		
		String sql;
		MatrixDescriptor descriptor = new MatrixDescriptor(InfoType.ARTIFACT, InfoType.ARTIFACT);
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		sql = "SELECT DISTINCT TFL.NewName AS FileName " + 
				"FROM TCLASS TCL, TFILE TFL, TCOMMIT TC, TREPOSITORY TR " +
			"WHERE TCL.fileId = TFL.id AND TFL.CommitID = TC.id " + 
				"AND TFL.NewName NOT LIKE 'null' AND TC.RepoId = TR.id " +						
				"AND TR.name = '" + _repository + "' " + 
			"ORDER BY TC.Date, TFL.NewName, TCL.name;";
		
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{
			descriptor.AddRowDesc(rs.getString("FileName"));
		}
		
		sql = "SELECT DISTINCT TFL.NewName AS FileName, TCL.name as ClassName, TF.name as FuncName " + 
				"FROM TFunction TF, TCLASS TCL, TFILE TFL, TCOMMIT TC, TREPOSITORY TR " +
			"WHERE TF.classid = TCL.id AND TCL.fileid = TFL.id AND TFL.CommitID = TC.id " + 
				"AND TFL.NewName NOT LIKE 'null' AND TC.RepoId = TR.id " +						
				"AND TR.name = '" + _repository + "' " + 
			"ORDER BY TC.Date, TFL.NewName, TCL.name, TF.Name;";
		
		
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{			
			descriptor.AddColDesc(rs.getString("FileName") +
					"$" + rs.getString("ClassName") + 
					"." + rs.getString("FuncName"));
		}
		
		Matrix2D result = new Matrix2D(descriptor);
		
		
		sql = "SELECT DISTINCT TFL.NewName AS FileName, TCL.name as ClassName, TF.name as FuncName " + 
				"FROM TFunction TF, TCLASS TCL, TFILE TFL, TCOMMIT TC, TREPOSITORY TR " +
			"WHERE TF.classid = TCL.id AND TCL.fileid = TFL.id AND TFL.CommitID = TC.id " + 
				"AND TFL.NewName NOT LIKE 'null' AND TC.RepoId = TR.id " +						
				"AND TR.name = '" + _repository + "' " + 
			"ORDER BY TC.Date, TFL.NewName, TCL.Name, TF.Name;";
	
		rs = smt.executeQuery(sql);
		
		while (rs.next())
			result.SetElement(rs.getString("FileName"), 
					rs.getString("FileName") +
					"$" + rs.getString("ClassName") + 
					"." + rs.getString("FuncName"), 1); 
		
		return result;
		
	}
	
	
	
	/**
	 * @param _repository
	 * @return A 2D Matrix containing information about what developer performed a commit
	 * @throws SQLException
	 */
	public static Matrix2D ExtractDeveloperCommitMatrix(String _repository, String filename) 
			throws SQLException{
		
		String sql;
		MatrixDescriptor descriptor = new MatrixDescriptor(InfoType.USER, InfoType.COMMIT);
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		// Get all commits
		sql = "SELECT TC.HashCode, TC.Date FROM TCOMMIT TC, TREPOSITORY TR, TFILE TF "
				+ "WHERE TC.RepoId = TR.id AND TR.Name = '" + _repository + "' ";
		
		if (filename != null)
			sql = sql.concat("AND TF.commitid = TC.id AND TF.newname = '" + filename + "' ");
		
		sql = sql.concat("ORDER BY TC.Date, TC.HashCode;");
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{
			descriptor.AddColDesc(rs.getString("HashCode"));
		}
		
		// Get all users
		sql = "SELECT Distinct(TU.name) FROM TUser TU, TCOMMIT TC, TREPOSITORY TR " +
				"WHERE TC.userID = TU.id AND TC.RepoId = TR.id AND TR.name = '" + _repository + "';";
					
		rs = smt.executeQuery(sql);
		while (rs.next())
		{
			descriptor.AddRowDesc(rs.getString("name"));
		}
		
		// Build Matrix
		Matrix2D mat = new Matrix2D(descriptor);
		
		// Get all commits
		sql = "SELECT TU.name, TC.HashCode FROM TUser TU, TCOMMIT TC, TREPOSITORY TR, TFILE TF " +
				"WHERE TC.userID = TU.id AND TC.RepoId = TR.id AND TR.name = '" + _repository + "' ";
		
		
		if (filename != null)
			sql = sql.concat("AND TF.commitid = TC.id AND TF.newname = '" + filename + "' ");
		
		sql = sql.concat("ORDER BY TC.Date, TC.HashCode;");

		rs = smt.executeQuery(sql);
		
		while (rs.next()){	
			mat.SetElement(rs.getString("name"),
					rs.getString("HashCode") , 1);
		}
		
		rs.close();
		smt.close();
		
		return mat;
	}
		
	public static Matrix3D ExtractDevArtifactSupportCube(Analyzer.Grain grain, 
			String _repository, Date startDate, CubeOptions cubeOptions, int minimum, int multiplicator) throws SQLException, ParseException {
		
		String sql;
		MatrixDescriptor descriptor = new MatrixDescriptor(InfoType.USER, 
				InfoType.ARTIFACT);
		Statement smt = conn.createStatement();
		ResultSet rs;
		Matrix3D cube = new Matrix3D();
		
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		
		// Get all users
		sql = "SELECT DISTINCT(TU.name) FROM TCOMMIT TC, TREPOSITORY TR, TUSER TU ";
		sql = sql.concat("WHERE TC.userid = TU.id AND TC.RepoId = TR.id AND TR.Name = '" + _repository + "' ");
		if (startDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(startDate) + "'");
		sql = sql.concat("ORDER BY TC.Date;");
		rs = smt.executeQuery(sql);
		
		while (rs.next())
			descriptor.AddRowDesc(rs.getString("name"));
				

		
		// Get all elements
		switch (grain){
		case FILE:		
			sql = "SELECT Distinct(TF.NewName) FROM TFILE TF, TCOMMIT TC, TREPOSITORY TR ";
			sql = sql.concat("WHERE TF.newname != 'null' AND TF.CommitId = TC.id AND ");
			sql = sql.concat("TC.RepoId = TR.id AND TR.name = '" + _repository + "' ");
			if (startDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(startDate) + "'");
			sql = sql.concat("AND TF.Newname like 'java/engine/org/apache/derby/impl/jdbc/EmbedConnection.java' ");
			sql = sql.concat(";");
			
			rs = smt.executeQuery(sql);
			
			while (rs.next())
				descriptor.AddColDesc(rs.getString("NewName"));
	
			rs.close();
			break;
			
		case METHOD:
			sql = "SELECT DISTINCT TCL.name as ClassName, TF.name as FuncName, TFL.newName as FileName ";
			sql = sql.concat("FROM TFunction TF, TCLASS TCL, TFILE TFL, TCOMMIT TC, TREPOSITORY TR ");
			sql = sql.concat("WHERE TF.classid = TCL.id AND TCL.fileid = TFL.id AND TFL.CommitID = TC.id AND FileName != 'null' "); 			
			sql = sql.concat("AND TC.RepoId = TR.id  AND TR.name = '" + _repository + "' ");
			if (startDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(startDate) + "' ");
			sql = sql.concat("AND FileName like 'java/engine/org/apache/derby/impl/jdbc/EmbedConnection.java' ");
			sql = sql.concat("GROUP BY TCL.name, TF.name;");
			
			rs = smt.executeQuery(sql);
			
			while (rs.next())
				descriptor.AddColDesc(rs.getString("FileName") +
						"$" + rs.getString("ClassName") + 
						"." + rs.getString("FuncName"));
					
			rs.close();
			break;
		}
		
				
		switch (grain){
		case FILE:	{
			
			
			// Process layers
			int count = 0;
			
			if (cubeOptions == CubeOptions.Max_Commit_Month){
			
				sql = "SELECT TU.name, TC.date, TC.hashcode, TF.newname FROM TFILE TF, ";
				sql = sql.concat("TUSER TU, TCOMMIT TC, TREPOSITORY TR WHERE TF.newname != 'null' AND ");
				sql = sql.concat("TC.userid = TU.id AND TF.CommitId = TC.id AND "); 
				sql = sql.concat("TC.repoid = tr.id and TR.name = '" + _repository + "' ");
				if (startDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(startDate) + "' "); 
				sql = sql.concat("AND TF.newname like 'java/engine/org/apache/derby/impl/jdbc/EmbedConnection.java' ");

				sql = sql.concat("ORDER BY TC.date desc, TC.hashcode;");
				
				rs = smt.executeQuery(sql);
			
				
				Map<String, Integer> max_per_month = Commits_Month(_repository);
				
				int num_commits_per_layer = minimum;
				
				for (Map.Entry<String, Integer> entry : max_per_month.entrySet()){
					if (entry.getValue() > minimum){
						num_commits_per_layer = entry.getValue();
						
						System.out.println("Month: " + entry.getKey() + " Value: " + entry.getValue());
					}
					else {
						System.out.println("Using minimum: " + num_commits_per_layer);
					}
					
					break;
				}
				
				// Create a commit group
				List<CommitGroup> commitGroups = new ArrayList<CommitGroup>();
				int currentMonth = -1, currentYear = -1;
				String currentCommitId = null;
				
				while (rs.next()){
					
					// Get the commit id
					String commitId = rs.getString("hashcode");
				
					if (currentCommitId == null || currentCommitId.compareTo(commitId) != 0){
						
						Calendar commitDate = Calendar.getInstance();
						commitDate.setTime(sdf.parse(rs.getString("date")));
						int month = commitDate.get(Calendar.MONTH);
						int year = commitDate.get(Calendar.YEAR);
						
						if (currentMonth != month || currentYear != year){
							currentMonth = month;
							currentYear = year;
							
							CommitGroup _commitGroup = new CommitGroup();
							_commitGroup.matrix = new Matrix2D(descriptor);
							_commitGroup.year = year;
							_commitGroup.month = month;
							commitGroups.add(_commitGroup);
						}
						
						for (CommitGroup _c : commitGroups)
							if (_c.count <= num_commits_per_layer)
								_c.count++;
						
						currentCommitId = commitId;						
					}
					
					for (CommitGroup _c : commitGroups){
						
						if (_c.count <= num_commits_per_layer)
							_c.matrix.AddToElement(rs.getString("name"), 
									rs.getString("NewName"), 1);
					}
				}
				
				Collections.reverse(commitGroups);
				CommitGroup _k = commitGroups.get(commitGroups.size()-1);
				_k.matrix.ExportCSV("Teste.txt");
				
				int _count = 0;
				for (CommitGroup _c : commitGroups){
					cube.AddLayer(CUBE_DEPTH_PREFIX+_count, _c.matrix);
					_count++;
				}
				
			} else if (cubeOptions == CubeOptions.Month){
				sql = "SELECT TU.name, TC.date, TC.hashcode, TF.newname FROM TFILE TF, ";
				sql = sql.concat("TUSER TU, TCOMMIT TC WHERE TF.newname != 'null' AND ");
				sql = sql.concat("TC.userid = TU.id AND TF.CommitId = TC.id AND ");
				sql = sql.concat("TC.id in ( ");
				sql = sql.concat("SELECT TC.id FROM tcommit TC, trepository TR "); 
				sql = sql.concat("WHERE TC.repoid = tr.id and TR.name = '" + _repository + "' ");
				if (startDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(startDate) + "' "); 
				sql = sql.concat("ORDER BY TC.date, TC.hashcode);");
				
				rs = smt.executeQuery(sql);
				
				Calendar refDateC = null;
				Matrix2D layer = null;
				int numRegs = 0;
				
				while (rs.next()){
					if (refDateC == null){
						refDateC = Calendar.getInstance();
						refDateC.setTime(sdf.parse(rs.getString("date")));
						layer = new Matrix2D(descriptor);
						layer.AddToElement(rs.getString("name"), 
								rs.getString("NewName"), 1);
					} else {
						Calendar currentDateC = Calendar.getInstance();
						Date _dt = sdf.parse(rs.getString("date")); 
						currentDateC.setTime(_dt);

						if (refDateC.get(Calendar.MONTH) - currentDateC.get(Calendar.MONTH) != 0 ||
							refDateC.get(Calendar.YEAR) - currentDateC.get(Calendar.YEAR) != 0){
							
							System.out.println("M" + (refDateC.get(Calendar.MONTH)+1) + 
									"-year" + Integer.toString(refDateC.get(Calendar.YEAR)).substring(2) + ": " + numRegs);
							numRegs = 0;
							
							refDateC = currentDateC;
							cube.AddLayer(CUBE_DEPTH_PREFIX + count, layer);
							
							layer = new Matrix2D(descriptor);
							count++;
						}
						
						layer.AddToElement(rs.getString("name"), 
								rs.getString("NewName"), 1);
						numRegs++;
						
						

					}
					
				}
				
			} else if (cubeOptions == CubeOptions.Week){
				Calendar refDateStartC = null;
				Calendar refDateEndC = null;
				Matrix2D layer = null;
				int numRegs = 0;
				
				SimpleDateFormat sdf2 = new SimpleDateFormat(
						"yyyy-MM-dd");
				
				while (rs.next()){
					if (refDateStartC == null){
						refDateStartC = Calendar.getInstance();
						refDateStartC.setTime(sdf.parse(rs.getString("date")));
						
						refDateEndC = Calendar.getInstance();
						refDateEndC.setTime(sdf.parse(rs.getString("date")));
						refDateEndC.add(Calendar.DAY_OF_WEEK, 7);
						
						layer = new Matrix2D(descriptor);
						layer.AddToElement(rs.getString("name"), 
								rs.getString("NewName"), 1);
					} else {
						Calendar currentDateC = Calendar.getInstance();
						Date _dt = sdf.parse(rs.getString("date")); 
						currentDateC.setTime(_dt);

						if (currentDateC.compareTo(refDateEndC) > 0){							
							//System.out.println("Week from " + sdf2.format(refDateStartC.getTime()) + 
								//	" to " + sdf2.format(refDateEndC.getTime()) + " Total: " + numRegs);
							System.out.println("M" + (refDateStartC.get(Calendar.MONTH)+1) + 
									"-year" + Integer.toString(refDateStartC.get(Calendar.YEAR)).substring(2) + ": " + numRegs);
							numRegs = 0;
							
							refDateStartC.setTime(_dt);
							refDateEndC.setTime(_dt);;
							refDateEndC.add(Calendar.DAY_OF_WEEK, 7);
							cube.AddLayer(CUBE_DEPTH_PREFIX + count, layer);
							
							layer = new Matrix2D(descriptor);
							count++;		
						}
						
						layer.AddToElement(rs.getString("name"), 
								rs.getString("NewName"), 1);
						numRegs++;
					}
				}
			}
						
			rs.close();
		}
			break;
			
		case METHOD: {
								
			// Process Window
			int count = 0;
	
				
			if (cubeOptions == CubeOptions.Max_Commit_Month){
				
				sql = "SELECT TC.id, TC.date, TC.hashcode, TU.name as username, TCL.name as ClassName, "; 
				sql = sql.concat("TF.name as FuncName, TFL.newname FROM TFunction TF, TCLASS TCL, "); 
				sql = sql.concat("TFILE TFL, TCOMMIT TC, TUser TU, trepository TR WHERE TF.classid = TCL.id "); 
				sql = sql.concat("AND TCL.fileid = TFL.id AND TFL.CommitID = TC.id AND TFL.newname != 'null' "); 
				if (startDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(startDate) + "' "); 
				sql = sql.concat("AND TC.userid = TU.id AND tc.repoid =  tr.id and TR.name = '" + _repository + "'" );
				sql = sql.concat("AND TFL.newname like 'java/engine/org/apache/derby/impl/jdbc/EmbedConnection.java' ");
				sql = sql.concat("ORDER BY tc.date desc, tc.hashcode;");
				
				rs = smt.executeQuery(sql);
				
				Map<String, Integer> max_per_month = Commits_Month(_repository);
				
				int num_commits_per_layer = minimum;
				
				for (Map.Entry<String, Integer> entry : max_per_month.entrySet()){
					if (entry.getValue() > minimum){
						num_commits_per_layer = entry.getValue() * multiplicator;
						
						System.out.println("Month: " + entry.getKey() + " Value: " + entry.getValue() + 
								" Multiplicator: " + multiplicator + "Using: " + num_commits_per_layer);
					}
					else {
						
					}
					System.out.println("Month: " + entry.getKey() + " Value: " + entry.getValue() + 
							" Multiplicator: " + multiplicator + "Using: " + num_commits_per_layer);
					
				}
				
				
				// Create a commit group
				List<CommitGroup> commitGroups = new ArrayList<CommitGroup>();
				int currentMonth = -1, currentYear = -1;
				String currentCommitId = null;
				
				while (rs.next()){
					
					// Get the commit id
					String commitId = rs.getString("hashcode");
				
					if (currentCommitId == null || currentCommitId.compareTo(commitId) != 0){
						
						Calendar commitDate = Calendar.getInstance();
						commitDate.setTime(sdf.parse(rs.getString("date")));
						int month = commitDate.get(Calendar.MONTH);
						int year = commitDate.get(Calendar.YEAR);
						
						if (currentMonth != month || currentYear != year){
							currentMonth = month;
							currentYear = year;
							
							CommitGroup _commitGroup = new CommitGroup();
							_commitGroup.matrix = new Matrix2D(descriptor);
							_commitGroup.year = year;
							_commitGroup.month = month;
							commitGroups.add(_commitGroup);
						}
						
						for (CommitGroup _c : commitGroups)
							if (_c.count <= num_commits_per_layer)
								_c.count++;
						
						currentCommitId = commitId;						
					}
					
					for (CommitGroup _c : commitGroups){
						
						if (_c.count <= num_commits_per_layer)
							_c.matrix.AddToElement(rs.getString("username"),
									rs.getString("newname") + "$" + 
									rs.getString("ClassName") + 
									"." + rs.getString("FuncName"), 1);
					}
				}
				
				Collections.reverse(commitGroups);
				
				int _count = 0;
				for (CommitGroup _c : commitGroups){
					cube.AddLayer(CUBE_DEPTH_PREFIX+_count, _c.matrix);
					_count++;
				}																	
				
			} else if (cubeOptions == CubeOptions.Month){
				
				sql = "SELECT TC.id, TC.date, TC.hashcode, TU.name as username, TCL.name as ClassName, "; 
				sql = sql.concat("TF.name as FuncName, TFL.newname FROM TFunction TF, TCLASS TCL, "); 
				sql = sql.concat("TFILE TFL, TCOMMIT TC, TUser TU WHERE TF.classid = TCL.id "); 
				sql = sql.concat("AND TCL.fileid = TFL.id AND TFL.CommitID = TC.id AND TFL.newname != 'null' "); 
				if (startDate != null) sql = sql.concat("AND TC.date >= '" + sdf.format(startDate) + "' "); 
				sql = sql.concat("AND TC.userid = TU.id AND TC.id in ( ");
				sql = sql.concat("SELECT TC.id FROM tcommit TC, trepository TR ");
				sql = sql.concat("WHERE tc.repoid =  tr.id and TR.name = '" + _repository + "'" );
				sql = sql.concat("ORDER BY tc.date, tc.hashcode);");
				
				rs = smt.executeQuery(sql);
					
				Calendar refDateC = null;
				Matrix2D layer = null;
				int numRegs = 0;
					
				while (rs.next()){
					if (refDateC == null){
						refDateC = Calendar.getInstance();
						refDateC.setTime(sdf.parse(rs.getString("date")));
						layer = new Matrix2D(descriptor);
						layer.AddToElement(rs.getString("username"),								
								rs.getString("newname") + "$" + 
								rs.getString("ClassName") + 
								"." + rs.getString("FuncName"), 1);
					} else {
						Calendar currentDateC = Calendar.getInstance();
						Date _dt = sdf.parse(rs.getString("date")); 
						currentDateC.setTime(_dt);

						if (refDateC.get(Calendar.MONTH) - currentDateC.get(Calendar.MONTH) != 0 ||
							refDateC.get(Calendar.YEAR) - currentDateC.get(Calendar.YEAR) != 0){
								
							System.out.println("M" + (refDateC.get(Calendar.MONTH)+1) + 
									"-year" + Integer.toString(refDateC.get(Calendar.YEAR)).substring(2) + ": " + numRegs);
							numRegs = 0;
								
							refDateC = currentDateC;
							cube.AddLayer(CUBE_DEPTH_PREFIX + count, layer);
								
							layer = new Matrix2D(descriptor);
							count++;
						}
							
						layer.AddToElement(rs.getString("username"),								
								rs.getString("newname") + "$" + 
								rs.getString("ClassName") + 
								"." + rs.getString("FuncName"), 1);
							numRegs++;																									
					}					
				}												
			}			
			rs.close();
		}
			break;	
		}
		
		smt.close();
		
		return cube;
	}

	public static String[] AvailableProjects() throws SQLException {
		
		List<String> repos = new ArrayList<String>();
		
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		// Get all commits
		String sql = "SELECT Name FROM TREPOSITORY;";
		rs = smt.executeQuery(sql);
		
		while (rs.next())
		{
			repos.add(rs.getString("name"));
		}
		
		rs.close();
		smt.close();
		
		return (String []) repos.toArray(new String[0]);
	}
	


	public static int NumCommits(String projName)  {
		
		int total = 0;
		
		try {
			Statement smt = conn.createStatement();
			ResultSet rs;
			
			// Get all commits
			String sql = "SELECT TCommit.HashCode FROM TCOMMIT, TREPOSITORY " +
					"WHERE TCOMMIT.RepoId = TREPOSITORY.id AND " +
					"TREPOSITORY.Name = '" + projName + "';";
			
			rs = smt.executeQuery(sql);
			
			while (rs.next())
			{
				total++;
			}
			
			rs.close();
			smt.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return total;
	}
	
	public static Map<String,Integer> Commits_Month(String projName)  {
					
		Map<String,Integer> res = new LinkedHashMap<String, Integer>();
		
		try {
			Statement smt = conn.createStatement();
			ResultSet rs;
			
						
			String sql = "SELECT strftime('%m-%Y', TCommit.Date) as 'month-year', "
					+ "COUNT(TCommit.Date) AS TotalMonth FROM TCOMMIT, TREPOSITORY, TFILE " +
					"WHERE TCOMMIT.RepoId = TREPOSITORY.id AND " +
					"TFILE.commitId = TCOMMIT.id AND TFile.newname like 'java/engine/org/apache/derby/impl/jdbc/EmbedConnection.java' AND " +
					"TREPOSITORY.Name = '" + projName + "' " +
					"GROUP BY strftime('%m-%Y', Date) ORDER BY TotalMonth desc;";
			
			rs = smt.executeQuery(sql);
			
			while (rs.next())
			{
				res.put(rs.getString("month-year"), rs.getInt("TotalMonth"));				
			}
			
			rs.close();
			smt.close();
		} catch (Exception e){
			e.printStackTrace();
		}
				
		return res;
	}
	
	public static void UpdateRepoToLastCommit(RepositoryNode repoNode){
		Statement smt;
		String sql;
		
		try {
			smt = conn.createStatement();
			
			// Update the repository with the last commit
			sql = "UPDATE TREPOSITORY SET LastCommitId = "
					+ "(SELECT distinct(id) FROM TCOMMIT where date = (select max(date) from TCOMMIT))"
					+ "WHERE Name = '" + repoNode.getName() + "';";
			smt.executeUpdate(sql);
			smt.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public static void AddCommit(CommitNode commit, RepositoryNode repoNode) {
		
		Statement smt;
		String sql;
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		
		try {
			smt = conn.createStatement();

			sql = "INSERT INTO TCOMMIT (RepoId, UserId, HashCode, Date, Message)"
					+ " VALUES ("
					+ "(SELECT id FROM TREPOSITORY TR WHERE TR.Name = '"
					+ repoNode.getName()
					+ "')," 
					+ Integer.toString(getUserId(commit.getUser().getName())) + ","
					+ "'"
					+ commit.getId()
					+ "',"
					+ "'"
					+ sdf.format(commit.getDate())
					+ "',"
					+ "'"
					+ commit.getLogMessage().replaceAll("'", "''")
					+ "');";

			for (FileNode fileNode : commit.getFiles()) {
				sql += "INSERT INTO TFILE (CommitId, NewName, OldName, NewObjId, PackageName, ChangeType)"
						+ " VALUES ("
						+ "(SELECT id FROM TCOMMIT TC WHERE TC.HashCode = '"
						+ commit.getId()
						+ "'),"
						+ "'"
						+ fileNode.newName
						+ "',"
						+ "'"
						+ fileNode.oldName
						+ "',"
						+ "'"
						+ fileNode.newObjId
						+ "',"
						+ "'"
						+ fileNode.packageName
						+ "',"
						+ "'"
						+ fileNode.changeType + "');";

				for (ClassNode classNode : fileNode.getClasses()) {
					sql += "INSERT INTO TCLASS (FileId, Name, LineStart, LineEnd, ChangeType)"
							+ " VALUES ("
							+ "(SELECT id FROM TFILE TF WHERE TF.NewObjId = '"
							+ fileNode.newObjId
							+ "'),"
							+ "'"
							+ classNode.getName()
							+ "',"
							+ classNode.getLineStart()
							+ ","
							+ classNode.getLineEnd()
							+ ","
							+ "'"
							+ classNode.getChangeType() + "');";

					for (FunctionNode functionNode : classNode
							.getFunctions()) {
						sql += "INSERT INTO TFUNCTION (ClassId, Name, LineStart, LineEnd, ChangeType)"
								+ " VALUES ("
								+ "(SELECT TC.id FROM TCLASS TC, TFILE TF WHERE TC.Name = '"
								+ classNode.getName()
								+ "' AND "
								+ "TF.NewObjId = '"
								+ fileNode.newObjId
								+ "' AND TC.FileId = TF.id),"
								+ "'"
								+ functionNode.getName()
								+ "',"
								+ functionNode.getLineStart()
								+ ","
								+ functionNode.getLineEnd()
								+ ","
								+ "'"
								+ functionNode.getChangeType() + "');";
					}
				}
			}
			smt.executeUpdate(sql);
		
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void MineBugs(String bugMatch, RepositoryNode repository) throws SQLException {
		
		String sql;
		MatrixDescriptor descriptor = new MatrixDescriptor(InfoType.USER, InfoType.COMMIT);
		Statement smt = conn.createStatement();
		ResultSet rs;
		
		// Get all commits
		sql = "SELECT TC.HashCode, TC.Message FROM TCommit TC, TRepository TR "
				+ "WHERE TC.RepoId = TR.id AND TR.Name = '" + repository.getName() + "' "
				+ "ORDER BY TC.Date;";
		rs = smt.executeQuery(sql);
		
		
		List<Entry<String, String>> bugCommit = new ArrayList<Map.Entry<String,String>>(); 
		
		while (rs.next())
		{
			String hashCode = rs.getString("HashCode");
			String message = rs.getString("Message");
			
			String bugId = extractBugId(message, bugMatch);
			
			if (bugId != null){
				Map.Entry<String,String> pair = new AbstractMap.SimpleEntry(bugId, hashCode);
				
				bugCommit.add(pair);
			}
		}
		
		
		// Add found bugs
		for (Entry<String, String> record : bugCommit){
			sql = "INSERT INTO TBUG (id, commitId)"
					+ " VALUES ('" + record.getKey() + "', '" + record.getValue() + "');";

			smt.executeUpdate(sql);
		} 
		
		rs.close();
		smt.close();
	}
	
	private static String extractBugId(String text, String match){
		
		String idBug = "derby-";
		String res = idBug;
		
		int start = text.toLowerCase().indexOf(idBug);
		
		for (int i = start + idBug.length(); i < text.length(); i++){
			if (!Character.isDigit(text.charAt(i))){
				break;
			}
			res += text.charAt(i);
		}
		
		if (idBug.equals(res))
			return null;
		
		return res;
	}

	public static Matrix2D ExtractCommitSubMatrix(Grain grain,
			String _repository, int offset, int size) {
		
		try {
			
		
			String sql;
			MatrixDescriptor descriptor = new MatrixDescriptor(InfoType.ARTIFACT, InfoType.COMMIT);
			Statement smt = conn.createStatement();
			ResultSet rs;
			
			// Get all commits
			sql = "SELECT TC.HashCode, TC.Date FROM TCOMMIT TC, TREPOSITORY TR "
					+ "WHERE TC.RepoId = TR.id AND TR.Name = '" + _repository + "' "
					+ "ORDER BY TC.Date ASC " 
					+ "LIMIT " + size 
					+ " OFFSET " + offset + ";";
			rs = smt.executeQuery(sql);
			
			while (rs.next())
			{
				descriptor.AddColDesc(rs.getString("HashCode"));
			}
			
			// Get all elements
			switch (grain){
			case FILE:
				
				sql = "SELECT Distinct(TF.NewName) FROM TFILE TF "
						+ "WHERE TF.CommitId IN " 
							+ "(SELECT TC.id FROM TCOMMIT TC, TREPOSITORY TR "
								+ "WHERE TC.RepoId = TR.id AND TR.name = '" + _repository + "'"
								+ "ORDER BY TC.date ASC LIMIT " + size + " OFFSET " + offset + ")";
							
				rs = smt.executeQuery(sql);
				
				while (rs.next())
					descriptor.AddRowDesc(rs.getString("NewName"));
						
				rs.close();
				break;
				
			case METHOD:
				sql = "SELECT DISTINCT TCL.name as ClassName, TF.name as FuncName " + 
							"FROM TFunction TF, TCLASS TCL, TFILE TFL, TCOMMIT TC, TREPOSITORY TR " +
						"WHERE TF.classid = TCL.id AND TCL.fileid = TFL.id AND TFL.CommitID = TC.id " + 
							"AND TC.RepoId = TR.id  AND TR.name = '" + _repository + "' " + 
						"GROUP BY TCL.name, TF.name;";
				
				rs = smt.executeQuery(sql);
				
				while (rs.next())
					descriptor.AddRowDesc(rs.getString("ClassName") + 
							"." + rs.getString("FuncName"));
						
				rs.close();
				break;
			}
			
			// Build Matrix
			Matrix2D mat = new Matrix2D(descriptor);
			
			
			switch (grain){
			case FILE:
				sql = "SELECT TC.HashCode, TF.NewName FROM TFILE TF, TCOMMIT TC "
						+ "WHERE TF.CommitId = TC.id AND TC.id IN " 
							+ "(SELECT TC.id FROM TCOMMIT TC, TREPOSITORY TR "
								+ "WHERE TC.RepoId = TR.id AND TR.name = '" + _repository + "'"
								+ "ORDER BY TC.date ASC LIMIT " + size + " OFFSET " + offset + ")";
				
				rs = smt.executeQuery(sql);
				
				while (rs.next()){	
					mat.SetElement(rs.getString("NewName"), 
							rs.getString("HashCode") , 1);
				}
				
				rs.close();
				break;
				
			case METHOD:
				sql = "SELECT TC.hashcode, TCL.name as ClassName, TF.name as FuncName from TFunction TF, " +
							"TCLASS TCL, TFILE TFL, TCOMMIT TC, TREPOSITORY TR " +
						"WHERE TF.classid = TCL.id AND TCL.fileid = TFL.id AND TFL.CommitID = TC.id AND " +
							"TC.RepoId = TR.id  AND TR.name = '" + _repository + "' " +
						"ORDER BY TC.Date, TFL.newname, TCL.name, TF.name;";
				
				
				rs = smt.executeQuery(sql);
				
				while (rs.next()){	
					mat.SetElement(rs.getString("ClassName") + 
							"." + rs.getString("FuncName"), 
							rs.getString("HashCode") , 1);
				}
				
				rs.close();
				break;
			}
	
			smt.close();
			
			return mat;
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
}