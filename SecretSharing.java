import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class SecretSharing {

    public static void main(String[] args) {
        // JSON File path
        String filePath = "data.json";  // Update with your JSON file path

        // Read JSON from file and parse it
        JsonObject jsonObject = parseJsonFile(filePath);

        // Extract n and k values
        int n = jsonObject.getAsJsonObject("keys").get("n").getAsInt();
        int k = jsonObject.getAsJsonObject("keys").get("k").getAsInt();

        // Create a map to store points
        Map<Integer, BigInteger> points = parsePoints(jsonObject);

        // Calculate the secret (constant term) using Lagrange Interpolation
        BigInteger secret = lagrangeInterpolation(points, 0);

        // Output the result
        System.out.println("The secret (constant term 'c') is: " + secret);
    }

    /**
     * Reads the JSON data from a file and parses it into a JsonObject.
     * 
     * @param filePath The path to the JSON file.
     * @return The parsed JsonObject.
     */
    private static JsonObject parseJsonFile(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses the points from the JSON object.
     * 
     * @param jsonObject The parsed JSON object containing the points.
     * @return A map of points.
     */
    private static Map<Integer, BigInteger> parsePoints(JsonObject jsonObject) {
        Map<Integer, BigInteger> points = new HashMap<>();
        jsonObject.entrySet().stream()
            .filter(entry -> entry.getKey().matches("\\d+")) // Only process numeric keys
            .forEach(entry -> {
                int x = Integer.parseInt(entry.getKey());
                JsonObject point = entry.getValue().getAsJsonObject();
                String base = point.get("base").getAsString();
                String value = point.get("value").getAsString();
                points.put(x, new BigInteger(value, Integer.parseInt(base)));
            });
        return points;
    }

    /**
     * Performs Lagrange interpolation to find the value of the polynomial at xValue.
     * 
     * @param points - Map of x, y pairs
     * @param xValue - The x-value to evaluate the polynomial (0 for constant term)
     * @return The interpolated value (BigInteger)
     */
    private static BigInteger lagrangeInterpolation(Map<Integer, BigInteger> points, int xValue) {
        BigInteger result = BigInteger.ZERO;

        for (Map.Entry<Integer, BigInteger> entry1 : points.entrySet()) {
            int xi = entry1.getKey();
            BigInteger yi = entry1.getValue();
            BigInteger term = yi;

            // Efficient Lagrange term calculation
            for (Map.Entry<Integer, BigInteger> entry2 : points.entrySet()) {
                int xj = entry2.getKey();
                if (xi != xj) { // Skip division by zero
                    term = term.multiply(BigInteger.valueOf(xValue - xj)).divide(BigInteger.valueOf(xi - xj));
                }
            }
            result = result.add(term);
        }
        return result;
    }
}
