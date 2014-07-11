package com.josericardojunior.gitdataminer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class GeometryHelper {
	
	public static void drawCube(float width, float length, float height, Vector3f position,
			Vector3f color) {

		float halfWidth = width / 2;
		float halfHeight = height / 2;
		float halfLength = length / 2;

		GL11.glTranslatef(position.x, position.y, position.z);
		
		GL11.glBegin(GL11.GL_QUADS);
		// top
		GL11.glColor3f(color.x, color.y, color.z);
		GL11.glNormal3f(0, 1, 0); GL11.glVertex3f(halfWidth, halfHeight, -halfLength);
		GL11.glNormal3f(0, 1, 0); GL11.glVertex3f(-halfWidth, halfHeight, -halfLength);
		GL11.glNormal3f(0, 1, 0); GL11.glVertex3f(-halfWidth, halfHeight, halfLength);
		GL11.glNormal3f(0, 1, 0); GL11.glVertex3f(halfWidth, halfHeight, halfLength);

		// botton
		GL11.glColor3f(color.x, color.y, color.z);
		GL11.glNormal3f(0, -1, 0); GL11.glVertex3f(halfWidth, -halfHeight, halfLength);
		GL11.glNormal3f(0, -1, 0); GL11.glVertex3f(-halfWidth, -halfHeight, halfLength);
		GL11.glNormal3f(0, -1, 0); GL11.glVertex3f(-halfWidth, -halfHeight, -halfLength);
		GL11.glNormal3f(0, -1, 0); GL11.glVertex3f(halfWidth, -halfHeight, -halfLength);

		// front
		GL11.glColor3f(color.x, color.y, color.z);
		GL11.glNormal3f(0, 0, 1); GL11.glVertex3f(halfWidth, halfHeight, halfLength);
		GL11.glNormal3f(0, 0, 1); GL11.glVertex3f(-halfWidth, halfHeight, halfLength);
		GL11.glNormal3f(0, 0, 1); GL11.glVertex3f(-halfWidth, -halfHeight, halfLength);
		GL11.glNormal3f(0, 0, 1); GL11.glVertex3f(halfWidth, -halfHeight, halfLength);

		// back
		GL11.glColor3f(color.x, color.y, color.z);
		GL11.glNormal3f(0, 0, -1); GL11.glVertex3f(halfWidth, -halfHeight, -halfLength);
		GL11.glNormal3f(0, 0, -1); GL11.glVertex3f(-halfWidth, -halfHeight, -halfLength);
		GL11.glNormal3f(0, 0, -1); GL11.glVertex3f(-halfWidth, halfHeight, -halfLength);
		GL11.glNormal3f(0, 0, -1); GL11.glVertex3f(halfWidth, halfHeight, -halfLength);

		// left
		GL11.glColor3f(color.x, color.y, color.z);
		GL11.glNormal3f(-1, 0, 0); GL11.glVertex3f(-halfWidth, halfHeight, halfLength);
		GL11.glNormal3f(-1, 0, 0); GL11.glVertex3f(-halfWidth, halfHeight, -halfLength);
		GL11.glNormal3f(-1, 0, 0); GL11.glVertex3f(-halfWidth, -halfHeight, -halfLength);
		GL11.glNormal3f(-1, 0, 0); GL11.glVertex3f(-halfWidth, -halfHeight, halfLength);

		// right
		GL11.glColor3f(color.x, color.y, color.z);
		GL11.glNormal3f(1, 0, 0); GL11.glVertex3f(halfWidth, halfHeight, -halfLength);
		GL11.glNormal3f(1, 0, 0); GL11.glVertex3f(halfWidth, halfHeight, halfLength);
		GL11.glNormal3f(1, 0, 0); GL11.glVertex3f(halfWidth, -halfHeight, halfLength);
		GL11.glNormal3f(1, 0, 0); GL11.glVertex3f(halfWidth, -halfHeight, -halfLength);
		GL11.glEnd();
	}
}
