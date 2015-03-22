package com.josericardojunior.gitdataminer;

import org.lwjgl.input.*;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import static java.lang.Math.*;
import static org.lwjgl.util.glu.GLU.*;




/** Camera class.
 */

public final class Camera
{

	  /** A zero vector. **/
	private Vector3f vZero = new Vector3f(0.0f, 0.0f, 0.0f);

	  /** The view vector. **/
	private Vector3f vView = new Vector3f(0.0f, 0.0f, 5.0f);

	  /** Up vector (rarely changed). **/
	private Vector3f vUp = new Vector3f(0.0f, 1.0f, 0.0f);

	  /** Strafe vector. **/
	private Vector3f m_vStrafe = new Vector3f();

	  /** Position vector. **/
	private Vector3f m_vPosition = vZero;

	  /** View Vector. **/
	private Vector3f m_vView = vView;

	  /** up vector. **/
	private Vector3f m_vUpVector = vUp;

	  /** View by mouse ? Default = false. **/
	private boolean viewByMouse = false;

	  /** Is mouse inverted ? Default = false. **/
	private boolean mouseInverted = false;

	  /** Mouse sensibility. Default = 750.0f. **/
	private float mouseSensibility = 40.0f;

	  /** Current mouse rotation on X axis. **/
	private float currentRotX = 0.0f;
	
	  /** Fixed on X axis ? **/
	private boolean fixedX = false;
	
	  /** Fixed on Y axis ? **/
	private boolean fixedY = false;
	
	  /** Fixed on Z axis ? **/
	private boolean fixedZ = false;
	
	private float radius;
	
	private Vector3f pr;              /* Point to rotate about   */
	private float focallength;  /* Focal Length along vd   */
	private float aperture;     /* Camera aperture         */
	private float eyesep;       /* Eye separation          */

	
	  /**  **/
	public final static int X_AXIS = 0;
	
	  /**  **/
	public final static int Y_AXIS = 1;
	
	  /**  **/
	public final static int Z_AXIS = 2;



	/** Camera constructor.
	 */

	public Camera()
	{
	}



	/** Camera constructor.
	 *  @param vbm View by mouse statement.
	 */

	public Camera(boolean vbm)
	{
		viewByMouse = vbm;
	}



	/** Change the view by mouse statement.
	 *  @param vbm View by mouse statement.
	 */
	
	public final void setViewByMouse(boolean vbm)
	{
		viewByMouse = vbm;
	}
	
	public final boolean getViewByMouse()
	{
		return viewByMouse;
	}



	/** Change mouse Y movement type.
	 *  @param inverted Mouse Inverted ?
	 */
	
	public final void setMouseInverted(boolean inverted)
	{
		mouseInverted = inverted;
	}

	public Vector3f getVview()
	{
		return m_vView;
	}

	/** Change mouse sensibility.
	 *  @param s Mouse sensibility (high = less sensible).
	 */
	
	public final void setMouseSensibility(float s)
	{
		mouseSensibility = s;
	}

	public final void setVview(float y)
	{
		m_vPosition.y = 0;
	}

	/** Set camera fixed to an axis.
	 *  @param axis <code>Camera.X_AXIS</code> or <code>Camera.Y_AXIS</code> or <code>Camera.Z_AXIS</code>.
	 */
	
	public final void setFixedAxis(int axis)
	{
		if(axis == X_AXIS)
			fixedX = true;
		else if(axis == Y_AXIS)
			fixedY = true;
		else if(axis == Z_AXIS)
			fixedZ = true;
	}



	/** Change the camera position.
	 *  @param positionX Position X.
	 *  @param positionY Position Y.
	 *  @param positionZ Position Z.
	 *  @param viewX View X.
	 *  @param viewY View Y.
	 *  @param viewZ View Z.
	 *  @param upVectorX Up vector X.
	 *  @param upVectorY Up vector Y.
	 *  @param upVectorZ Up vector Z.
	 */
	
	public final void setPosition(float positionX, float positionY, float positionZ,
								  float viewX,     float viewY,     float viewZ,
								  float upVectorX, float upVectorY, float upVectorZ)
	{
		m_vPosition = new Vector3f(positionX, positionY, positionZ);
		m_vView		= new Vector3f(viewX, viewY, viewZ);
		m_vUpVector = new Vector3f(upVectorX, upVectorY, upVectorZ);
	}


	public final Vector3f[] getPositionCamera()
	{
		Vector3f[] position = {m_vPosition, m_vView, m_vUpVector};
		return position;
	}

	public void setPosition(Vector3f m_vPosition)
	{
		
		this.m_vPosition = m_vPosition;
	}
	
	public void setView(Vector3f m_vView)
	{
		
		this.m_vView = m_vView;
	}
	
