package boundary;

import java.awt.Color;

public class NodeInfo {
	String id;
	String userData;
	private float threshold = 0;
	
	boolean isHighlighted = false;
	Color color = Color.BLACK;
	
	public void setHighlighted(boolean isHighlighted) {
		this.isHighlighted = isHighlighted;
	}

	static final Color Highlight = Color.YELLOW;
	
	
	
	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}


	
	public Color getColor() {
		if (isHighlighted)
			return Highlight;
		
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getId() {
		return id;
	}

	public NodeInfo(String id){
		this.id = id;
	}
	
	public String toString(){
		return id;
	}
	
	public boolean isHighlighted(){
		return isHighlighted;
	}
	
	public float getThreshold() {
		return threshold;
	}

	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}
	
}
