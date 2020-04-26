package fall.detection.classifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Extracts from the given input file all the values given by accelerometer for axis X, Y, Z.
 * The input file must match the following pattern:
 *     x_value, y_value, z_value
 *     x_value, y_value, z_value
 *
 * To avoid future bugs, all the whitespaces will be removed in the parser.
 */
public class DataParser {
    /**
     * Arrays for accelerometer values for axes X, Y, Z
     */
    private static ArrayList<Float> xSamples = new ArrayList<>();
    private static ArrayList<Float> ySamples = new ArrayList<>();
    private static ArrayList<Float> zSamples = new ArrayList<>();

    public static ArrayList<Float> getxSamples() {
        return xSamples;
    }

    public static ArrayList<Float> getySamples() {
        return ySamples;
    }

    public static ArrayList<Float> getzSamples() {
        return zSamples;
    }

    /**
     * @param fileName Name of the input file
     */
    public static void extractFilesData(String fileName) {
        try {
            File inputFile = new File(fileName);
            Scanner myReader = new Scanner(inputFile);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                // Remove all whitespaces
                data = data.replaceAll("\\s","");
                if (data.length() != 0) {
                    // Split input in values delimited by ','
                    String[] dataArray = data.split(",");
                    // Extract only one value for X, Y, Z in this order
                    xSamples.add(Float.parseFloat(dataArray[0]));
                    ySamples.add(Float.parseFloat(dataArray[1]));
                    zSamples.add(Float.parseFloat(dataArray[2]));
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while trying to read from file.");
            e.printStackTrace();
        }
    }

    /**
     * @param fileName Name of the input file
     */
    public static void extractData(String fileName) {
        try {
            File inputFile = new File(fileName);
            Scanner myReader = new Scanner(inputFile);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                // Remove all whitespaces
                data = data.replaceAll("\\s","");
                if (data.length() != 0) {
                    // Split input in values delimited by ','
                    String[] dataArray = data.split(",");
                    // Extract only one value for X, Y, Z in this order
                    xSamples.add(Float.parseFloat(dataArray[0]));
                    ySamples.add(Float.parseFloat(dataArray[1]));
                    zSamples.add(Float.parseFloat(dataArray[2]));
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while trying to read from file.");
            e.printStackTrace();
        }
    }
}