	public void setUpVector(Vector3f m_vUpVector)
	{
		
		this.m_vUpVector = m_vUpVector;
	}
	
	
	public final Vector3f getPosition()
	{
		
		return m_vPosition;
	}
	
	public final Vector3f getView()
	{
		
		return m_vView;
	}
	
	public final Vector3f getUpVector()
	{
		
		return m_vUpVector;
	}
	
	
	
	
	/** Move the camera (forward if speed is positive).
	 *  @param speed The camera speed.
	 */
	
	public final void move(float speed)
	{
		Vector3f vVector= new Vector3f();

		  // Get our view vector (The direction we are facing).
		vVector = Vector3f.sub(m_vView, m_vPosition, null);
		//vVector.x = m_vView.x - m_vPosition.x;
		//vVector.y = m_vView.y - m_vPosition.y;
		//vVector.z = m_vView.z - m_vPosition.z;

		  // That way you don't move faster than you strafe, since the strafe vector
		  // is normalized too.
		vVector = vVector.normalise(vVector);

		  // Fixed axis ?
		if(!fixedX) 
		{ 
			m_vPosition.x += vVector.x * speed; 
			m_vView.x += vVector.x * speed; 
		}
		if(!fixedY) 
		{ 
			m_vPosition.y += vVector.y * speed; 
			m_vView.y += vVector.y * speed; 
		}
		if(!fixedZ) 
		{ 
			m_vPosition.z += vVector.z * speed; 
			m_vView.z += vVector.z * speed; 
		}
	}
	


	/** Set the view according to the mouse position.
	 */
	
	private final void mouse_view()
	{
		float angleY = 0.0f;
		float angle_product = 0.0f;
		final Vector3f vAxis = new Vector3f();

		  // Get the direction the mouse moved in, but bring the number down to a reasonable amount.
		angleY = -(float)(Mouse.getDX())/ mouseSensibility;
		angle_product = (float)(Mouse.getDY())/ mouseSensibility;

		  // If mouse is inverted, invert rotation on angle_product axis.
		if(mouseInverted)
			angle_product = -angle_product;

		  // Here we keep track of the current rotation (for up and down) so that
		  // we can restrict the camera from doing a full 360 loop.
		currentRotX -= angle_product;

		  // If the current rotation (in radians) is greater than 1.0, we want to cap it.
		if(currentRotX > 90.0f)
			currentRotX = 90.0f;

		  // Check if the rotation is below -1.0, if so we want to make sure it doesn't continue.
		else if(currentRotX < -90.0f)
			currentRotX = -90.0f;

		  // Otherwise, we can rotate the view around our position.
		else
		{
			Vector3f sub = new Vector3f();
			Vector3f.sub(m_vView, m_vPosition, sub);
			Vector3f.cross(sub, m_vUpVector, vAxis);
			vAxis.normalise();

			  // Rotate around our perpendicular axis and along the y-axis.
			rotateWithQuaternion(angle_product, vAxis.x, vAxis.y, vAxis.z);
			rotateWithQuaternion(angleY, 0, 1, 0);
			//rotate(angleZ, vAxis.x, vAxis.y, vAxis.z);
			//rotate(angleY, 0, 1, 0);
		}
	}



	/** This rotates the view around the position using an axis-angle rotation.
	 *  @param angle Rotation angle.
	 *  @param x On X axis.
	 *  @param y On Y axis.
	 *  @param z On Z axis.
	 */

	@SuppressWarnings("unused")
	private final void rotate(float angle, float x, float y, float z)
	{
		Vector3f vNewView = new Vector3f();
		Vector3f vView = new Vector3f();	

		  // Get our view vector (The direction we are facing).
		
		vView.x = m_vView.x - m_vPosition.x;		// This gets the direction of the X.
		vView.y = m_vView.y - m_vPosition.y;		// This gets the direction of the Y.
		vView.z = m_vView.z - m_vPosition.z;		// This gets the direction of the Z.

		  // Calculate the sine and cosine of the angle once.
		float cosTheta = (float)cos(angle);
		float sinTheta = (float)sin(angle);

		  // Find the new x position for the new rotated point.
		vNewView.x  = (cosTheta + (1 - cosTheta) * x * x)	* vView.x;
		vNewView.x += ((1 - cosTheta) * x * y - z * sinTheta)	* vView.y;
		vNewView.x += ((1 - cosTheta) * x * z + y * sinTheta)	* vView.z;

		  // Find the new y position for the new rotated point.
		vNewView.y  = ((1 - cosTheta) * x * y + z * sinTheta)	* vView.x;
		vNewView.y += (cosTheta + (1 - cosTheta) * y * y)	* vView.y;
		vNewView.y += ((1 - cosTheta) * y * z - x * sinTheta)	* vView.z;

		  // Find the new z position for the new rotated point.
		vNewView.z  = ((1 - cosTheta) * x * z - y * sinTheta)	* vView.x;
		vNewView.z += ((1 - cosTheta) * y * z + x * sinTheta)	* vView.y;
		vNewView.z += (cosTheta + (1 - cosTheta) * z * z)	* vView.z;

		  // Now we just add the newly rotated vector to our position to set.
		  // our new rotated view of our camera.
		m_vView.x = m_vPosition.x + vNewView.x;
		m_vView.y = m_vPosition.y + vNewView.y;
		m_vView.z = m_vPosition.z + vNewView.z;
	}

