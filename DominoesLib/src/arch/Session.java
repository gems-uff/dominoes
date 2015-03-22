package arch;

import java.util.ArrayList;
import java.util.List;

public class Session {

	private static boolean sessionStarted = false;
	private static List<Matrix2D> matrices;
	
	public static boolean isSessionStarted(){
		return sessionStarted;
	}
	
	public static void startSession(){
		sessionStarted = true;
		matrices = new ArrayList<Matrix2D>();
	}
	
	public static void closeSection(){
		for (int i = 0; i < matrices.size(); i++)
			matrices.get(i).finalize();
		
		
	}
	
	public static void register2DMatrix(Matrix2D mat){
		matrices.add(mat);
	}
	
}
