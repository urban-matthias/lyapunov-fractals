#include <math.h>
#include <jni.h>
#include <stdlib.h>

JNIEXPORT jdouble JNICALL Java_com_urban_app_fractal_ljapunow_FractalGenerator_logistic(JNIEnv* env, jclass in_class, jdouble a, jdouble b, jdouble x0, jint iterations, jint warmup, jcharArray sequenceArray, jint sequence_length)
{
	jdouble xn = x0, rn = 0, sum = 0, fraction = 1, tmp, logv;
	jint n, s;

	// prepare AB sequence
	jchar *sequenceString = (*env)->GetCharArrayElements(env, sequenceArray, NULL);
	jdouble* sequence = (jdouble*) malloc(sizeof(jdouble) * sequence_length);
	for (s = 0; s < sequence_length; ++s)
	{
		if (sequenceString[s] == 'A')
		{
			sequence[s] = a;
		}
		else if (sequenceString[s] == 'B')
		{
			sequence[s] = b;
		}
		else if (s > 0)
		{
			sequence[s] = -sequence[s - 1];
		}
		else
		{
			sequence[s] = -a;
		}
	}

	// warm up, calculate initial xn
	for (n = 0, s = 0; n < warmup; ++n, ++s)
	{
		if (s == sequence_length)
		{
			s = 0;
		}
		xn = sequence[s] * xn * (1 - xn);
	}

	// calculate sum
	for (n = 0, s = 0; n < iterations; ++n, ++s)
	{
		if (s == sequence_length)
		{
			s = 0;
		}
		rn = sequence[s];
		logv = rn - (2 * rn * xn);
		if (logv < 0)
		{
			logv = -logv;
		}
		tmp = fraction * logv;
		if (tmp == 0)
		{
			sum += log(fraction);
			fraction = 1;
			tmp = logv;
		}
		fraction = tmp;
		xn *= rn * (1 - xn);
	}
	sum += log(fraction);

	(*env)->ReleaseCharArrayElements(env, sequenceArray, sequenceString, 0);
	free(sequence);

	// calculate exponent
	return sum / iterations;
}

JNIEXPORT jdouble JNICALL Java_com_urban_app_fractal_ljapunow_FractalGenerator_newton3rdGrade(JNIEnv* env, jclass in_class, jdouble a, jdouble b, jdouble x0, jint iterations, jint warmup, jcharArray sequenceArray, jint sequence_length)
{
	jdouble sum = 0, f, aOld = x0, bOld = x0, distance, rn = 0, aa, bb;
	jint n, s;

	jchar *sequence = (*env)->GetCharArrayElements(env, sequenceArray, NULL);

	// calculate sum
	for (n = 0, s = 0; n < iterations; n++, s++)
	{
		if (s == sequence_length)
		{
			s = 0;
		}
		if (sequence[s] == 'B')
		{
			rn = a;
			a = b;
			b = rn;
		}
		else if (sequence[s] == 'C')
		{
			a = -a;
			b = -b;
		}

		aa = a * a;
		bb = b * b;
		f = 3 * ((aa - bb) * (aa - bb) + 4 * aa * bb);
		if (f == 0)
		{
			f = .0000001;
		}
		a = .6666667 * a + (aa - bb) / f;
		b = .6666667 * b - 2 * a * b / (f * 5);

		distance = sqrt(((a - aOld) * (a - aOld)) + ((b - bOld) * (b - bOld)));
		if (distance == 0)
		{
			break;
		}
		sum += distance;
		aOld = a;
		bOld = b;
	}

	(*env)->ReleaseCharArrayElements(env, sequenceArray, sequence, 0);

	// calculate exponent
	return log(sum);
}

#define I (__extension__ 1.0iF)

JNIEXPORT jdouble JNICALL Java_com_urban_app_fractal_ljapunow_FractalGenerator_newton(JNIEnv* env, jclass in_class, jdouble a, jdouble b, jdouble x0, jint iterations, jint warmup, jcharArray sequenceArray, jint sequence_length)
{
	jdouble sum = x0, distance = 0, re, im, tmp;
	jint i, j;

	// prepare AB sequence
	jchar *sequenceString = (*env)->GetCharArrayElements(env, sequenceArray, NULL);
	jdouble* sequence = (jdouble*) malloc(sizeof(jdouble) * sequence_length);
	for (j = 0; j < sequence_length; ++j)
	{
		if (sequenceString[j] == 'A')
		{
			sequence[j] = 1;
		}
		else if (sequenceString[j] == 'B')
		{
			sequence[j] = -1;
		}
		else if (j > 0)
		{
			sequence[j] = sequence[j - 1];
		}
		else
		{
			sequence[j] = 1;
		}
	}

	// warmup
	_Complex double z = a + I * b;
	_Complex double zn = z;

	_Complex double f = sequence[sequence_length - 1] + I * 0;
	_Complex double d = f * (sequence_length - 1), ctmp;
	for (j = sequence_length - 2; j > 0; --j)
	{
		f = (f * z) + sequence[j];
		d = (d * z) + (sequence[j] * j);
		if (sequenceString[j + 1] == 'C')
		{
			ctmp = f;
			f = d;
			d = ctmp;
		}
	}
	f = (f * z) + sequence[0];
	zn -= f / d;
	z = zn;

	// calculate sum
	for (i = 0; i < iterations; i++)
	{
		f = sequence[sequence_length - 1] + I * 0;
		d = f * (sequence_length - 1);
		for (j = sequence_length - 2; j > 0; --j)
		{
			f = (f * z) + sequence[j];
			d = (d * z) + (sequence[j] * j);
			if (sequenceString[j + 1] == 'C')
			{
				ctmp = f;
				f = d;
				d = ctmp;
			}
		}
		f = (f * z) + sequence[0];
		zn -= f / d;
		tmp = zn - z;

		re = __real__ tmp;
		im = __imag__ tmp;
		distance = (re * re) + (im * im);
		if (distance == 0)
			break;

		sum += distance;
		z = zn;
	}

	(*env)->ReleaseCharArrayElements(env, sequenceArray, sequenceString, 0);
	free(sequence);

	// calculate exponent
	return log(fabs(sum * __builtin_carg(z)));
}