	private final void rotateWithQuaternion(final float angleDir, final float xSpeed, final float ySpeed, final float zSpeed)
	{
		Quaternion qRotation = new Quaternion();
		Quaternion qView = new Quaternion();
		Quaternion qNewView = new Quaternion();

		// Create the rotation quaternion based on the axis we are rotating on.
		Vector4f quat = new Vector4f(xSpeed, ySpeed, zSpeed, angleDir);
		qRotation.setFromAxisAngle(quat);

		// Create the view quaternion.  This will be the direction of the view and position.
		qView.x = m_vView.x - m_vPosition.x;		// This gets the direction of the X.
		qView.y = m_vView.y - m_vPosition.y;		// This gets the direction of the Y.
		qView.z = m_vView.z - m_vPosition.z;		// This gets the direction of the Z.
		qView.w = 0;

		// Create the resulting quaternion by multiplying the rotation quat by the view quat
		// then multiplying that by the conjugate of the rotation quat.
		
		Quaternion q = new Quaternion();
		Quaternion.mul(qRotation, qView, q);
		
		Quaternion.mul(q, qRotation.negate(qRotation), qNewView);
		
		//qNewView = Quaternion.multiply(Quaternion.multiply(qRotation,qView), qRotation.conjugate());

		// Update the view information by adding the position to the resulting quaternion.
		m_vView.x = m_vPosition.x + qNewView.x;
		m_vView.y = m_vPosition.y + qNewView.y;
		m_vView.z = m_vPosition.z + qNewView.z;
   
	}



	/** Strafe the camera (left or right, depending on the speed).
	 *  @param speed The camera moving speed.
	 */
	
	public final void strafe(float speed)
	{
		  // Add the strafe vector to our position.
		m_vPosition.x += m_vStrafe.x * speed;
		m_vPosition.z += m_vStrafe.z * speed;

		  // Add the strafe vector to our view.
		m_vView.x += m_vStrafe.x * speed;
		m_vView.z += m_vStrafe.z * speed;
	}



	/** Update the camera state.
	 */
	
	public final void update() 
	{
		  // Normalize the strafe vector.
		Vector3f sub = new Vector3f();
		Vector3f.sub(m_vView, m_vPosition, sub);
		Vector3f.cross(sub, m_vUpVector, m_vStrafe);
		//m_vStrafe = VectorMath.cross_product(VectorMath.subtract(m_vView , m_vPosition), m_vUpVector);
		//VectorMath.normalize(m_vStrafe);
		m_vStrafe.normalise();

		  // View by mouse if enabled.
		if(viewByMouse)
			mouse_view();
	}



	/** Update camera view.
	 */
	
	public final void look()
	{
		
		gluLookAt(m_vPosition.x, m_vPosition.y, m_vPosition.z,
				  m_vView.x,	 m_vView.y,     m_vView.z,	
				  m_vUpVector.x, m_vUpVector.y, m_vUpVector.z);
	}



	/**
	 * @param radius the radius to set
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}



	/**
	 * @return the radius
	 */
	public float getRadius() {
		return radius;
	}



	/**
	 * @param pr the pr to set
	 */
	public void setPr(Vector3f pr) {
		this.pr = pr;
	}



	/**
	 * @return the pr
	 */
	public Vector3f getPr() {
		return pr;
	}



	/**
	 * @param focallength the focallength to set
	 */
	public void setFocallength(float focallength) {
		this.focallength = focallength;
	}



	/**
	 * @return the focallength
	 */
	public float getFocallength() {
		return focallength;
	}



	/**
	 * @param aperture the aperture to set
	 */
	public void setAperture(float aperture) {
		this.aperture = aperture;
	}



	/**
	 * @return the aperture
	 */
	public float getAperture() {
		return aperture;
	}



	/**
	 * @param eyesep the eyesep to set
	 */
	public void setEyesep(float eyesep) {
		this.eyesep = eyesep;
	}



	/**
	 * @return the eyesep
	 */
	public float getEyesep() {
		return eyesep;
	}
}
