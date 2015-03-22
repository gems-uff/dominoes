package charts3D;

import java.awt.List;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.josericardojunior.gitdataminer.GeometryHelper;
import com.josericardojunior.gitdataminer.Matrix2D;
import com.josericardojunior.gitdataminer.MatrixDescriptor;

public class VolumeChart extends Base3DChart {
	private int volumeWidth;
	private int volumeLenght;
	java.util.List<Vector4f> positions;
	Vector3f[] borders;
	boolean renderBorder = false;
	
	public VolumeChart(String _name) {
		super(_name);
		// TODO Auto-generated constructor stub
	}
	
	public void setBorders(Vector3f [] _minMaxBorders){
		borders = _minMaxBorders;
	}
	
	public synchronized void renderBorder(boolean _render){
		renderBorder = _render;
	}
	
	public synchronized boolean getRenderBorder(){
		return renderBorder;
	}
	
	public void setData(Matrix2D matrix){
		
		MatrixDescriptor matDesc = matrix.getMatrixDescriptor();
		
				
		volumeWidth = matDesc.getNumRows();
		volumeLenght = matDesc.getNumCols();
		
		float normalizeWidth = 2.0f / volumeWidth;
		float normalizeLenght = 2.0f / volumeLenght;
		float normalizedHeight = 2.0f / volumeWidth;
		
		positions = new ArrayList<Vector4f>();
		
		for (int i = 0; i < matDesc.getNumRows(); i++){
			for (int j = 0; j < matDesc.getNumCols(); j++){
				
				float value = matrix.GetElement(i, j);
				
				//positions.add( new Vector4f(((float)i * normalizeWidth),
					//	value, (j * normalizeLenght), value));
				
				if (value > 0){
					positions.add( new Vector4f( ((float) i) + 0.5f,
							value - 0.5f, ((float) j) + 0.5f, value));
				}
			}
		}
	}

	@Override
	public void Render() {
		
		GL11.glPushMatrix();
		

		//GL11.glDisable(GL11.GL_LIGHTING);
		//GL11.glLineWidth(5);
		//GL11.glBegin(GL11.GL_LINES);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		for (Vector4f poscolor : positions){
			Vector3f pos = new Vector3f(poscolor.x, 0.5f, poscolor.z);
			Vector3f color = new Vector3f(poscolor.w, 0, 0);
			GL11.glColor3f(color.x, color.y, color.z);
			GL11.glVertex3f(pos.x, pos.y, pos.z);
			GL11.glPushMatrix();
			GeometryHelper.drawCube(1, 1, 
					1, pos, color);
			GL11.glPopMatrix();
		}
		GL11.glEnd();
		GL11.glPopMatrix();
		
		if (getRenderBorder()){
		    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		    GL11.glLineWidth(3);
		    GL11.glDisable(GL11.GL_LIGHTING);
		    
			// BOrders
			GL11.glPushMatrix();
			
			for (int i = 0; i < borders.length-1; i+=2){
				Vector3f min = borders[i];
				Vector3f max = borders[i+1];
				Vector3f size = Vector3f.sub(max, min, null);
				size.x += 1;
				size.z += 1;
				Vector3f pos = new Vector3f();
				pos.x = min.x + (size.x / 2.0f);
				pos.y = min.y + (size.y / 2.0f);
				pos.z = min.z + (size.z / 2.0f);
				
				
				GL11.glPushMatrix();
				GeometryHelper.drawCube(size.x, size.z, 
						1, pos, new Vector3f(1, 1, 1));
				GL11.glPopMatrix();
			}
			GL11.glPopMatrix();
		}
		
	    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	    GL11.glEnable(GL11.GL_LIGHTING);
	}
}
