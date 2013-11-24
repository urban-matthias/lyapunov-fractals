package com.urban.app.fractal.ljapunow.util;

import java.util.ArrayList;
import java.util.List;

public class Complex
{
	public static final Complex	I		= new Complex(0.0D, 1.0D);

	public static final Complex	NaN		= new Complex((0.0D / 0.0D), (0.0D / 0.0D));

	public static final Complex	INF		= new Complex((1.0D / 0.0D), (1.0D / 0.0D));

	public static final Complex	ONE		= new Complex(1.0D, 0.0D);

	public static final Complex	ZERO	= new Complex(0.0D, 0.0D);

	private double				imaginary;
	private double				real;
	private transient boolean	isNaN;
	private transient boolean	isInfinite;

	public Complex()
	{
		assign(0, 0);
	}

	public Complex(Complex other)
	{
		assign(other);
	}

	public Complex(double real, double imaginary)
	{
		assign(real, imaginary);
	}

	public Complex assign(Complex other)
	{
		return assign(other.real, other.imaginary);
	}

	public Complex assign(double real, double imaginary)
	{
		this.real = real;
		this.imaginary = imaginary;
		return this;
	}

	public double abs()
	{
		if (Math.abs(this.real) < Math.abs(this.imaginary))
		{
			if (this.imaginary == 0.0D)
			{
				return Math.abs(this.real);
			}
			double q = this.real / this.imaginary;
			return Math.abs(this.imaginary) * Math.sqrt(1.0D + q * q);
		}
		if (this.real == 0.0D)
		{
			return Math.abs(this.imaginary);
		}
		double q = this.imaginary / this.real;
		return Math.abs(this.real) * Math.sqrt(1.0D + q * q);
	}

	public Complex add(Complex rhs)
	{
		return createComplex(this.real + rhs.getReal(), this.imaginary + rhs.getImaginary());
	}

	public Complex conjugate()
	{
		return createComplex(this.real, -this.imaginary);
	}

