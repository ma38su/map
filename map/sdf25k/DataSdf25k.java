package map.sdf25k;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Map;

import labeling.Label;
import map.Curve;
import map.Road;

/**
 * 数値地図25000の地図データ
 * @author ma38su
 */
public class DataSdf25k implements Serializable {

	private Rectangle area;

	private Curve[] border;
	
	private Curve[] coast;

	/**
	 * 市区町村番号
	 */
	private Integer code;

	private Curve[] jr;
	
	private Map<String, Label[]> label;
	
	private Mesh[][] mesh;

	private Map<Integer, Node> nodes;

	private Curve[] railway;

	private Curve[] river;

	private Road[][] road;

	private Station[] station;
	public DataSdf25k(Integer code, Rectangle area) {
		this.code = code;
		this.area = area;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.equals(this.code);
	}
	
	public Rectangle getArea() {
		return this.area;
	}
	
	public Curve[] getBorder() {
		return this.border;
	}
	
	public Curve[] getCoast() {
		return this.coast;
	}
	
	public Integer getCode() {
		return this.code;
	}
	
	public Curve[] getJr() {
		return this.jr;
	}

	public Map<String, Label[]> getLabel() {
		return this.label;
	}

	public Mesh[][] getMesh() {
		return this.mesh;
	}

	public Map<Integer, Node> getNodes() {
		return this.nodes;
	}
	
	public Curve[] getRailway() {
		return this.railway;
	}

	public Curve[] getRiver() {
		return this.river;
	}

	public Road[][] getRoad() {
		return this.road;
	}

	public Station[] getStation() {
		return this.station;
	}

	@Override
	public int hashCode() {
		return this.code;
	}

	public boolean intersects(Rectangle rect) {
		return this.area.intersects(rect);
	}

	public void setBorder(Curve[] border) {
		this.border = border;
	}

	void setCoast(Curve[] coast) {
		this.coast = coast;
	}

	public void setJr(Curve[] jr) {
		this.jr = jr;
	}

	public void setLabel(Map<String, Label[]> label) {
		this.label = label;
	}

	void setMesh(Mesh[][] mesh) {
		this.mesh = mesh;
	}

	public void setNodes(Map<Integer, Node> nodes) {
		this.nodes = nodes;
	}

	public void setRailway(Curve[] railway) {
		this.railway = railway;
	}

	void setRailway(Curve[] jr, Curve[] railway) {
		this.jr = jr;
		this.railway = railway;
	}

	void setRiver(Curve[] river) {
		this.river = river;
	}

	public void setRoad(Road[][] road) {
		this.road = road;
	}

	void setStation(Station[] station) {
		this.station = station;
	}

}
