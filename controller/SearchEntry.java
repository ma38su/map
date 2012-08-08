package controller;

import java.awt.Point;

/**
 * 住所検索結果のためのトランスファー・オブジェクト
 * @author ma38su
 */
public class SearchEntry {
	private int code;
	private String address;
	private Point point;
	public SearchEntry(int code, String address) {
		this.code = code;
		this.address = address;
	}

	public SearchEntry(String address, Point p) {
		this.point = p;
		this.address = address;
	}

	public int getCode() {
		return this.code;
	}
	
	public Point getPoint() {
		return this.point;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Integer) {
			Integer key = (Integer) obj;
			return key.intValue() == this.code;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return this.address;
	}

	@Override
	public int hashCode() {
		return this.code;
	}
}
