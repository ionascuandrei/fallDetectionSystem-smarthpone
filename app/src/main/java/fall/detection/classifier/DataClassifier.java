package fall.detection.classifier;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
    private static void createAxisFeatures(ArrayList<Float> xSamples, ArrayList<Float> ySamples, ArrayList<Float> zSamples) {
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
    private static String getParsedFeatures() {
        for (int index = 1; index <= FeatureExtractor.getFeaturesNumber() * axisNumber; index++) {
            parsedFeatures = parsedFeatures + " " + index + ":" + sampleFeatures.get(index -1).toString();
        }
        return parsedFeatures;
    }

    /**
     * Converts Gravity into Acceleration Data with SysFall function:
     * Acceleration [g]: [(2*Range)/(2^Resolution)]*AD
     * @param inputArray Gravity array for given axis
     * @return Array filled with AD values of inputArray
     */
    private static ArrayList<Float> gravityIntoAccelerationData (ArrayList<Double> inputArray) {
        ArrayList<Float> convertedResult = new ArrayList<>(inputArray.size());
        for (int i = 0; i < inputArray.size(); i++) {
            // TODO : Modica Range-ul si Resolition conform accelerometrului din fitbit
            double range = 16;
            double resolution = 13;
            double result = (inputArray.get(i) / ((2 * range) / Math.pow(2,resolution)));
            convertedResult.add(i, (float) result);
        }
        return convertedResult;
    }

    public static String classifyData(ArrayList<Double> xArray, ArrayList<Double> yArray, ArrayList<Double> zArray, AssetManager assetManager) throws IOException {

        // TODO: Pe cazul asta ai facut din date G in AD (acceleration data) precum in Paper
        // Convert Gravity into AD as in classifier data
        ArrayList<Float> convertedXArray = gravityIntoAccelerationData(xArray);
        ArrayList<Float> convertedYArray = gravityIntoAccelerationData(yArray);
        ArrayList<Float> convertedZArray = gravityIntoAccelerationData(zArray);
        // Debug
        System.out.println("ConvertedX: " + convertedXArray);
        System.out.println("ConvertedY: " + convertedYArray);
        System.out.println("ConvertedZ: " + convertedZArray);

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

        // Open Classifier Model file
        String[] files;
        BufferedReader model = null;
        try {
            files = assetManager.list("");
            if (files != null) {
                model = new BufferedReader(new InputStreamReader(assetManager.open(files[0])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Call Classification predict
        String classificationResult = SvmPredict.predictStringInput(standardizedFeatures, model, 0);

        // Convert float result into string result
        if (Float.parseFloat(classificationResult) == Float.parseFloat("-1")) {
            classificationResult = "ADL";
        } else {
            if (Float.parseFloat(classificationResult) == Float.parseFloat("1")) {
                classificationResult = "FALL";
            }
        }
        // Debug
        System.out.println("[DataClassifier] Classification result: " + classificationResult);

        // Return processed value
        return classificationResult;
    }
}