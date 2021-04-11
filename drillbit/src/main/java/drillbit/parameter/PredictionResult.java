/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package drillbit.parameter;

public class PredictionResult {

    private final String predictedLabel;
    private final double predictedScore;

    private double squaredNorm;
    private double variance;

    public PredictionResult(double predictedScore) {
        this(null, predictedScore);
    }

    public PredictionResult(String predictedLabel, double predictedScore) {
        this.predictedLabel = predictedLabel;
        this.predictedScore = predictedScore;
    }

    public PredictionResult squaredNorm(double sqnorm) {
        this.squaredNorm = sqnorm;
        return this;
    }

    public PredictionResult variance(double var) {
        this.variance = var;
        return this;
    }

    public String getLabel() {
        return predictedLabel;
    }

    public double getScore() {
        return predictedScore;
    }

    public double getSquaredNorm() {
        return squaredNorm;
    }

    public double getVariance() {
        return variance;
    }

}
