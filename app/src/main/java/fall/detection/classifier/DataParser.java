package fall.detection.classifier;

import java.util.ArrayList;


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
     * @param xValues Array of X values
     * @param yValues Array of Y values
     * @param zValues Array of Z values
     */
    public static void extractData(ArrayList<Float> xValues, ArrayList<Float> yValues, ArrayList<Float> zValues) {
        xSamples = xValues;
        ySamples = yValues;
        zSamples = zValues;
    }
}
