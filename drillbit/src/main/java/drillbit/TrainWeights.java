package drillbit;


public final class TrainWeights {

    public enum WeightType {
        Single, WithCovar, WithMAndV, WithDelta,
        WithSumOfSquaredGradients, WithSumOfGradientsAndSumOfSquaredGradientsAndDelta,
        WithSumOfSquaredGradientsAndSumOfSquaredDeltaX, WithSumOfSquaredGradientsAndSumOfGradients
    }

    public static WeightBuilder getWeightBuilder(WeightType weightType) {
        switch (weightType) {
            case WithCovar:
                return WeightWithCovar.newBuilder();
            case WithMAndV:
                return WeightWithMAndV.newBuilder();
            case WithDelta:
                return WeightWithDelta.newBuilder();
            case WithSumOfSquaredGradients:
                return WeightWithSumOfSquaredGradients.newBuilder();
            case WithSumOfGradientsAndSumOfSquaredGradientsAndDelta:
                return WeightWithSumOfGradientsAndSumOfSquaredGradientsAndDelta.newBuilder();
            case WithSumOfSquaredGradientsAndSumOfSquaredDeltaX:
                return WeightWithSumOfSquaredGradientsAndSumOfSquaredDeltaX.newBuilder();
            case WithSumOfSquaredGradientsAndSumOfGradients:
                return WeightWithSumOfSquaredGradientsAndSumOfGradients.newBuilder();
            default:
                return SingleWeight.newBuilder();
        }
    }

    public abstract static class Weight{
        double weight;
        boolean touched;

        public boolean isTouched() {
            return touched;
        }

        public void setTouched(boolean touched) {
            this.touched = touched;
        }

        public double get() {
            return this.weight;
        }

        public void set(double weight) {
            this.weight = weight;
        }

        static WeightBuilder newBuilder() {
            return null;
        }
    }

    public interface WeightBuilder {

        ExtendedWeight buildFromWeight(double weight);

        ExtendedWeight buildFromWeightAndParams(double weight, double... params);

        WeightType getWeightType();
    }

    /*
     * To add a new weight, extends ExtendedWeight and put getters for new weight params in it.
     * Getters must throw UnsupportedOperationException in ExtendedWeight.
     *
     * Default weight for a null ExtendedWeight is 0.d.
     * If a ExtendedWeight contains a 0.d value, it is considered null or invalid.
     */
    public static class ExtendedWeight extends Weight {
        int paramNum = 0;

        double[] params;

        ExtendedWeight() {
            weight = 0.d;
            paramNum = 0;
            allocParams(paramNum);
        }

        ExtendedWeight(double weight, int paramNum) {
            this.weight = weight;
            this.paramNum = paramNum;
            allocParams(paramNum);
        }

        protected ExtendedWeight(int paramNum) {
            this.weight = 0.d;
            this.paramNum = paramNum;
            allocParams(paramNum);
        }

        double getParam(int index) {
            if (index == 0 || index > paramNum) {
                throw new IllegalArgumentException();
            }
            return params[index - 1];
        }

        final void setParam(int index, double weight) {
            if (index == 0 || index > paramNum) {
                throw new IllegalArgumentException();
            }
            params[index - 1] = weight;
        }

        public final void clear() {
            for (int i = 0; i < paramNum; i++) {
                params[i] = 0.d;
            }
        }

        protected void allocParams(int paramNum) {
            params = new double[paramNum];
        }

        public static WeightBuilder newBuilder() {
            return null;
        }

        public double getCovar() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public void setCovar(double covar) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public void setM(double M) {
            throw new UnsupportedOperationException();
        }

        public double getM() {
            throw new UnsupportedOperationException();
        }

        public void setV(double V) {
            throw new UnsupportedOperationException();
        }

        public double getV() {
            throw new UnsupportedOperationException();
        }

        public void setDelta(double delta) {
            throw new UnsupportedOperationException();
        }

        public double getDelta() {
            throw new UnsupportedOperationException();
        }

        public void setSumOfSquaredGradients(double sumOfSquaredGradients) {
            throw new UnsupportedOperationException();
        }

        public double getSumOfSquaredGradients() {
            throw new UnsupportedOperationException();
        }

        public void setSumOfGradients(double sumOfGradients) {
            throw new UnsupportedOperationException();
        }

        public double getSumOfGradients() {
            throw new UnsupportedOperationException();
        }

        public void setSumOfSquaredDeltaX(double sumOfSquaredDeltaX) {
            throw new UnsupportedOperationException();
        }

        public double getSumOfSquaredDeltaX() {
            throw new UnsupportedOperationException();
        }
    }

    public static class SingleWeight extends ExtendedWeight {
        public SingleWeight() {
            super(0.d, 0);
        }

        public SingleWeight(double weight) {
            super(weight, 0);
        }

        public static WeightBuilder newBuilder() {
            return new Builder();
        }

        public static class Builder implements WeightBuilder {

            Builder() { }

            @Override
            public SingleWeight buildFromWeight(double weight) {
                return new SingleWeight(weight);
            }

