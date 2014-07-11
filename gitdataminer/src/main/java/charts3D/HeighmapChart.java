package charts3D;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import com.josericardojunior.gitdataminer.Matrix2D;
import com.josericardojunior.gitdataminer.MatrixDescriptor;

public class HeighmapChart extends Base3DChart {

	private int gridWidth;
	private int gridLenght;
	private Vector3f vertex[];
	private Vector3f vertexNormals[];
	
	public HeighmapChart(String _name) {
		super(_name);
	}

	@Override
	public void Render() {
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
	    DisplayTerrain();
	    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
	    GL11.glLineWidth(3);
	    GL11.glDisable(GL11.GL_LIGHTING);
	    GL11.glTranslatef(0, 0.01f, 0);
	    DisplayTerrain();
	}
	
	void DisplayTerrain(){
		GL11.glPushMatrix();
		GL11.glColor3f(1, 1, 1);
		GL11.glBegin(GL11.GL_QUADS);
		
		for (int i = 0; i < gridWidth - 1; i++){
						
			for (int j = 0; j < gridLenght-1; j++ ){
				Vector3f v1 = vertex[i * gridWidth + j];
				Vector3f v2 = vertex[i * gridWidth + j + 1];
				Vector3f v3 = vertex[(i + 1) * gridWidth + j + 1];
				Vector3f v4 = vertex[(i + 1) * gridWidth + j];
				Vector3f n1 = vertexNormals[i * gridWidth + j];
				Vector3f n2 = vertexNormals[i * gridWidth + j + 1];
				Vector3f n3 = vertexNormals[(i + 1) * gridWidth + j + 1];
				Vector3f n4 = vertexNormals[(i + 1) * gridWidth + j];
				
				GL11.glColor3f(v1.y+0.2f, 0, 0);
				GL11.glNormal3f(n1.x, n1.y, n1.z);
				GL11.glVertex3f(v1.x, v1.y, v1.z);
				
				GL11.glNormal3f(n2.x, n2.y, n2.z);
				GL11.glVertex3f(v2.x, v2.y, v2.z);
				
				GL11.glNormal3f(n3.x, n3.y, n3.z);
				GL11.glVertex3f(v3.x, v3.y, v3.z);
				
				GL11.glNormal3f(n4.x, n4.y, n4.z);
				GL11.glVertex3f(v4.x, v4.y, v4.z);
				
			}
		}
		
		GL11.glEnd();
		GL11.glPopMatrix();
	}
	
	public void setData(Matrix2D matrix){
				
		MatrixDescriptor matDesc = matrix.getMatrixDescriptor();
		
		gridWidth = matDesc.getNumRows();
		gridLenght = matDesc.getNumCols();
		
		float normalizeWidth = 5.0f / gridWidth;
		float normalizeLenght = 5.0f / gridLenght;
		
		vertex = new Vector3f[gridWidth * gridLenght];
		
		for (int i = 0; i < matDesc.getNumRows(); i++){
			for (int j = 0; j < matDesc.getNumCols(); j++){
				
				vertex[i * gridWidth + j] = new Vector3f((float) i * normalizeWidth,
						matrix.GetElement(i, j), (float)j * normalizeLenght);
			}
		}
		
		ComputeVertexNormals();
	}
	
	private void ComputeVertexNormals() {
		
		Vector3f facesNormal[][] = new Vector3f[gridWidth-1][gridLenght-1];
		
		// Face normals
		for (int i = 0; i < gridWidth - 1; i++){
			for (int j = 0; j < gridLenght-1; j++){
				
				Vector3f v1 = vertex[i * gridWidth + j];
				Vector3f v2 = vertex[i * gridWidth + j + 1];
				Vector3f v3 = vertex[(i+1) * gridWidth + j];
				
				Vector3f v1_v2 = Vector3f.sub(v2, v1, null);
				Vector3f v1_v3 = Vector3f.sub(v3, v1, null);
				Vector3f cross = Vector3f.cross(v1_v2, v1_v3, null);
				cross.normalise();
				
				facesNormal[i][j] = cross;
			}
		}
		
		// Vector Normals
		vertexNormals = new Vector3f[vertex.length];
		
		
		
		for (int i = 0; i < gridWidth; i++){
			for (int j = 0; j < gridLenght; j++){
				
				Vector3f v1 = facesNormal[Math.min(i, gridWidth-2)][Math.max(j-1, 0)];
				Vector3f v2 = facesNormal[Math.max(i-1,0)][Math.max(j-1, 0)];
				Vector3f v3 = facesNormal[Math.max(i-1,0)][Math.min(j,gridLenght-2)];
				Vector3f v4 = facesNormal[Math.min(i, gridWidth-2)][Math.min(j,gridLenght-2)];
				
				Vector3f sum = new Vector3f();
				sum.x = (v1.x + v2.x + v3.x + v4.x) / 4;
				sum.y = (v1.y + v2.y + v3.y + v4.y) / 4;
				sum.z = (v1.z + v2.z + v3.z + v4.z) / 4;
				sum.normalise();
				vertexNormals[i*gridWidth + j] = sum;	
			}
		}	
	}

}
