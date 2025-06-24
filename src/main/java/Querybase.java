import org.jsoup.nodes.Document;

import java.util.*;

/**
 * The Querybase class provides an interactive command-line interface (CLI) to analyze and explore various
 * datasets related to the Summer Olympic Games. Users can retrieve information about Olympic sports, countries
 * participating in the Olympics, medal counts, podium sweeps, governing bodies, torch relays, flag bearers, and
 * more via predefined commands. The class utilizes web scraping techniques to fetch and process data from
 * a specified source website.
 *
 * This class features a main program loop where users input commands to perform specific tasks, such as listing
 * sports, querying medal statistics, and generating a collage of participating countries. The program supports
 * dynamic user input to customize queries and ensures input validation for several cases.
 *
 * Key capabilities of Querybase:
 * 1. Display sports starting with a specific letter.
 * 2. List countries considered "obsolete" in the context of Olympic participation.
 * 3. Query medal statistics by country, year, and medal type.
 * 4. Identify countries with podium sweeps in a specific year.
 * 5. Analyze total medals won by a country in a specific sport.
 * 6. Count governing bodies headquartered in a specific country.
 * 7. Calculate the number of countries involved in a torch relay hosted in a specific country.
 * 8. Check the status of a flag bearer for a specific country and year.
 * 9. Generate a collage of participating countries' flags for a specific year.
 *
 * This class also integrates helper methods to perform web scraping and process collected data.
 *
 * Note: This tool heavily relies on receiving inputs from users via the console and may rely on external
 * libraries or resources for parsing and data retrieval tasks.
 */
public class Querybase {
    public static String GLOBAL_URL = "https://en.wikipedia.org/wiki/Summer_Olympic_Games";

