package fall.detection.classifier;

import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


import fall.detection.general.Constants;
import fall.detection.classifier.FeatureExtractor;

/**
 * Computes a file which contains features of all the actions given in the input files in the classification format:
 * <class_label> <1:maximum_amplitude 2:minimum_amplitude 3:mean_ampltitude 4:variance 5:kurtosis 6:Skewness> <7-12> <13-18>
 *     <1-6>   - Features for X axis
 *     <7-12>  - Features for Y axis
 *     <13-18> - Features for Z axis
 * In the current format there are only 2 classes:
 *  "1"  - Fall Action
 *  "-1" - ADL Action
 */
public class DataClassifier {
    private static int axisNumber = 3;
    private static String ADL = "-1";
    private static String FALL = "1";

    private static ArrayList<Float> sampleFeatures;
    private static String parsedFeatures;
    private static String standardizedFeatures;

    /**
     * Computes the features for each axis.
     * It uses featureExtractor Class.
     * @param xSamples Array of samples for X axis
     * @param ySamples Array of samples for Y axis
     * @param zSamples Array of samples for Z axis
     */
    public static void createAxisFeatures(ArrayList<Float> xSamples, ArrayList<Float> ySamples, ArrayList<Float> zSamples) {
        // Get features for X axis
        FeatureExtractor.extractFeatures(xSamples);
        sampleFeatures = FeatureExtractor.getFeatures();
        // Get features for Y axis
        FeatureExtractor.extractFeatures(ySamples);
        sampleFeatures.addAll(FeatureExtractor.getFeatures());
        // Get features for Z axis
        FeatureExtractor.extractFeatures(zSamples);
        sampleFeatures.addAll(FeatureExtractor.getFeatures());
    }

    /**
     * Returns string with features for the input samples in classification format
     * @return String with features
     */
    public static String getParsedFeatures() {
        for (int index = 1; index <= FeatureExtractor.getFeaturesNumber() * axisNumber; index++) {
            parsedFeatures = parsedFeatures + " " + index + ":" + sampleFeatures.get(index -1).toString();
        }
        return parsedFeatures;
    }

    private static ArrayList<Float> int16ToFloat32Converter (ArrayList<Integer> inputArray) {
        ArrayList<Float> convertedResult = new ArrayList<>(inputArray.size());
        Log.i(Constants.WSS, "Size:" + inputArray.size() + "INPUT: " + inputArray.toString());
        for (int i = 0; i < inputArray.size(); i++) {
            float value = inputArray.get(i);
//            System.out.println("I: " + i + " value: " + value);
            // If the high bit is on, then it is a negative number, and actually counts backwards.
            float convertedValue = (value >= 0x8000) ? - ((0x10000 - value) / 0x8000) :  (value / 0x7FFF);
//            Log.i(Constants.WSS, "CONV: " + convertedValue);
            convertedResult.add(i, convertedValue);
        }
        return convertedResult;
    }

    public static String classifyData(ArrayList<Integer> xArray, ArrayList<Integer> yArray, ArrayList<Integer> zArray) {
        // Convert 16Int to 32Float
        ArrayList<Float> convertedXArray = int16ToFloat32Converter(xArray);
        ArrayList<Float> convertedYArray = int16ToFloat32Converter(yArray);
        ArrayList<Float> convertedZArray = int16ToFloat32Converter(zArray);

        Log.i(Constants.WSS,  "X: " + convertedXArray.toString());
        Log.i(Constants.WSS,  "Y: " + convertedYArray.toString());
        Log.i(Constants.WSS,  "Z: " + convertedZArray.toString());

        // TODO: Schimba daca este nevoie din ADL in altceva
        parsedFeatures = ADL;
        // Save data in DataParser
        DataParser.extractData(convertedXArray, convertedYArray, convertedZArray);
        // Compute features for the given samples
        createAxisFeatures(DataParser.getxSamples(), DataParser.getySamples(), DataParser.getzSamples());
        // Create string of parsed data in classification format
        String dataString = getParsedFeatures();
        // Standardize data
        standardizedFeatures = DataStandardize.standardizeData(dataString);

        // TODO: Return processed value
        return null;
    }

}