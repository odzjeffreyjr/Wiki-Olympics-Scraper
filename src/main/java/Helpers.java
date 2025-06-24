import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Helpers class provides utility methods for string manipulation, validation, and data processing.
 * It includes common functionalities such as string capitalization, year validation, extracting country
 * names from URLs, and retrieving country information for a given city using the OpenStreetMap API.
 */
public class Helpers {
    public Helpers() {

    }
    /**
     * Capitalizes the given string according to specified rules.
     * If the onlyFirst parameter is true, only the first word in the string is capitalized.
     * If the onlyFirst parameter is false, all words in the string are capitalized.
     *
     * @param str the input string to capitalize; if null or empty, the method returns the input as-is.
     * @param onlyFirst a boolean indicating whether only the first word should be capitalized.
     *                  If true, only the first word is capitalized. If false, all words are capitalized.
     * @return a capitalized version of the input string based on the value of onlyFirst,
     *         or the original string if it is null or empty.
     */
    public static String capitalize(String str, boolean onlyFirst) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        String[] words = str.split("\\s+");
        StringBuilder result = new StringBuilder();
        boolean set = false;
        for (String word : words) {
            if (!word.isEmpty()) {
                if (!onlyFirst || !set) {
                    result.append(word.substring(0, 1).toUpperCase())
                            .append(word.substring(1).toLowerCase())
                            .append(" ");
                    set = true;
                } else {
                    result.append(word).append(" ");
                }
            }
        }
        return result.toString().trim();
    }

    /**
     * Validates whether the provided string represents a valid year
     * based on a predefined range and format.
     *
     * @param year the year to validate as a string. It should follow the format of
     *             four digits and fall within the range of 1800 to 2025.
     * @return true if the year matches the criteria, false otherwise.
     */
    public static boolean isValidYear(String year) {
        String regex = "^(18[0-9]{2}|20[0-1][0-9]|202[0-5]|19[0-9]{2})$";
        return year.matches(regex);
    }

    /**
     * Extracts the country name from a given URL that matches a specific pattern.
     * The URL is expected to contain a segment in the format "Flag_of_<country_name>.<extension>".
     * The method processes the extracted segment to normalize it (e.g., replacing spaces, hyphens,
     * and certain special characters) and returns the country name in lowercase.
     * If no match is found, the method returns "Unknown".
     *
     * @param url the URL string from which the country name is to be extracted.
     *            It should contain a segment in the format "Flag_of_<country_name>.<extension>".
     * @return the normalized country name extracted from the URL, or "Unknown" if no match is found.
     */
    public static String extractCountryName(String url) {
        Pattern pattern = Pattern.compile("Flag_of_([a-zA-Z_\\-%28%29]+)\\.");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1).replace(" ", "_").replace("-", "_")
                    .replace("%28", "").replace("%29", "")
                    .toLowerCase();
        }
        return "Unknown";
    }
}
