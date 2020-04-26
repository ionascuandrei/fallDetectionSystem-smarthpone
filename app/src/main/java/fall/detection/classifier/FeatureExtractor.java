package fall.detection.classifier;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Calculates for the given input data following features:
 *     - Maximum amplitude
 *     - Minimum amplitude
 *     - Mean ampltitude
 *     - Variance
 *     - Kurtosis
 *     - Skewness
 */
public class FeatureExtractor {
    private static int featuresNumber = 6;
    private static float maximumAmplitude;
    private static float minimumAmplitude;
    private static float meanAmplitude;
    private static float variance;
    private static float kurtosis;
    private static float skewness;

    /**
     * Extracts maximum value of the given features.
     * @param samples Array which contains samples of the same feature
     */
    private static void extractMaximumAmplitude(ArrayList<Float> samples) {
        maximumAmplitude = Collections.max(samples);
    }

    /**
     * Extracts minimum value of the given features.
     * @param samples Array which contains samples of the same feature
     */
    private static void extractMinimumAmplitude(ArrayList<Float> samples) {
        minimumAmplitude = Collections.min(samples);
    }

    /**
     * Calculates mean value of the given features.
     * @param samples Array which contains samples of the same feature
     */
    private static void extractMeanAmplitude(ArrayList<Float> samples) {
        float sum = 0;
        for(float value : samples) {
            sum += value;
        }
        meanAmplitude = sum / samples.size();
    }

    /**
     * Extracts variance of the given features.
     * <<< !!! Must be called after extractMeanAmplitude !!! >>>
     * @param samples Array which contains samples of the same feature
     */
    private static void extractVariance(ArrayList<Float> samples) {
        float sum = 0;
        for (float current : samples) {
            float temp = (current - meanAmplitude);
            sum += (float) Math.pow(temp, 2);
        }
        variance = sum / samples.size();
    }

    /**
     * Calculates kurtosis of the given features.
     * @param samples Array which contains samples of the same feature
     */
    private static void extractKurtosis(ArrayList<Float> samples) {
        float sum = 0;
        for (float current : samples) {
            float temp = (current - meanAmplitude);
            sum += (float) Math.pow(temp, 4);
        }
        sum /= samples.size();

        float standard_deviation = (float) Math.sqrt(variance);
        standard_deviation = (float) Math.pow(standard_deviation, 4);

        kurtosis = sum / standard_deviation;

    }

    /**
     * Calculates skewness of the given features.
     * @param samples Array which contains samples of the same feature
     */
    private static void extractSkewness(ArrayList<Float> samples) {
        float sum = 0;
        for (float current : samples) {
            float temp = (current - meanAmplitude);
            sum += (float) Math.pow(temp, 3);
        }
        sum /= samples.size();

        float standard_deviation = (float) Math.sqrt(variance);
        standard_deviation = (float) Math.pow(standard_deviation, 3);

        skewness = sum / standard_deviation;
    }


    /**
     * Calculates all the features for the given sample array.
     * @param samples Array which contains samples of the same feature
     */
    public static void extractFeatures(ArrayList<Float> samples) {
        extractMaximumAmplitude(samples);
        extractMinimumAmplitude(samples);
        extractMeanAmplitude(samples);
        extractVariance(samples);
        extractKurtosis(samples);
        extractSkewness(samples);
    }


    /**
     * Returns an array of all the features calculated for the given samples.
     * @return ArrayList<Float> of processed features.
     */
    public static ArrayList<Float> getFeatures() {
        ArrayList<Float> features = new ArrayList<>();
        features.add(getMaximumAmplitude());
        features.add(getMinimumAmplitude());
        features.add(getMeanAmplitude());
        features.add(getVariance());
        features.add(getKurtosis());
        features.add(getSkewness());
        return features;
    }

    public static float getMaximumAmplitude() {
        return maximumAmplitude;
    }

    public static float getMinimumAmplitude() {
        return minimumAmplitude;
    }

    public static float getMeanAmplitude() {
        return meanAmplitude;
    }

    public static float getVariance() {
        return variance;
    }

    public static float getKurtosis() {
        return kurtosis;
    }

    public static float getSkewness() {
        return skewness;
    }

    public static int getFeaturesNumber() { return featuresNumber; }
}
