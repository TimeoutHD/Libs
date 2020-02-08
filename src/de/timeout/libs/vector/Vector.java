package de.timeout.libs.vector;

import java.util.Map;
import java.util.SplittableRandom;

import javax.annotation.Nonnull;

import org.bukkit.configuration.serialization.SerializableAs;

import com.google.common.base.Preconditions;

import net.jafama.DoubleWrapper;
import net.jafama.FastMath;

/**
 * This class reinitialize Methods with FastMathUtils
 * @author Timeout
 *
 */
@SerializableAs("Vector")
public class Vector extends org.bukkit.util.Vector {
	
	private static final SplittableRandom random = new SplittableRandom();

	private static final double EPSILON = 1.0E-6D;
	  
	public Vector() {
		this.x = 0.0D;
	    this.y = 0.0D;
	    this.z = 0.0D;
	}
	  
	public Vector(int x, int y, int z) {
		this.x = x;
	    this.y = y;
	    this.z = z;
	}

	public Vector(double x, double y, double z) {
		this.x = x;
	    this.y = y;
	    this.z = z;
	}
	  
	public Vector(float x, float y, float z) {
	    this.x = x;
	    this.y = y;
	    this.z = z;
	}
	
	public Vector(org.bukkit.util.Vector old) {
		this.x = old.getX();
		this.y = old.getY();
		this.z = old.getZ();
	}

	@Override
	public Vector add(@Nonnull org.bukkit.util.Vector vec) {
	    this.x += vec.getX();
	    this.y += vec.getY();
	    this.z += vec.getZ();
	    return this;
	}

	@Override
	public Vector subtract(@Nonnull org.bukkit.util.Vector vec) {
	    this.x -= vec.getX();
	    this.y -= vec.getY();
	    this.z -= vec.getZ();
	    return this;
	}

	@Override
	public Vector multiply(@Nonnull org.bukkit.util.Vector vec) {
	    this.x *= vec.getX();
	    this.y *= vec.getY();
	    this.z *= vec.getZ();
	    return this;
	}

	@Override
	public Vector divide(@Nonnull org.bukkit.util.Vector vec) {
		this.x /= vec.getX();
		this.y /= vec.getY();
	  this.z /= vec.getZ();
	  	return this;
	}

	@Override
	public Vector copy(@Nonnull org.bukkit.util.Vector vec) {
	    this.x = vec.getX();
	    this.y = vec.getY();
	    this.z = vec.getZ();
	    return this;
	}

	@Override
	public double length() {
		return FastMath.sqrtQuick(lengthSquared());
	}

	@Override
	public double lengthSquared() {
		return FastMath.pow2(this.x) + FastMath.pow2(this.y) + FastMath.pow2(this.z);
	}

	@Override
	public double distance(@Nonnull org.bukkit.util.Vector o) {
		return FastMath.sqrtQuick(distanceSquared(o));
	}
	  
	@Override
	public double distanceSquared(@Nonnull org.bukkit.util.Vector o) {
		return FastMath.pow2(this.x - o.getX()) + FastMath.pow2(this.y - o.getY()) + FastMath.pow2(this.z - o.getZ());
	}
 
	@Override
	public float angle(@Nonnull org.bukkit.util.Vector other) {
		double dot = FastMath.min(FastMath.max(dot(other) / length() * other.length(), -1.0D), 1.0D);
	    
	    return (float) FastMath.acos(dot);
	}
 
	@Override
	public Vector midpoint(@Nonnull org.bukkit.util.Vector other) {
		this.x = (this.x + other.getX()) / 2.0D;
		this.y = (this.y + other.getY()) / 2.0D;
		this.z = (this.z + other.getZ()) / 2.0D;
		return this;
	}

	@Override
	public Vector getMidpoint(@Nonnull org.bukkit.util.Vector other) {
	    double x = (this.x + other.getX()) / 2.0D;
	    double y = (this.y + other.getY()) / 2.0D;
	    double z = (this.z + other.getZ()) / 2.0D;
	    return new Vector(x, y, z);
	}
	  
