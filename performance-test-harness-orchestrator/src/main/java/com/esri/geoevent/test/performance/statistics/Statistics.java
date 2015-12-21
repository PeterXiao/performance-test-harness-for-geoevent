/*
 Copyright 1995-2015 Esri

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 For additional information, contact:
 Environmental Systems Research Institute, Inc.
 Attn: Contracts Dept
 380 New York Street
 Redlands, California, USA 92373

 email: contracts@esri.com
 */
package com.esri.geoevent.test.performance.statistics;

import java.util.Arrays;

import com.esri.geoevent.test.performance.TestException;

public class Statistics {

    long[] values;
    int size;

    public Statistics(long[] values) throws TestException {
        if (values == null) {
            throw new TestException("Statistics cannot be calculated for null value.");
        }
        if (values.length == 0) {
            throw new TestException("Statistics cannot be calculated for empty array.");
        }
        this.values = values;
        this.size = values.length;
        Arrays.sort(this.values);
    }

    public long getMinimum() {
        return values[0];
    }

    public long getMaximum() {
        return values[size - 1];
    }

    public double getMean() {
        double sum = 0.0;
        for (long value : values) {
            sum += value;
        }
        return sum / size;
    }

    public long getMedian() {
        int middle = values.length / 2;
        if (values.length % 2 == 0) {
            return (long) ((values[middle - 1] + values[middle]) / 2.0);
        } else {
            return values[middle];
        }
    }

    double getVariance() {
        double mean = getMean();
        double temp = 0;
        for (long value : values) {
            temp += (mean - value) * (mean - value);
        }
        return temp / size;
    }

    public double getStdDev() {
        return Math.sqrt(getVariance());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMinimum()).append(",").append(getMaximum()).append(",").append(getMean()).append(",").append(getMedian()).append(",").append(getStdDev());
        return sb.toString();
    }
}
