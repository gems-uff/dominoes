package com.josericardojunior.gitdataminer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;

public class FileNode extends PatchInfo {

	List<ClassNode> classes = new ArrayList<ClassNode>();
	public String packageName = null;
	
	public List<ClassNode> getClasses(){
		return classes;
	}
	
	public FileNode(String _newName, String _oldName, ObjectId _newObjId, ObjectId _oldObjId,
			ChangeType _changeType) {
		super(_newName, _oldName, _newObjId, _oldObjId, _changeType);
	}
	
	public void Parse(Repository _repo){
		try {
			// Extract the package name of this file
			if (changeType == ChangeType.ADD || changeType == changeType.MODIFY)
				extractPackage(_repo);
			
			classes = ClassNode.Parse(this, _repo);
		} catch (MissingObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private void extractPackage(Repository _repo) throws MissingObjectException, IOException{
		ObjectLoader newFile = _repo.open(this.newObjId);
		String newData = readStream(newFile.openStream());

		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(newData.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Hashtable<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.DISABLED);
		parser.setCompilerOptions(options);
		
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		cu.accept(new ASTVisitor() {
			
			public boolean visit(PackageDeclaration _package){
				packageName = _package.getName().getFullyQualifiedName();
				return false;
			}
		});
	}
	
	public void Debug(){
		System.out.println("Name: " + newName);
		System.out.println("--Modification: " + changeType.toString());
		
		for (ClassNode cl : classes){
			cl.Debug();
		}
	}
	
	static String readStream(InputStream iStream) throws IOException {
		// build a Stream Reader, it can read char by char
		InputStreamReader iStreamReader = new InputStreamReader(iStream);
		// build a buffered Reader, so that i can read whole line at once
		BufferedReader bReader = new BufferedReader(iStreamReader);
		String line = null;
		StringBuilder builder = new StringBuilder();
		while ((line = bReader.readLine()) != null) { // Read till end
			builder.append(line + '\n');
		}
		bReader.close(); // close all opened stuff
		iStreamReader.close();
		iStream.close();
		return builder.toString();
	}
}