    /**
     * The entry point for the Web Scraper program. Provides a menu-driven interface for users
     * to interact with scraped Olympics data and perform various queries.
     *
     * @param args the command-line arguments (not used in this implementation)
     */
    public static void main(String[] args) {
        // Create a Scanner to enable interactivity
        Scanner scanner = new Scanner(System.in);
        Document homepage =  WebParser.fetchPage(Querybase.GLOBAL_URL);
        String instructions = """
            Welcome to the Web Scraper!
            We have scraped Olympics data. You cannot trick us with absurd inputs. You can:
            
            0: Type 0 to repeat the instructions.
            000: Type 000 to exit the program.
            1. Type 1 to list all past and present Olympic sports that start with the letter {inp: letter}.
            2. Type 2 to see all countries that have participated in the Olympics, but are now considered “obsolete”.
            3. Type 3 to list all countries that have won at least {inp: number} {inp: medal colour} medals in {inp: year}.
            4. Type 4 to list all countries that had podium sweeps in {inp:year}.
            5. Type 5 to list how many total medals {inp: country} has won in {inp: sport}.
            6. Type 6 to see how many governing bodies of the past or present sports from the Summer Olympics 
               are headquartered in {inp: country}.
            7. Type 7 to answer the question: among all Summer Olympics hosted in {inp: country} since {inp: year}, 
               how many countries did the torch relay that covered the longest total distance pass through?
            8. Type 8 to find out if the flag bearer of {inp: country} from {inp: year} is still alive.
            10. Type 10 to generate a collage of all countries that participated in the summer olympics of {year}.
            """;
        System.out.println(instructions);
        while (true) {
            try {
                String input = scanner.nextLine();
                switch (input) {
                    case "0":
                        System.out.println(instructions);
                        System.out.println("Next question... ");
                        break;
                    case "000":
                        System.out.println("Thanks for exploring our data!");
                        scanner.close();
                        return;

                    case "1":
                        System.out.println("\nList all past and present Olympic sports that start with the letter {inp: letter}");
                        System.out.print("Enter the starting letter(s): ");
                        String letter = scanner.nextLine().trim();
                        displayCountriesStartingWith(letter, homepage);
                        System.out.println("Next question... ");
                        break;

                    case "2":
                        System.out.println("\nSee all countries that have participated in the Olympics, but are now considered “obsolete”");
                        displayObsoleteNations(homepage, null);
                        System.out.println("Next question... ");
                        break;

                    case "3":
                        System.out.println("List all countries that have won at least {inp: number} {inp: medal colour} medals in {inp: year}\n");
                        System.out.print("Enter the minimum number of medals: ");
                        int silverCount = 0;
                        while (true) {
                            try {
                                silverCount = Integer.parseInt(scanner.nextLine().trim());
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("Please enter a valid number!");
                            }
                        }
                        System.out.print("Enter the colour of medals (gold, silver, or bronze): ");
                        String colour = scanner.nextLine().trim();
                        System.out.print("Enter the year: ");
                        String year = scanner.nextLine().trim();
                        displaySilverNations(homepage, silverCount, year, colour);
                        System.out.println("Next question... ");
                        break;

                    case "4":
                        System.out.println("list all countries that had podium sweeps in {inp:year}\n");
                        System.out.print("Enter the year: ");
                        String podiumYear = scanner.nextLine().trim();
                        displayPodiumSweeps(homepage, podiumYear);
                        System.out.println("Next question... ");
                        break;

                    case "5":
                        System.out.println("How many total medals {inp: country} has won in {inp: sport}\n");
                        System.out.print("Enter country: ");
                        String country = scanner.nextLine().trim();
                        System.out.print("Enter sport: ");
                        String sport = scanner.nextLine().trim();
                        displayTotalMedals(homepage, country, sport);
                        System.out.println("Next question... ");
                        break;

                    case "6":
                        System.out.println("How many governing bodies of the past or present sports from the Summer Olympics \n" +
                                "               are headquartered in {inp: country}}\n");
                        System.out.print("Enter country: ");
                        String hqCountry = scanner.nextLine().trim();
                        displayHeadquarterCount(homepage, hqCountry);
                        System.out.println("Next question... ");
                        break;

                    case "7":
                        System.out.println("Among all Summer Olympics hosted in {inp: country} since {inp: year}, \n" +
                                "               how many countries did the torch relay that covered the longest total distance pass through?\n");
                        System.out.print("Enter host country: ");
                        String hostCountry = scanner.nextLine().trim();
                        System.out.print("Enter starting year: ");
                        String startYear = scanner.nextLine().trim();
                        System.out.println("This might take a while..... ");
                        displayLongestTorchRelayCount(homepage, null, hostCountry, startYear);
                        System.out.println("Next question... ");
                        break;

                    case "8":
                        System.out.println("Find out if the flag bearer of {inp: country} from {inp: year} is still alive\n");
                        System.out.print("Enter country: ");
                        String flagCountry = scanner.nextLine().trim();
                        System.out.print("Enter year: ");
                        String flagYear = scanner.nextLine().trim();
                        displayFLagBearerStatus(homepage, flagCountry, flagYear);
                        System.out.println("Next question... ");
                        break;

                    case "10":
                        System.out.println("Generate a collage of all countries that participated in the summer olympics of {year}\n");
                        System.out.print("Enter year: ");
                        String collageYear = scanner.nextLine().trim();
                        createCollage(homepage, collageYear);
                        System.out.println("Next question... ");
                        break;

                    default:
                        System.out.println("Invalid input. Please enter a number between 0 and 8.");
                        System.out.println("Next question... ");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error occured: " + e.getMessage());
                System.out.print("Attempting recovery ... ");
                while (homepage == null) {
                    homepage =  WebParser.fetchPage(Querybase.GLOBAL_URL);
                }
                System.out.println("Recovered!");
                System.out.println("Instructions!");
            }
        }
    }

    /**
     * Creates a collage of flags and medal data for a specified year from the provided document.
     *
     * @param home the Document object containing the HTML content to parse data from
     * @param collageYear the year for which the collage is to be created
     */
    private static void createCollage(Document home, String collageYear) {
        ArrayList<String> flagSrcs = WebParser.getFlagSources(home, collageYear);
        System.out.println(flagSrcs.size());
        String filepath = SrcGetter.downloadAllFlags(flagSrcs);
        SrcGetter.convertFlagsToCSV(filepath);
        WebParser.getAllMedals(home, collageYear, filepath);
        SrcGetter.createCollage(filepath, 50);
    }

    /**
     * Displays the status of a flag bearer from a specific country in a given year.
     * This method retrieves and prints information about whether the flag bearer
     * from the specified country for the given year has passed away.
     *
     * @param homepage the Document object containing the HTML content to parse data from
     * @param country the name of the country whose flag bearer status is being queried
     * @param year the year for which the flag bearer status is being checked
     */
    private static void displayFLagBearerStatus(Document homepage, String country, String year
    ) {
        System.out.println(WebParser.didFlagBearerDie(homepage, year, country));
    }

    /**
     * Displays the count of countries that participated in the longest torch relay
     * for a specific country since the given year.
     *
     * @param homepage the Document object containing the HTML content for the main page
     * @param target the Document object containing the HTML content for the target page
     * @param country the name of the country for which the torch relay count is being queried
     * @param year the starting year from which to look for the longest torch relay
     */
    private static void displayLongestTorchRelayCount(Document homepage, Document target, String country, String year) {
        country = Helpers.capitalize(country, false);
        int result = WebParser.fetchNumTorchPassed(homepage, target, country, year);
        System.out.println("Longest torch relay in " + country + " since " +
                year + " was " + result + " countries.");
    }

    /**
     * Displays the count of headquartered locations for a specified country.
     * This method retrieves and prints the number of locations headquartered in
     * the specified country based on the provided webpage document.
     *
     * @param homepage the Document object containing the HTML content to parse data from
     * @param country the name of the country whose headquarters count is being queried
     */
    private static void displayHeadquarterCount(Document homepage, String country) {
        System.out.print("This might take a while..... ");
        System.out.println(WebParser.fetchNumHeadquartered(homepage, country) + " countries");
    }

    /**
     * Displays the total number of medals won by a specified country
     * in a particular sport based on the parsed data from the given webpage document.
     *
     * @param homepage the Document object containing the HTML content to parse data from
     * @param country the name of the country for which the medal count is being queried
     * @param sport the name of the sport for which total medals are being counted
     */
    private static void displayTotalMedals(Document homepage, String country, String sport) {
        country = Helpers.capitalize(country, false);
        sport = Helpers.capitalize(sport, true);
        System.out.print("This might take a while..... ");
        int result = WebParser.getTotalMedals(homepage, country, sport);
        System.out.println(country + " has " + result + " total medals in " + sport);
    }

    /**
     * Displays the list of podium sweeps for a specific year based on the parsed data
     * from the provided webpage document. The method retrieves and prints all podium
     * sweeps found within the HTML content.
     *
     * @param parent the Document object containing the HTML content to parse data from
     * @param year the year for which the podium sweeps information is being queried
     */
    public static void displayPodiumSweeps(Document parent, String year) {
        HashSet<String> result = WebParser.getPodiumSweepsByYear(parent, year);
        for (String str: result) {
            System.out.println(str);
        }
    }

    /**
     * Displays a list of nations that meet the medal count threshold for the specified medal type
     * (defaulting to silver) in a given year based on the parsed data from the provided document.
     *
     * @param home the Document object containing the HTML data to parse
     * @param thresh the minimum medal count threshold for a nation to be displayed
     * @param year the year for which the medal data is being queried
     * @param colour the medal type to filter nations by (e.g., "gold", "silver"); defaults to "silver"
     *               if an invalid or unspecified value is provided
     */
    public static void displaySilverNations(Document home, int thresh, String year, String colour) {
        int medal = 2;
        if (colour.equalsIgnoreCase("gold")) {
            medal = 4;
        } else if (colour.equalsIgnoreCase("silver")) {
            medal = 3;
        }
        HashSet<String> silvers = WebParser.getMedalNations(home, thresh, year, medal);
        for (String nation : silvers) {
            System.out.println(nation);
        }
    }

    /**
     * Displays the list of nations that are obsolete in the context of the Olympics.
     * This method identifies obsolete nations by comparing data from provided documents
     * across winter and summer Olympics and prints them to the console.
     *
     * @param parent2 the Document object containing the HTML data for the summer page
     * @param parent3 the Document object containing the HTML data for the winter page
     */
    public static void displayObsoleteNations(Document parent2, Document parent3) {
        HashSet<String> obsoleteSummer = WebParser.getObsoleteOlympicNations(parent2, parent3, "summer");
        for (String str: obsoleteSummer) {
            System.out.println(str);
        }
    }

    /**
     * Displays a list of countries whose names start with the specified letters.
     * The method retrieves country names starting with the given letters from the provided document
     * and prints them to the console.
     *
     * @param letters the starting letters to filter country names
     * @param parent1 the Document object containing the HTML content to parse data from
     */
    public static void displayCountriesStartingWith(String letters, Document parent1) {
        HashSet<String> countriesStartingWithC = WebParser.scrapeCountriesStartingWith(letters, parent1);
        for (String str: countriesStartingWithC) {
            System.out.println(str);
        }
    }
}
