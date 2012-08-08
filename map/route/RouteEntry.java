package map.route;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import map.Road;
import map.sdf25k.Node;

public class RouteEntry {

	private double cost;
	private Node start;
	private Node terminal;
	private final Set<Road> route;

	public RouteEntry() {
		this.route = null;
	}
	public RouteEntry(Node s, Node t) {
		this.start = s;
		this.terminal = t;
		this.route = new HashSet<Road>();
		this.cost = Double.POSITIVE_INFINITY;
	}
	
	public void setRoute(Node s, Node t) {
		this.start = s;
		this.terminal = t;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RouteEntry) {
			RouteEntry entry = (RouteEntry) obj;
			return this.start.equals(entry.start) && this.terminal.equals(entry.terminal);
		}
		return false;
	}

	public double getCost() {
		return this.cost;
	}

	public Collection<Road> getRoute() {
		return this.route;
	}

	public boolean add(Road road) {
		return this.route.add(road);
	}

	public void clear() {
		this.route.clear();
	}

	@Override
	public int hashCode() {
		return this.start.hashCode() + this.terminal.hashCode();
	}
	
	public Node getTerminal() {
		return this.terminal;
	}
	public Node getStart() {
		return this.start;
	}
}