            @Override
            public SingleWeight buildFromWeightAndParams(double weight, double... params) {
                return new SingleWeight(weight);
            }

            @Override
            public WeightType getWeightType() {
                return WeightType.Single;
            }
        }
    }

    public static class WeightWithCovar extends ExtendedWeight {
        public WeightWithCovar() {
            super(0.d, 1);
            setCovar(1.d);
        }

        WeightWithCovar(double weight) {
            super(weight, 1);
            setCovar(1.d);
        }

        public WeightWithCovar(double weight, double covar) {
            super(weight, 1);
            setCovar(covar);
        }

        @Override
        public double getCovar() {
            return getParam(1);
        }

        @Override
        public void setCovar(double covar) {
            setParam(1, covar);
        }

        public static WeightBuilder newBuilder() {
            return new Builder();
        }

        public static class Builder implements WeightBuilder {

            Builder() { }

            @Override
            public WeightWithCovar buildFromWeight(double weight) {
                return new WeightWithCovar(weight);
            }

            @Override
            public WeightWithCovar buildFromWeightAndParams(double weight, double... params) {
                return new WeightWithCovar(weight, params[1]);
            }

            @Override
            public WeightType getWeightType() {
                return WeightType.WithCovar;
            }
        }
    }

    public static class WeightWithMAndV extends ExtendedWeight {
        public WeightWithMAndV(double weight) {
            super(weight, 2);
            setM(0.d);
            setV(0.d);
        }

        public WeightWithMAndV(double weight, double M, double V) {
            super(weight, 2);
            setM(M);
            setV(V);
        }

        @Override
        public void setM(double M) {
            setParam(1, M);
        }

        @Override
        public double getM() {
            return getParam(1);
        }

        @Override
        public void setV(double V) {
            setParam(2, V);
        }

        @Override
        public double getV() {
            return getParam(2);
        }

        public static WeightBuilder newBuilder() {
            return new Builder();
        }

        public static class Builder implements WeightBuilder {

            @Override
            public ExtendedWeight buildFromWeight(double weight) {
                return new WeightWithMAndV(weight);
            }

            @Override
            public ExtendedWeight buildFromWeightAndParams(double weight, double... params) {
                assert params.length >= 2;
                return new WeightWithMAndV(weight, params[0], params[1]);
            }

            @Override
            public WeightType getWeightType() {
                return WeightType.WithMAndV;
            }
        }
    }

    public static class WeightWithDelta extends ExtendedWeight {
        public WeightWithDelta(double weight) {
            super(weight, 1);
            setDelta(0.d);
        }

        public WeightWithDelta(double weight, double delta) {
            super(weight, 1);
            setDelta(delta);
        }

        @Override
        public void setDelta(double delta) {
            setParam(1, delta);
        }

        @Override
        public double getDelta() {
            return getParam(1);
        }

        public static WeightBuilder newBuilder() {
            return new Builder();
        }

        public static class Builder implements WeightBuilder {

            @Override
            public ExtendedWeight buildFromWeight(double weight) {
                return new WeightWithDelta(weight);
            }

            @Override
            public ExtendedWeight buildFromWeightAndParams(double weight, double... params) {
                assert params.length >= 1;
                return new WeightWithDelta(weight, params[0]);
            }

            @Override
            public WeightType getWeightType() {
                return WeightType.WithDelta;
            }
        }
    }

    public static class WeightWithSumOfSquaredGradients extends ExtendedWeight {
        public WeightWithSumOfSquaredGradients(double weight) {
            super(weight, 1);
            setSumOfSquaredGradients(0.d);
        }

        public WeightWithSumOfSquaredGradients(double weight, double sumOfSquaredGradients) {
            super(weight, 1);
            setSumOfSquaredGradients(sumOfSquaredGradients);
        }

        @Override
        public void setSumOfSquaredGradients(double sumOfSquaredGradients) {
            setParam(1, sumOfSquaredGradients);
        }

        @Override
        public double getSumOfSquaredGradients() {
            return getParam(1);
        }

        public static WeightBuilder newBuilder() {
            return new Builder();
        }

        public static class Builder implements WeightBuilder {

            @Override
            public ExtendedWeight buildFromWeight(double weight) {
                return new WeightWithSumOfSquaredGradients(weight);
            }

            @Override
            public ExtendedWeight buildFromWeightAndParams(double weight, double... params) {
                assert params.length >= 1;
                return new WeightWithSumOfSquaredGradients(weight, params[0]);
            }

            @Override
            public WeightType getWeightType() {
                return WeightType.WithSumOfSquaredGradients;
            }
        }
    }

    public static class WeightWithSumOfGradientsAndSumOfSquaredGradientsAndDelta extends ExtendedWeight {
        public WeightWithSumOfGradientsAndSumOfSquaredGradientsAndDelta(double weight) {
            super(weight, 3);
            setSumOfGradients(0.d);
            setSumOfSquaredGradients(0.d);
            setDelta(0.d);
        }

        public WeightWithSumOfGradientsAndSumOfSquaredGradientsAndDelta(double weight, double sumOfGradients, double sumOfSquaredGradients, double delta) {
            super(weight, 3);
            setSumOfGradients(sumOfGradients);
            setSumOfSquaredGradients(sumOfSquaredGradients);
            setDelta(delta);
        }

