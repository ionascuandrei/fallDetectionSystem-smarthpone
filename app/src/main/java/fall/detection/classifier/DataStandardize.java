package fall.detection.classifier;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Standardizes given input file using Mean and Deviation.
 * It must respect the classification format:
 *  < class_label 1:maximum_amplitude ..... 18:Skewness >
 *
 */
public class DataStandardize {
    private static ArrayList<Float> sampleFeatures = new ArrayList<>();
    private static ArrayList<Float> meanValue = new ArrayList<>();
    private static ArrayList<Float> standardDeviation = new ArrayList<>();
    private static String parsedFeatures;
    private static String samplesClass = "";

    /**
     * Calculates deviation for each feature.
     */
    public static void calculateDeviation() {
        for (int feature = 0; feature < FeatureExtractor.getFeaturesNumber() * 3; feature++) {
            float variance = sampleFeatures.get(feature) - meanValue.get(feature);
            variance = (float) Math.pow(variance, 2);
            standardDeviation.set(feature, standardDeviation.get(feature) + variance);
            float stDev = standardDeviation.get(feature) / (float) (sampleFeatures.size() - 1);
            stDev = (float) Math.sqrt(stDev);
            standardDeviation.set(feature, stDev);
        }
    }

    /**
     * Calculates mean for each feature.
     */
    public static void calculateMean() {
        for (int index = 0; index < meanValue.size(); index++) {
            float mean = meanValue.get(index) / sampleFeatures.size();
            meanValue.set(index, mean);
        }
    }

    /**
     * Applies standardization for each feature in dataset.
     */
    public static void standardizeDataset() {
        for (int feature = 0; feature < FeatureExtractor.getFeaturesNumber() * 3; feature++) {
            float stdValue = (sampleFeatures.get(feature) - meanValue.get(feature))
                    / standardDeviation.get(feature);
            sampleFeatures.set(feature, stdValue);
        }
    }

    /**
     * Parse standardized sample in the classification format.
     * @param sample Array of samples
     * @param sampleClass Class of the given sample
     * @return Parsed string for given sample
     */
    public static String getParsedFeatures(ArrayList<Float> sample, String sampleClass) {
        parsedFeatures = sampleClass;
        for (int index = 1; index <= sample.size(); index++) {
            parsedFeatures = parsedFeatures + " " + index + ":" + sample.get(index -1).toString();
        }
        return parsedFeatures;
    }

    /**
     * Standardize given samples matching the format: < class_label 1:maximum_amplitude ..... 18:Skewness >.
     * @param parsedFeatures String with the parsedFeatures
     */
    public static String standardizeData(String parsedFeatures) {

        // Mean array and Standard deviation array initialization
        for (int i = 0; i< FeatureExtractor.getFeaturesNumber() * 3; i ++) {
            meanValue.add(0.0f);
            standardDeviation.add(0.0f);
        }

        // Remove all the white spaces and extra characters
        StringTokenizer st = new StringTokenizer(parsedFeatures," \t\n\r\f:");
        // Extract class_label
        samplesClass = st.nextToken();
        int column = 0;

        while(st.hasMoreTokens()) {
            // Pass the index of the feature
            st.nextToken();
            // Extract only the feature
            float feature = Float.parseFloat(st.nextToken());
            sampleFeatures.add(feature);
            // Calculate mean for each feature
            meanValue.set(column, meanValue.get(column) + feature);
            column++;
        }

        // Calculate mean for each feature
        calculateMean();
        // Calculate deviation for each feature
        calculateDeviation();
        // Standardize dataset
        standardizeDataset();

        // Return standardized features
        return getParsedFeatures(sampleFeatures, samplesClass);
    }
}
