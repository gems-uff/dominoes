package boundary;

import java.awt.Color;

public class NodeLink {

	float capacity;
	float weight;
	String id;
	Color color = Color.BLACK;
	
	public Color getColor() {
		return color;
	}
	
	public float getWediht(){
		return weight;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getId() {
		return id;
	}

	public NodeLink(String id, float weight){
		this.id = id;
		this.weight = weight;
	}
	
	public String toString(){
		return id;
	}
}
