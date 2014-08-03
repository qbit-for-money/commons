package com.qbit.commons.model;

import java.io.Serializable;

/**
 * @author Alexander_Sergeev
 */
public class Point2 implements Serializable, Comparable<Point2> {

	private final int x;
	private final int y;

	public Point2() {
		this(0, 0);
	}

	public Point2(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public int len2() {
		return (x * x + y * y);
	}

	public int len() {
		return (int) Math.round(Math.sqrt(x * x + y * y));
	}

	public Point2 add(Point2 o) {
		return new Point2(x + o.getX(), y + o.getY());
	}
	
	public Point2 sub(Point2 o) {
		return new Point2(x - o.getX(), y - o.getY());
	}

	public Point2 mul(int k) {
		return new Point2(k * x, k * y);
	}
	
	public boolean isPositive() {
		return ((x > 0) && (y > 0));
	}
	
	public boolean isZero() {
		return ((x == 0) && (y == 0));
	}
	
	public boolean isInsideSquare(Point2 square) {
		Point2 delta = square.sub(this);
		return (delta.isPositive() || delta.isZero());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + this.x;
		hash = 79 * hash + this.y;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Point2 other = (Point2) obj;
		if (this.x != other.x) {
			return false;
		}
		if (this.y != other.y) {
			return false;
		}
		return true;
	}
	
	@Override
	public int compareTo(Point2 o) {
		if (o == null) {
			return 1;
		}
		return (len() - o.len());
	}

	@Override
	public String toString() {
		return "Point2{" + "x=" + x + ", y=" + y + '}';
	}
}
