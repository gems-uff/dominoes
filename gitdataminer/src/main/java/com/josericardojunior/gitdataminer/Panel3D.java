package com.josericardojunior.gitdataminer;


import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;

import javax.swing.JPanel;

/*import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;*/

import charts3D.Base3DChart;

@SuppressWarnings("restriction")
public class Panel3D extends JPanel {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Canvas parent_canvas;
	Thread visualizationThread;
	boolean running = false;
	boolean paused = false;
	Camera cam = new Camera();
	Base3DChart currentChart = null;
	
	public Panel3D(){
		//super();
		setLayout(new BorderLayout());
		setBounds(272, 454, 528, 333);
		parent_canvas = new Canvas() {
			
			public final void addNotify(){
				super.addNotify();
				startLWJGL();
			}
			
			public final void removeNotify(){
				stopLWJGL();
				super.removeNotify();
			}
		};
		
		
		parent_canvas.setBounds(getBounds());
		add(parent_canvas, BorderLayout.CENTER);
		parent_canvas.setFocusable(true);
		parent_canvas.requestFocus();
		parent_canvas.setIgnoreRepaint(true);
		setVisible(true);
	}
	
	private void startLWJGL(){
		/*visualizationThread = new Thread() {
			public void run(){
				running = true;
				
				try {
					
					Display.setParent(parent_canvas);
					Display.create();
					initGL();
				} catch (LWJGLException e) {
					e.printStackTrace();
				}
				
				renderLoop();
			}
		};
		visualizationThread.start();*/
		
	}
	
	private void stopLWJGL(){
		running = false;
		
		try {
			visualizationThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void renderLoop(){
		/*cam.setViewByMouse(true);
		Display.sync(60);
		try {
			Mouse.create();
			Keyboard.create();
			Keyboard.enableRepeatEvents(true);
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (running){

			if (!paused){
				render();

				if (Keyboard.isKeyDown(Keyboard.KEY_W)){
					cam.move(10f * 1.0f/60.0f);
				}
				
				if (Keyboard.isKeyDown(Keyboard.KEY_S)){
					cam.move(-10f * 1.0f/60.0f);
				}
				
				if (Keyboard.isKeyDown(Keyboard.KEY_A)){
					cam.strafe(-10f * 1.0f/60.0f);;
				}
				
				if (Keyboard.isKeyDown(Keyboard.KEY_D)){
					cam.strafe(10f * 1.0f/60.0f);
				}
				
				if (Mouse.isButtonDown(0))
					cam.setViewByMouse(true);
				else 
					cam.setViewByMouse(false);
				
				cam.update();
			}
			
			Display.update();
		}
		
		Display.destroy();*/
	}
	
	public synchronized void setChart(Base3DChart _chart){
		currentChart = _chart;
	}
	
	private synchronized Base3DChart getCurrentChart(){
		return currentChart;
	}
	
	
	private void render(){
		/*GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	    
	    GL11.glMatrixMode(GL11.GL_MODELVIEW);
	    GL11.glLoadIdentity();
	    cam.look();
	    
	    Base3DChart chart = getCurrentChart();
	    
	    if (chart != null)
	    	chart.Render();
	    
	    // Draw Axis
	    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
	    GL11.glLineWidth(3);
	    GL11.glDisable(GL11.GL_LIGHTING);
	    DrawAxis();
	    GL11.glEnable(GL11.GL_LIGHTING);
	    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);*/
	}
	
	private void DrawAxis(){
	  /*  GL11.glLineWidth(5);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3f(1, 0, 0);
		GL11.glVertex3f(0, 0, 0);
		GL11.glVertex3f(5, 0, 0);
		
		GL11.glColor3f(0, 1, 0);
		GL11.glVertex3f(0, 0, 0);
		GL11.glVertex3f(0, 5, 0);
		
		GL11.glColor3f(0, 0, 1);
		GL11.glVertex3f(0, 0, 0);
		GL11.glVertex3f(0, 0, 5);
		GL11.glEnd();*/
	}
	
	private void initGL(){
	/*	Dimension d = getSize();
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
	    GL11.glLoadIdentity();
	    GLU.gluPerspective(60.0f, (float)d.width/(float)d.height, 0.1f, 1000.0f);
	    
		GL11.glClearColor(0, 0, 0, 0);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_LIGHT0);
		GL11.glViewport(0, 0, getWidth(), getHeight());
		
		cam.setPosition(new Vector3f(0, 0, 10));*/
	}
	
}
