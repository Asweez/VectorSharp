package vectorsharp;

import java.text.DecimalFormat;

public class Vector {
	public double x, y, z;

	public static final Vector ZERO = new Vector(0, 0, 0);
	public static final Vector ONE = new Vector(1, 1, 1);

	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector(double x, double y) {
		this(x, y, 0);
	}

	public static Vector fromAngleMagnitude(double angleDegrees, double magnitude) {
		double angleRadians = (Math.PI / 180) * angleDegrees;
		return new Vector(magnitude * Math.cos(angleRadians), magnitude * Math.sin(angleRadians));
	}

	public Vector normalize() {
		double magnitude = getMagnitude();
		return new Vector(x / magnitude, y / magnitude, z / magnitude);
	}

	public static Vector midpoint(Vector u, Vector v) {
		return new Vector((u.x + v.x) / 2, (u.y + v.y) / 2, (u.z + v.z) / 2);
	}

	public static double completeTheSquareRadius(Vector v) {
		Vector u = midpoint(v, new Vector(0, 0, 0));
		return u.x * u.x + u.y * u.y + u.z * u.z;
	}

	public double getMagnitude() {
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
	}

	public double sqrMagnitude() {
		return Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2);
	}

	public Vector add(Vector v) {
		return new Vector(v.x + x, v.y + y, v.z + z);
	}

	public Vector subtract(Vector v) {
		return new Vector(x - v.x, y - v.y, z - v.z);
	}

	public Vector multiply(double scalar) {
		return new Vector(scalar * x, scalar * y, scalar * z);
	}

	public Vector divide(double scalar) {
		return multiply(1 / scalar);
	}

	public String toString() {
		DecimalFormat df = new DecimalFormat("#0.###");
		return "<" + df.format(x) + ", " + df.format(y) + ", " + df.format(z) + ">";
	}
}
