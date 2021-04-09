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

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Model {

    enum ModelType {
        Dense, Sparse
    }

    Weights.WeightBuilder getWeightBuilder();

    int size();

    boolean contains(@Nonnull Object feature);

    void delete(@Nonnull Object feature);

    @Nullable
    <T extends Weights.ExtendedWeight> T get(@Nonnull Object feature);

    <T extends Weights.ExtendedWeight> void set(@Nonnull Object feature, @Nonnull T value);

    @Nullable
    byte[] toByteArray();

    Model fromByteArray(byte[] byteArray) throws InvalidProtocolBufferException;

    double getWeight(@Nonnull Object feature);

    void setWeight(@Nonnull Object feature, @Nonnull double value);

    <V extends Weights.ExtendedWeight> ConcurrentHashMap<Object, V> weightMap();

    <T extends Weights.ExtendedWeight> ArrayList<T> weightList();

    Model toAnotherModelWithDifferentWeight(Weights.WeightBuilder builder);
}
