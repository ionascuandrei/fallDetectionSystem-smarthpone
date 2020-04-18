package fall.detection.classifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Standardizes given input file using Mean and Deviation.
 * It must respect the classification format:
 *  < class_label 1:maximum_amplitude ..... 18:Skewness >
 *
 */
public class dataStandardize {
    private static ArrayList<ArrayList<Float>> sampleFeatures = new ArrayList<>();
    private static ArrayList<Float> meanValue = new ArrayList<>();
    private static ArrayList<Float> standardDeviation = new ArrayList<>();
    private static String parsedFeatures;
    private static ArrayList<String> samplesClass = new ArrayList<>();

    /**
     * Calculates deviation for each feature.
     */
    public static void calculateDeviation() {
        for (int feature = 0; feature < featureExtractor.getFeaturesNumber() * 3; feature++) {
            for (int sample = 0; sample < sampleFeatures.size(); sample++) {
                float variance = sampleFeatures.get(sample).get(feature) - meanValue.get(feature);
                variance = (float) Math.pow(variance, 2);
                standardDeviation.set(feature, standardDeviation.get(feature) + variance);
            }
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
        for (int sample = 0; sample < sampleFeatures.size(); sample++) {
            for (int feature = 0; feature < featureExtractor.getFeaturesNumber() * 3; feature++) {
                float stdValue = (sampleFeatures.get(sample).get(feature) - meanValue.get(feature))
                        / standardDeviation.get(feature);
                sampleFeatures.get(sample).set(feature, stdValue);
            }
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
     * Writes parsed samples in the output folder.
     * @param myWriter File writer
     */
    public static void writeStandardizedDataset(FileWriter myWriter) {
        int index = 0;
        for (ArrayList<Float> sample : sampleFeatures) {
            try {
                myWriter.write(getParsedFeatures(sample, samplesClass.get(index)));
                myWriter.write("\n");
                index++;
            } catch (IOException e) {
                System.out.println("An error occurred while trying to write in output file.");
                e.printStackTrace();
            }

        }
    }

    /**
     * Standardize given samples matching the format: < class_label 1:maximum_amplitude ..... 18:Skewness >.
     * @param inputFilename Input file name
     * @param outputFilename Output file name
     */
    public static void standardizeData(String inputFilename, String outputFilename) {
        try {
            File inputFile = new File(inputFilename);
            Scanner myReader = new Scanner(inputFile);
            int row = 0;
            // Mean array and Standard deviation array initialization
            for (int i = 0; i< featureExtractor.getFeaturesNumber() * 3; i ++) {
                meanValue.add(0.0f);
                standardDeviation.add(0.0f);
            }

            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                if (line.length() != 0) {
                    // Remove all the white spaces and extra characters
                    StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
                    // Extract class_label
                    samplesClass.add(st.nextToken());
                    // Initialize sample features array
                    sampleFeatures.add(new ArrayList<Float>());
                    int column = 0;

                    while(st.hasMoreTokens()) {
                        // Pass the index of the feature
                        st.nextToken();
                        // Extract only the feature
                        float feature = Float.parseFloat(st.nextToken());
                        sampleFeatures.get(row).add(feature);
                        // Calculate mean for each feature
                        meanValue.set(column, meanValue.get(column) + feature);
                        column++;
                    }
                    row++;
                }
            }

            // Calculate mean for each feature
            calculateMean();
            // Calculate deviation for each feature
            calculateDeviation();
            // Standardize dataset
            standardizeDataset();

            // Write on output file
            File outputFile = new File(outputFilename);
            FileWriter myWriter = new FileWriter(outputFile);
            writeStandardizedDataset(myWriter);

            // Close files
            myWriter.close();
            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while trying to read from file.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occurred while trying to write in output file.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.print("usage: dataStandardize <input_file> <output_file>\n");
            System.exit(1);
        } else {
            standardizeData(args[0], args[1]);
        }
    }
}
