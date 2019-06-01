package de.timeout.libs.vector;

import org.bukkit.util.Vector;

public class Straight {

	private Vector supportVector, directionVector;
	
	public Straight(Vector support, Vector direction) {
		this.supportVector = support;
		this.directionVector = direction;
	}
	
	public boolean isInStraight(Vector coord) {
		double ri = (coord.getX() - supportVector.getX()) / directionVector.getX();
		if(ri == (coord.getY() - supportVector.getY()) / directionVector.getY())
			return ri == (coord.getZ() - supportVector.getZ()) / directionVector.getZ();
		return false;
	}
	
	public double angle(Straight straight) {
		return straight.getDirectionVector().angle(directionVector);
	}

	/**
	 * @return the support vector
	 */
	public Vector getSupportVector() {
		return supportVector;
	}

	/**
	 * @param supportVector the support vector to set
	 */
	public void setSupportVector(Vector supportVector) {
		this.supportVector = supportVector;
	}

	/**
	 * @return the direction vector
	 */
	public Vector getDirectionVector() {
		return directionVector;
	}

	/**
	 * @param directionVector the direction vector to set
	 */
	public void setDirectionVector(Vector directionVector) {
		this.directionVector = directionVector;
	}
}