        public void setSumOfGradients(double sumOfGradients) {
            setParam(1, sumOfGradients);
        }

        public double getSumOfGradients() {
            return getParam(1);
        }

        @Override
        public void setSumOfSquaredGradients(double sumOfSquaredGradients) {
            setParam(2, sumOfSquaredGradients);
        }

        @Override
        public double getSumOfSquaredGradients() {
            return getParam(2);
        }

        @Override
        public void setDelta(double delta) {
            setParam(3, delta);
        }

        @Override
        public double getDelta() {
            return getParam(3);
        }

        public static WeightBuilder newBuilder() {
            return new Builder();
        }

        public static class Builder implements WeightBuilder {

            @Override
            public ExtendedWeight buildFromWeight(double weight) {
                return new WeightWithSumOfGradientsAndSumOfSquaredGradientsAndDelta(weight);
            }

            @Override
            public ExtendedWeight buildFromWeightAndParams(double weight, double... params) {
                assert params.length >= 3;
                return new WeightWithSumOfGradientsAndSumOfSquaredGradientsAndDelta(weight, params[0], params[1], params[2]);
            }

            @Override
            public WeightType getWeightType() {
                return WeightType.WithSumOfGradientsAndSumOfSquaredGradientsAndDelta;
            }
        }
    }

    public static class WeightWithSumOfSquaredGradientsAndSumOfSquaredDeltaX extends ExtendedWeight {
        public WeightWithSumOfSquaredGradientsAndSumOfSquaredDeltaX(double weight) {
            super(weight, 2);
            setSumOfSquaredGradients(0.d);
            setSumOfSquaredDeltaX(0.d);
        }

        public WeightWithSumOfSquaredGradientsAndSumOfSquaredDeltaX(double weight, double sumOfSquaredGradients, double sumOfSquaredDeltaX) {
            super(weight, 2);
            setSumOfSquaredGradients(sumOfSquaredGradients);
            setSumOfSquaredDeltaX(sumOfSquaredDeltaX);
        }

        @Override
        public void setSumOfSquaredGradients(double sumOfSquaredGradients) {
            setParam(1, sumOfSquaredGradients);
        }

        @Override
        public double getSumOfSquaredGradients() {
            return getParam(1);
        }

        @Override
        public void setSumOfSquaredDeltaX(double sumOfSquaredDeltaX) {
            setParam(2, sumOfSquaredDeltaX);
        }

        @Override
        public double getSumOfSquaredDeltaX() {
            return getParam(2);
        }

        public static WeightBuilder newBuilder() {
            return new Builder();
        }

        public static class Builder implements WeightBuilder {

            @Override
            public ExtendedWeight buildFromWeight(double weight) {
                return new WeightWithSumOfGradientsAndSumOfSquaredGradientsAndDelta(weight);
            }

            @Override
            public ExtendedWeight buildFromWeightAndParams(double weight, double... params) {
                assert params.length >= 2;
                return new WeightWithSumOfSquaredGradientsAndSumOfSquaredDeltaX(weight, params[0], params[1]);
            }

            @Override
            public WeightType getWeightType() {
                return WeightType.WithSumOfGradientsAndSumOfSquaredGradientsAndDelta;
            }
        }
    }

    public static class WeightWithSumOfSquaredGradientsAndSumOfGradients extends ExtendedWeight {
        public WeightWithSumOfSquaredGradientsAndSumOfGradients(double weight) {
            super(weight, 2);
            setSumOfSquaredGradients(0.d);
            setSumOfGradients(0.d);
        }

        public WeightWithSumOfSquaredGradientsAndSumOfGradients(double weight, double sumOfSquaredGradients, double sumOfGradients) {
            super(weight, 2);
            setSumOfSquaredGradients(sumOfSquaredGradients);
            setSumOfGradients(sumOfGradients);
        }

        @Override
        public void setSumOfSquaredGradients(double sumOfSquaredGradients) {
            setParam(1, sumOfSquaredGradients);
        }

        @Override
        public double getSumOfSquaredGradients() {
            return getParam(1);
        }

        @Override
        public void setSumOfGradients(double sumOfGradients) {
            setParam(2, sumOfGradients);
        }

        @Override
        public double getSumOfGradients() {
            return getParam(2);
        }

        public static WeightBuilder newBuilder() {
            return new Builder();
        }

        public static class Builder implements WeightBuilder {

            @Override
            public ExtendedWeight buildFromWeight(double weight) {
                return new WeightWithSumOfSquaredGradientsAndSumOfGradients(weight);
            }

            @Override
            public ExtendedWeight buildFromWeightAndParams(double weight, double... params) {
                assert params.length >= 2;
                return new WeightWithSumOfSquaredGradientsAndSumOfGradients(weight, params[0], params[1]);
            }

            @Override
            public WeightType getWeightType() {
                return WeightType.WithSumOfSquaredGradientsAndSumOfGradients;
            }
        }
    }
}