	public Complex divide(Complex rhs)
	{
		double c = rhs.getReal();
		double d = rhs.getImaginary();
		if (Math.abs(c) < Math.abs(d))
		{
			double q = c / d;
			double denominator = c * q + d;
			return createComplex((this.real * q + this.imaginary) / denominator, (this.imaginary * q - this.real) / denominator);
		}
		double q = d / c;
		double denominator = d * q + c;
		return createComplex((this.imaginary * q + this.real) / denominator, (this.imaginary - this.real * q) / denominator);
	}

	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if ((other instanceof Complex))
		{
			Complex rhs = (Complex) other;
			return (this.real == rhs.real) && (this.imaginary == rhs.imaginary);
		}
		return false;
	}

	public int hashCode()
	{
		return 37 * (17 * hash(this.imaginary) + hash(this.real));
	}

	private static int hash(double value)
	{
		return new Double(value).hashCode();
	}

	public double getImaginary()
	{
		return this.imaginary;
	}

	public double getReal()
	{
		return this.real;
	}

	public boolean isNaN()
	{
		return this.isNaN;
	}

	public boolean isInfinite()
	{
		return this.isInfinite;
	}

	public Complex multiply(Complex rhs)
	{
		return createComplex(this.real * rhs.real - this.imaginary * rhs.imaginary, this.real * rhs.imaginary + this.imaginary * rhs.real);
	}

	public Complex multiply(double rhs)
	{
		return createComplex(this.real * rhs, this.imaginary * rhs);
	}

	public Complex negate()
	{
		return createComplex(-this.real, -this.imaginary);
	}

	public Complex subtract(Complex rhs)
	{
		return createComplex(this.real - rhs.getReal(), this.imaginary - rhs.getImaginary());
	}

	public Complex acos()
	{
		Complex tmp = new Complex();
		return add(tmp.assign(this).sqrt1z().multiply(I)).log().multiply(tmp.assign(I).negate());
	}

	public Complex asin()
	{
		Complex tmp = new Complex();
		return sqrt1z().add(tmp.assign(this).multiply(I)).log().multiply(tmp.assign(I).negate());
	}

	public Complex atan()
	{
		Complex tmp = new Complex();
		return add(I).divide(tmp.assign(I).subtract(this)).log().multiply(tmp.assign(I).divide(new Complex(2.0D, 0.0D)));
	}

	public Complex cos()
	{
		return createComplex(Math.cos(this.real) * Math.cosh(this.imaginary), -Math.sin(this.real) * Math.sinh(this.imaginary));
	}

	public Complex cosh()
	{
		return createComplex(Math.cosh(this.real) * Math.cos(this.imaginary), Math.sinh(this.real) * Math.sin(this.imaginary));
	}

	public Complex exp()
	{
		double expReal = Math.exp(this.real);
		return createComplex(expReal * Math.cos(this.imaginary), expReal * Math.sin(this.imaginary));
	}

	public Complex log()
	{
		return createComplex(Math.log(abs()), Math.atan2(this.imaginary, this.real));
	}

	public Complex pow(Complex x)
	{
		return log().multiply(x).exp();
	}

	public Complex pow(double x)
	{
		return log().multiply(x).exp();
	}

	public Complex sin()
	{
		return createComplex(Math.sin(this.real) * Math.cosh(this.imaginary), Math.cos(this.real) * Math.sinh(this.imaginary));
	}

	public Complex sinh()
	{
		return createComplex(Math.sinh(this.real) * Math.cos(this.imaginary), Math.cosh(this.real) * Math.sin(this.imaginary));
	}

	public Complex sqrt()
	{
		if ((this.real == 0.0D) && (this.imaginary == 0.0D))
		{
			return createComplex(0.0D, 0.0D);
		}
		double t = Math.sqrt((Math.abs(this.real) + abs()) / 2.0D);
		if (this.real >= 0.0D)
		{
			return createComplex(t, this.imaginary / (2.0D * t));
		}
		return createComplex(Math.abs(this.imaginary) / (2.0D * t), indicator(this.imaginary) * t);
	}

	private static double indicator(double x)
	{
		return x >= 0.0D ? 1.0D : -1.0D;
	}

	public Complex sqrt1z()
	{
		Complex zz = new Complex(multiply(this));
		return assign(1.0D, 0.0D).subtract(zz).sqrt();
	}

	public Complex tan()
	{
		double real2 = 2.0D * this.real;
		double imaginary2 = 2.0D * this.imaginary;
		double d = Math.cos(real2) + Math.cosh(imaginary2);
		return createComplex(Math.sin(real2) / d, Math.sinh(imaginary2) / d);
	}

	public Complex tanh()
	{
		double real2 = 2.0D * this.real;
		double imaginary2 = 2.0D * this.imaginary;
		double d = Math.cosh(real2) + Math.cos(imaginary2);
		return createComplex(Math.sinh(real2) / d, Math.sin(imaginary2) / d);
	}

	public double getArgument()
	{
		return Math.atan2(getImaginary(), getReal());
	}

	public List<Complex> nthRoot(int n) throws IllegalArgumentException
	{
		List<Complex> result = new ArrayList<Complex>();
		double nthRootOfAbs = Math.pow(abs(), 1.0D / n);
		double nthPhi = getArgument() / n;
		double slice = 6.283185307179586D / n;
		double innerPart = nthPhi;
		for (int k = 0; k < n; k++)
		{
			double realPart = nthRootOfAbs * Math.cos(innerPart);
			double imaginaryPart = nthRootOfAbs * Math.sin(innerPart);
			result.add(new Complex(realPart, imaginaryPart));
			innerPart += slice;
		}
		return result;
	}

	protected Complex createComplex(double realPart, double imaginaryPart)
	{
		assign(realPart, imaginaryPart);
		return this;
	}
}
