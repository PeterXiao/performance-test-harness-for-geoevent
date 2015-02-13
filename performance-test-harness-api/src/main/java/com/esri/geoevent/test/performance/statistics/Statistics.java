package com.esri.geoevent.test.performance.statistics;

import java.util.Arrays;

import com.esri.geoevent.test.performance.TestException;

public class Statistics
{
	long[] values;
	int size;

	public Statistics(long[] values) throws TestException
	{
		if (values == null)
			throw new TestException("Statistics cannot be calculated for null value.");
		if (values.length == 0)
			throw new TestException("Statistics cannot be calculated for empty array.");
		this.values = values;
		this.size = values.length;
		Arrays.sort(this.values);
	}

	public long getMinimum()
	{
		return values[0];
	}

	public long getMaximum()
	{
		return values[size-1];
	}

	public double getMean()
	{
		double sum = 0.0;
		for(long value : values)
			sum += value;
		return sum/size;
	}

	public long getMedian()
	{
		int middle = values.length / 2;
		if (values.length % 2 == 0)
			return (long) ((values[middle-1] + values[middle]) / 2.0);
		else
			return values[middle];
	}

	double getVariance()
	{
		double mean = getMean();
		double temp = 0;
		for(long value : values)
			temp += (mean-value)*(mean-value);
		return temp/size;
	}

	public double getStdDev()
	{
		return Math.sqrt(getVariance());
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getMinimum()).append(",").append(getMaximum()).append(",").append(getMean()).append(",").append(getMedian()).append(",").append(getStdDev());
		return sb.toString();
	}
}