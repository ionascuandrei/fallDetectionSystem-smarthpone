package fall.detection.classifier;

import java.io.FileWriter;
import java.io.IOException;
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
        for (int i = 0; i < inputArray.size(); i++) {
            int value = inputArray.get(i);
            // If the high bit is on, then it is a negative number, and actually counts backwards.
            float convertedValue = (value >= 0x8000) ? -(0x10000 - value) / 0x8000 : value / 0x7FFF;
            convertedResult.set(i, convertedValue);
        }
        return convertedResult;
    }

    public static String classifyData(ArrayList<Integer> xArray, ArrayList<Integer> yArray, ArrayList<Integer> zArray) {
        ArrayList<Float> convertedXArray = int16ToFloat32Converter(xArray);
        ArrayList<Float> convertedYArray = int16ToFloat32Converter(yArray);
        ArrayList<Float> convertedZArray = int16ToFloat32Converter(zArray);

        System.out.println("X: " + convertedXArray.toString());
        System.out.println("Y: " + convertedYArray.toString());
        System.out.println("Z: " + convertedZArray.toString());

        // TODO: Return processed value
        return null;
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.print("usage: trainingDataCreator <nr_of_input_files> <path_to_files> <file1 file2 ...> <output_file>\n");
            System.exit(1);
        } else {
            if (Integer.parseInt(args[0]) > 0) {
                try {
                    FileWriter output = new FileWriter(args[args.length-1]);

                    for (int i = 0; i < Integer.parseInt(args[0]); i++) {
                        // Concatenating path of the file with the actual name
                        String fileName = args[1] + args[i+2];
                        // Get from file name if it is a FALL (F) or ADL (D) action
                        if (args[i+2].charAt(0) == 'F') {
                            parsedFeatures = FALL;
                        } else {
                            parsedFeatures = ADL;
                        }
                        // Printing for debug purpose
                        System.out.println("[trainingDataCreator] " + fileName);
                        // Parse data from given file
                        DataParser.extractFilesData(fileName);
                        // Compute features for the given samples
                        createAxisFeatures(DataParser.getxSamples(), DataParser.getySamples(), DataParser.getzSamples());
                        // Write on output file features in classification format
                        output.write(getParsedFeatures());
                        output.write("\n");
                    }
                    output.close();
                } catch (IOException e) {
                    System.err.print("There was a problem at creating the output file\n");
                    e.printStackTrace();
                }
            } else {
                System.err.print("<nr_of_input_files> should be > 0\n");
                System.exit(1);
            }
        }
    }
}