	@Override
	public Vector multiply(int m) {
	    this.x *= m;
	    this.y *= m;
	    this.z *= m;
	    return this;
	}

	@Override
	public Vector multiply(double m) {
	    this.x *= m;
	    this.y *= m;
	    this.z *= m;
	    return this;
	}

	@Override
	public Vector multiply(float m) {
	    this.x *= m;
	    this.y *= m;
	    this.z *= m;
	    return this;
	}

	@Override
	public Vector crossProduct(@Nonnull org.bukkit.util.Vector o) {
	    double newX = this.y * o.getZ() - o.getY() * this.z;
	    double newY = this.z * o.getX() - o.getZ() * this.x;
	    double newZ = this.x * o.getY() - o.getX() * this.y;
	    
	    this.x = newX;
	    this.y = newY;
	    this.z = newZ;
	    return this;
	}

	@Override
	public Vector getCrossProduct(@Nonnull org.bukkit.util.Vector o) {
		double x = this.y * o.getZ() - o.getY() * this.z;
	    double y = this.z * o.getX() - o.getZ() * this.x;
	    double z = this.x * o.getY() - o.getX() * this.y;
	    return new Vector(x, y, z);
	}

	@Override
	public Vector normalize() {
	    double length = length();
	    
	    this.x /= length;
	    this.y /= length;
	    this.z /= length;
	    
	    return this;
	}
  
	@Override
	public Vector zero() {
	    this.x = 0.0D;
	    this.y = 0.0D;
	    this.z = 0.0D;
	    return this;
	}

	@Override
	public boolean isInSphere(@Nonnull org.bukkit.util.Vector origin, double radius) {
		return (FastMath.pow2(origin.getX() - this.x) +
				FastMath.pow2(origin.getY() - this.y) +
				FastMath.pow2(origin.getZ() - this.z) <= FastMath.pow2(radius));
	}

	public boolean isNormalized() { 
		return (FastMath.abs(lengthSquared() - 1.0D) < getEpsilon());
	}

	@Nonnull
	public Vector rotateAroundX(double angle) {
		// create double wrapper
		DoubleWrapper wrapper = new DoubleWrapper();
		
		// calculate sin and cos together. Return is sin and wrapper caches cos
	    double sin = FastMath.sinAndCos(angle, wrapper);
	    double cos = wrapper.value;
	    
	    double y = cos * getY() - sin * getZ();
	    double z = sin * getY() + cos * getZ();
	    return setY(y).setZ(z);
	}

	@Nonnull
	public Vector rotateAroundY(double angle) {
		// create double wrapper
		DoubleWrapper wrapper = new DoubleWrapper();
		
		// calculate sin and cos together. Return is sin and wrapper caches cos
	    double sin = FastMath.sinAndCos(angle, wrapper);
	    double cos = wrapper.value;
	    
	    double x = cos * getX() + sin * getZ();
	    double z = -sin * getX() + cos * getZ();
	    return setX(x).setZ(z);
	}

	@Nonnull
	public Vector rotateAroundZ(double angle) {
		// create double wrapper
		DoubleWrapper wrapper = new DoubleWrapper();
		
		// calculate sin and cos together. Return is sin and wrapper caches cos
	    double sin = FastMath.sinAndCos(angle, wrapper);
	    double cos = wrapper.value;
	    
	    double x = cos * getX() - sin * getY();
	    double y = sin * getX() + cos * getY();
	    return setX(x).setY(y);
	}
	  
	@Nonnull
	public Vector rotateAroundAxis(@Nonnull Vector axis, double angle) {
		  Preconditions.checkArgument((axis != null), "The provided axis vector was null");
	    
		  return rotateAroundNonUnitAxis(axis.isNormalized() ? axis : axis.clone().normalize(), angle);
	}

