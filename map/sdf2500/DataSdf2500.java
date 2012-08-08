package map.sdf2500;

import java.awt.Rectangle;
import java.io.Serializable;

import map.Curve;
import map.Road;

/**
 * 数値地図2500の地図データ
 * @author ma38su
 */
public class DataSdf2500 implements Serializable {

	/**
	 * 建物ポリゴン
	 */
	private ExtendedPolygon[] buildingPgn;

	/**
	 * 墓地ポリゴン
	 */
	private ExtendedPolygon[] cemetery;

	/**
	 * 市区町村ポリゴン
	 */
	private ExtendedPolygon[] city;

	/**
	 * 国土基本図図名
	 * 図葉番号
	 * 最初の2文字は座標系の番号
	 */
	private final String code;

	/**
	 * JRアーク
	 */
	private Railway[] jr;

	/**
	 * 水域アーク
	 */
	private Curve[] mizuArc;

	/**
	 * 水域ポリゴン
	 */
	private ExtendedPolygon[] mizuPgn;
	
	/**
	 * その他の場ポリゴン
	 */
	private ExtendedPolygon[] other;
	
	/**
	 * 都市公園ポリゴン
	 */
	private ExtendedPolygon[] park;

	/**
	 * 鉄道敷ポリゴン
	 */
	private ExtendedPolygon[] railbase;
	
	/**
	 * 鉄道アーク
	 */
	private Railway[] railway;

	/**
	 * 道路アーク
	 */
	private Road[][] road;

	/**
	 * 学校ポリゴン
	 */
	private ExtendedPolygon[] school;

	/**
	 * 神社・寺ポリゴン
	 */
	private ExtendedPolygon[] shrine;

	/**
	 * 駅ポイント
	 */
	private ExtendedPoint[] station;
	
	/**
	 * 街区ポリゴン
	 */
	private ExtendedPolygon[] town;

	private Curve[] buildingArc;

	public DataSdf2500(String code) {
		this.code = code;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj.equals(this.code);
	}
	
	public ExtendedPolygon[] getBuildingPgn() {
		return this.buildingPgn;
	}
	
	public Curve[] getBuildingArc() {
		return this.buildingArc;
	}

	public ExtendedPolygon[] getCemetery() {
		return this.cemetery;
	}
	
	public ExtendedPolygon[] getCityPgn() {
		return this.city;
	}

	public Railway[] getJR() {
		return this.jr;
	}

	public Curve[] getMizuArc() {
		return this.mizuArc;
	}

	public ExtendedPolygon[] getMizuPgn() {
		return this.mizuPgn;
	}

	public ExtendedPolygon[] getOther() {
		return this.other;
	}
	public ExtendedPolygon[] getPark() {
		return this.park;
	}
	public ExtendedPolygon[] getRailbase() {
		return this.railbase;
	}

	public Railway[] getRailway() {
		return this.railway;
	}

	public Road[][] getRoad() {
		return this.road;
	}

	public ExtendedPolygon[] getSchool() {
		return this.school;
	}

	public ExtendedPolygon[] getShrine() {
		return this.shrine;
	}

	public ExtendedPoint[] getStation() {
		return this.station;
	}

	public ExtendedPolygon[] getTown() {
		return this.town;
	}

	@Override
	public int hashCode() {
		return this.code.hashCode();
	}

	public boolean intersects(Rectangle rect) {
		if (this.city != null) {
			for (ExtendedPolygon polygon : this.city) {
				if (rect.intersects(polygon.getBounds())) {
					return true;
				}
			}
		}
		return false;
	}

	void setBuilding(ExtendedPolygon[] polygon, Curve[] arc) {
		this.buildingPgn = polygon;
		this.buildingArc = arc;
	}

	void setCemetery(ExtendedPolygon[] polygon) {
		this.cemetery = polygon;
	}
	void setGyuosei(ExtendedPolygon[] city, ExtendedPolygon[] town) {
		this.town = town;
		this.city = city;
	}
	void setJR(Railway[] jr) {
		this.jr = jr;
	}

	void setMizu(Curve[] arc, ExtendedPolygon[] polygon) {
		this.mizuArc = arc;
		this.mizuPgn = polygon;
	}
	
	public void setOther(ExtendedPolygon[] other) {
		this.other = other;
	}

	void setPark(ExtendedPolygon[] park) {
		this.park = park;
	}

	void setRailbase(ExtendedPolygon[] railbase) {
		this.railbase = railbase;
	}

	void setRailway(Railway[] others) {
		this.railway = others;
	}

	void setRoad(Road[][] road) {
		this.road = road;
	}

	public void setSchool(ExtendedPolygon[] school) {
		this.school = school;
	}

	void setShrine(ExtendedPolygon[] polygon) {
		this.shrine = polygon;
	}

	void setStation(ExtendedPoint[] station) {
		this.station = station;
	}
}
