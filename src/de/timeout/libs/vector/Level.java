package de.timeout.libs.vector;


public class Level {

	private final Vector supportVector;
	private final Vector normalVector;
	
	public Level(Vector supportVector, Vector v, Vector u) {
		this.supportVector = supportVector;
		this.normalVector = new Vector(v.clone().crossProduct(u).normalize());
	}
	
	public Level(Vector supportVector, Vector normalVector) {
		this.supportVector = supportVector;
		this.normalVector = normalVector.normalize();
	}
	
	public Level(double x, double y, double z, double y0) {
		this.normalVector = new Vector(x, y, z).normalize();
		this.supportVector = new Vector().add(new Vector(0D, 1D, 0D).clone().multiply(y0));
	}
	
	public boolean isInLevel(Vector coord) {
		return coord.clone().subtract(supportVector).dot(normalVector) == 0;
	}
	
	public double distance(Vector coord) {
		return Math.abs(coord.clone().subtract(supportVector).dot(normalVector));
	}
	
	public boolean isInLevel(Straight straight) {
		return normalVector.angle(straight.getDirectionVector()) == 0D && straight.isInStraight(supportVector);
	}
	
	public Vector cross(Straight straight) {
		double r = (supportVector.getX() - straight.getSupportVector().getX() +
					supportVector.getY() - straight.getSupportVector().getY() + 
					supportVector.getZ() - straight.getSupportVector().getZ()) / 
					(straight.getDirectionVector().getX() + straight.getDirectionVector().getY() + straight.getDirectionVector().getZ());
		return straight.getSupportVector().clone().add(straight.getDirectionVector().clone().multiply(r));
	}
}