	@Nonnull
	public Vector rotateAroundNonUnitAxis(@Nonnull Vector axis, double angle) {
	    Preconditions.checkArgument((axis != null), "The provided axis vector was null");
	    
	    double x = getX();
	    double y = getY();
	    double z = getZ();
	    double x2 = axis.getX();
	    double y2 = axis.getY();
	    double z2 = axis.getZ();
	    
	    // create Double Wrapper
	    DoubleWrapper wrapper = new DoubleWrapper();
	    
	    double sinTheta = FastMath.sinAndCos(angle, wrapper);
	    double cosTheta = wrapper.value;
	    double dotProduct = dot(axis);
	    
	    double xPrime = x2 * dotProduct * (1.0D - cosTheta) + 
	      x * cosTheta + (
	      -z2 * y + y2 * z) * sinTheta;
	    double yPrime = y2 * dotProduct * (1.0D - cosTheta) + 
	      y * cosTheta + (
	      z2 * x - x2 * z) * sinTheta;
	    double zPrime = z2 * dotProduct * (1.0D - cosTheta) + 
	      z * cosTheta + (
	      -y2 * x + x2 * y) * sinTheta;
	    
	    return setX(xPrime).setY(yPrime).setZ(zPrime);
	}
	  
	@Override
	public Vector setX(int x) {
	    this.x = x;
	    return this;
	}

	@Override
	public Vector setX(double x) {
	    this.x = x;
	    return this;
	}
	  
	@Override
	public Vector setX(float x) {
	    this.x = x;
	    return this;
	}

	@Override
	public Vector setY(int y) {
	    this.y = y;
	    return this;
	}

	@Override
	public Vector setY(double y) {
	    this.y = y;
	    return this;
	}

	@Override
	public Vector setY(float y) {
	    this.y = y;
	    return this;
	}

	@Override
	public Vector setZ(int z) {
	    this.z = z;
	    return this;
	}

	@Override
	public Vector setZ(double z) {
	    this.z = z;
	    return this;
	}

	@Override
	public Vector setZ(float z) {
	    this.z = z;
	    return this;
	}

	@Override
	public boolean equals(Object obj) {
		// check if object is not null
		if(obj != null) {
			// check if is no instance of this class
			if(!obj.getClass().equals(this.getClass())) {
				// call super
				return super.equals(obj);
			} else {
			    Vector other = (Vector)obj;
			    
			    return (FastMath.abs(this.x - other.x) < EPSILON &&
			    		FastMath.abs(this.y - other.y) < EPSILON &&
			    		FastMath.abs(this.z - other.z) < EPSILON);
			}
		}
		// return false
		return false;
	}

	@Override
	public int hashCode() {
	    int hash = 7;
	    
	    hash = 79 * hash + (int)(Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
	    hash = 79 * hash + (int)(Double.doubleToLongBits(this.y) ^ Double.doubleToLongBits(this.y) >>> 32);
	    return 79 * hash + (int)(Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
	}
	  
	@Override
	public Vector clone() {
		return (Vector) super.clone();
	}
	
	@Nonnull
	public static Vector getMinimum(@Nonnull org.bukkit.util.Vector v1, @Nonnull org.bukkit.util.Vector v2) {
		return new Vector(FastMath.min(v1.getX(), v2.getX()), FastMath.min(v1.getY(), v2.getY()), FastMath.min(v1.getZ(), v2.getZ()));
	}

	@Nonnull
	public static Vector getMaximum(@Nonnull org.bukkit.util.Vector v1, @Nonnull org.bukkit.util.Vector v2) {
		return new Vector(FastMath.max(v1.getX(), v2.getX()), FastMath.max(v1.getY(), v2.getY()), FastMath.max(v1.getZ(), v2.getZ()));
	}

	@Nonnull
	public static Vector getRandom() {
		return new Vector(random.nextDouble(), random.nextDouble(), random.nextDouble());
	}
	  
	@Nonnull
	public static Vector deserialize(@Nonnull Map<String, Object> args) {    
	    return new Vector(org.bukkit.util.Vector.deserialize(args));
	}
}
