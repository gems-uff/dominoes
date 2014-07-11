package charts3D;

public abstract class Base3DChart {
	
	private String name;

	public Base3DChart(String _name){
		name = _name;
	}
	
	public abstract void Render();
}
