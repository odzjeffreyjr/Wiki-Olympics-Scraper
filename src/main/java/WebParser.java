import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * The WebParser class provides utility methods to scrape and process
 * web documents, primarily focused on extracting information from structured
 * data sources such as Wikipedia. The class uses the Jsoup library for HTML parsing
 * and document traversal.
 * <p>
 * It includes static methods to fetch web pages, extract and analyze specific pieces
 * of information, and process hierarchical and tabular data structures. Methods are
 * available to deal with topics such as Olympic medal-related data, national flag information,
 * torch relay events, and more.
 */
public class WebParser {
    /**
     * Fetches the HTML content of the given URL and parses it into a Jsoup Document.
     *
     * @param url the URL of the webpage to be fetched
     * @return a Document object containing the HTML content of the fetched page,
     *         or null if an IOException occurs during the connection
     */
    public static Document fetchPage(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            System.out.println("Couldn't connect to " + url);
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Counts the number of organizations headquartered in a specific country based on the provided
     * parent Document and extracts the relevant pages.
     *
     * @param parent the parent Document representing the starting point for scraping data
     * @param country the name of the country to check for headquartered organizations
     * @return an integer representing the number of organizations headquartered in the specified country
     */
    public static int fetchNumHeadquartered(Document parent, String country) {
        int result = 0;
        ArrayList<Document> documents = fetchSportsPage(parent, "impossibleStringCountry");
        ArrayList<Document> countryPages = new ArrayList<>();
        for (Document doc : documents) {
            countryPages.add(headQuarterPage(doc));
        }
        for (Document doc : countryPages) {
            result += isHeadQuarteredIn(doc, country);
        }
        return result;
    }

    /**
     * Determines if the organization described by the given Document is headquartered
     * in the specified country.
     *
     * @param doc the Jsoup Document object representing the webpage to be checked
     * @param country the name of the country to verify as the organization's headquarters
     * @return 1 if the organization is headquartered in the specified country; 0 otherwise
     */
    private static int isHeadQuarteredIn(Document doc, String country) {
        if (doc != null) {
            Element table = doc.select("table.infobox").first();
            Elements rows = null;
            if (table != null) {
                rows = table.select("tr");
                for (Element row : rows) {
                    Element title = row.select("th").first();
                    if (title != null && title.text().equals("Headquarters")) {
                        Element dataCell = row.selectFirst("td");
                        if (dataCell != null) {
                            StringBuilder combined = new StringBuilder();
                            for (org.jsoup.nodes.Node node : dataCell.childNodes()) {
                                if (node instanceof org.jsoup.nodes.TextNode textNode) {
                                    String text = textNode.text();
                                    if (!text.isEmpty()) {
                                        combined.append(text);
                                    }
                                } else if (node instanceof Element element && element.tagName().equals("a")) {
                                    String text = element.text();
                                    if (!text.isEmpty()) {
                                        combined.append(text);
                                    }
                                }
                            }
                            String str = combined.toString().replace(",","");
                            String[] parts = str.split("\\s+");
                            for (String string: parts) {
                                if (string.equalsIgnoreCase(country.trim())) {
                                    return 1;
                                }
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Extracts and retrieves the specific subpage of an organization linked under the "Governing body" section
     * in the provided document's infobox table.
     *
     * @param doc the Jsoup Document object representing the webpage to be checked;
     *            must not be null to process the document.
     * @return a Document object representing the subpage of the governing body if found,
     *         or null if the "Governing body" link does not exist or the input document is null.
     */
    private static Document headQuarterPage(Document doc) {
        if (doc != null) {
            Element table = doc.select("table.infobox").first();
            Elements rows = null;
            if (table != null) {
                rows = table.select("tr");
                for (Element row : rows) {
                    Element title = row.select("th").first();
                    if (title != null && title.text().equals("Governing body")) {
                        Element textBox = row.select("a").first();
                        if (textBox != null) {
                            String link = textBox.attr("href");
                            return fetchPage("https://en.wikipedia.org" + link);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Calculates the total number of medals won by a specific country in a given sport
     * by parsing information from a structured HTML document.
     *
     * @param parent the parent Document representing the starting webpage. Can be null
     *               to automatically fetch the default page.
     * @param country the name of the country whose medal count is being queried.
     * @param sport the name of the sport for which the medal count is being computed.
     * @return the total number of medals won by the specified country in the given sport.
     */
    public static int getTotalMedals(Document parent, String country, String sport) {
        int result = 0;
        if (parent == null) {
            parent = fetchPage(Querybase.GLOBAL_URL);
        }
        if (parent != null) {
            ArrayList<Document> sportsPages = fetchSportsPage(parent, sport);
            if (!sportsPages.isEmpty()) {
                Element sportsPage = sportsPages.getFirst();
                Element medalTableHeading = sportsPage.select("h2").stream()
                        .filter(el -> el.text().equalsIgnoreCase("Medal table"))
                        .findFirst()
                        .orElse(null);
                if (medalTableHeading == null) {
                    medalTableHeading = sportsPage.select("h3").stream()
                            .filter(el -> el.text().equalsIgnoreCase("Medal table"))
                            .findFirst()
                            .orElse(null);
                }
                if (medalTableHeading == null) {
                    medalTableHeading = sportsPage.select("h1").stream()
                            .filter(el -> el.text().equalsIgnoreCase("Medal table"))
                            .findFirst()
                            .orElse(null);
                }

                if (medalTableHeading != null) {
                    Element ancestor = medalTableHeading.parent();
                    if (ancestor != null) {
                        Element next = ancestor.nextElementSibling();
                        while (next != null && !next.tagName().equals("table")) {
                            next = next.nextElementSibling();
                        }
                        if (next != null) {
                            Element tBody = next.getElementsByTag("tbody").first();
                            Elements rows = null;
                            if (tBody != null) {
                                rows = tBody.select("tr");
                                for (Element row : rows) {
                                    Element title = row.select("a").first();
                                    if (title != null) {
                                        String name = title.text();
                                        if (name.equals(country)) {
                                            Element columns = row.select("td").last();
                                            if (columns != null) {
                                                result += Integer.parseInt(columns.text());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Fetches and returns a list of documents corresponding to the sports pages
     * listed in the table of the given parent document. If the specified sport
     * is found, the method returns only the document for that sport.
     *
     * @param parent the parent Document object representing the webpage containing the sports list
     * @param sport the name of the specific sport to search for; if matched,
     *              returns the page of this sport
     * @return a list of Document objects representing
     * the sports pages extracted from the given parent;
     *         if the specified sport is found, returns a singleton list containing only its page
     */
    private static ArrayList<Document> fetchSportsPage(Document parent, String sport) {
        Element table = parent.select("table.wikitable.sortable").first();
        ArrayList<Document> result = new ArrayList<Document>();
        if (table != null) {
            Element tBody = table.select("tbody").first();
            if (tBody != null) {
                Elements rows = tBody.select("tr");
                for (Element row : rows) {
                    Element title = row.select("a").first();
                    if (title != null) {
                        String name = title.text();
                        String link = title.attr("href");
                        Document thisPage = fetchPage("https://en.wikipedia.org" + link);
                        result.add(thisPage);
                        if (name.equals(sport)) {
                            ArrayList<Document> singleton = new ArrayList<Document>();
                            singleton.add(thisPage);
                            return singleton;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Retrieves a set of country names that achieved podium sweeps in a specified year
     * by parsing the given document for relevant data.
     *
     * @param parent the parent Document object to start the parsing from; can be null to trigger
     *               fetching a new document based on the year
     * @param year   the year for which podium sweeps are to be extracted
     * @return a HashSet containing the names of countries that
     * achieved podium sweeps in the specified year
     */
    public static HashSet<String> getPodiumSweepsByYear(Document parent, String year) {
        HashSet<String> sweepers = new HashSet<>();
        Document home = getPodiumSweepsPage(parent, year);
        Element heading = null;
        if (home != null) {
            heading = home.select("h3#Podium_sweeps").first();
            if (heading != null) {
                Element ancestor = heading.parent();
                if (ancestor != null) {
                    Element next = ancestor.nextElementSibling();
                    while (next != null && !next.tagName().equals("table")) {
                        next = next.nextElementSibling();
                    }
                    if (next != null) {
                        Elements rows = next.select("tr");
                        boolean onHeader = true;
                        for (Element row : rows) {
                            if (onHeader) {
                                onHeader = false;
                                continue;
                            }
                            Element noc = row.select("td").get(3);
                            Element textBox = noc.selectFirst("a");
                            if (textBox != null) {
                                sweepers.add(textBox.text());
                            }
                        }
                    }
                }
            }
        }
        return sweepers;
    }

    /**
     * Retrieves the webpage corresponding to a specific Olympic year when
     * podium sweeps are documented.
     * If the parent document is not provided, it fetches the main page by
     * default and searches for the specified year.
     *
     * @param parent the parent Document representing the starting webpage; can be null to trigger
     *               fetching the default main page
     * @param year   the year for which the podium sweeps page is to be retrieved
     * @return a Document object containing the HTML content of the podium
     * sweeps page for the specified year,
     *         or null if the year is not found or the parent document could not be fetched
     */
    public static Document getPodiumSweepsPage(Document parent, String year) {
        HashSet<String> podiumSweeps = new HashSet<>();
        if (parent == null) {
            parent = fetchPage(Querybase.GLOBAL_URL);
        }
        if (parent != null) {
            Element table = parent.select(".hlist").get(5);
            Elements years = table.select("li");
            for (Element thisYear : years) {
                Element yearBox = thisYear.select("a").first();
                if (yearBox != null && yearBox.text().equals(year)) {
                    String link = yearBox.attr("href");
                    System.out.println("https://en.wikipedia.org" + link);
                    return fetchPage("https://en.wikipedia.org" + link);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a list of flag image sources from an Olympic podium
     * sweeps webpage for a specified year.
     * It identifies a section labeled "Participating Nations" on the
     * page and extracts the image URLs
     * of flags listed under that section, if available.
     *
     * @param parent the parent Document object representing the starting webpage; can be null, in which case the method
     *               fetches the corresponding page for the specified year
     * @param year   a String representing the year for which flag sources are to be extracted
     * @return an ArrayList containing the URLs of flag images as Strings;
     *         returns an empty list if no such images are found or the input is invalid
     */
    public static ArrayList<String> getFlagSources(Document parent, String year) {
        Document home = getPodiumSweepsPage(parent, year);
        Element heading = null;
        ArrayList<String> ret = new ArrayList<>();
        if (home != null) {
            Element participatingNations = home.select("h2").stream()
                    .filter(el -> el.text().toLowerCase().contains("participating")
                            && el.text().toLowerCase().contains("nation"))
                    .findFirst()
                    .orElse(null);
            if (participatingNations == null) {
                participatingNations = home.select("h3").stream()
                        .filter(el -> el.text().toLowerCase().contains("participating")
                                && el.text().toLowerCase().contains("nation"))
                        .findFirst()
                        .orElse(null);
            }
            if (participatingNations == null) {
                participatingNations = home.select("h1").stream()
                        .filter(el -> el.text().toLowerCase().contains("participating")
                                && el.text().toLowerCase().contains("nation"))
                        .findFirst()
                        .orElse(null);
            }

            if (participatingNations != null) {
                Element ancestor = participatingNations.parent();
                if (ancestor != null) {
                    Element next = ancestor.nextElementSibling();
                    while (next != null && !next.tagName().equals("table") &&
                            !next.hasClass("wikitable")) {
                        next = next.nextElementSibling();
                    }
                    if (next != null) {
                        Element tBody = next.getElementsByTag("tbody").first();
                        Elements rows = null;
                        if (tBody != null) {
                            rows = tBody.select("tr");
                            for (Element row : rows) {
                                Elements countries = row.select("li");
                                for (Element country: countries) {
                                    Element img = country.selectFirst("img");
                                    if (img != null) {
                                        String src = "https:" + img.attr("src");
                                        ret.add(src);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Identifies and retrieves a set of nations that are no longer
     * participating in the Olympic Games
     * based on the provided parent document and page document for
     * a specified season.
     * If the page document is null, it attempts to fetch the page from the parent.
     *
     * @param parent the parent Document object from which the current Olympic data may be derived;
     *               can be null, in which case it attempts to fetch the parent document.
     * @param page the Document object representing the current Olympic nations page;
     *             can be null, in which case it attempts to fetch this page from
     *             the parent document.
     * @param season a String representing the Olympic season (e.g., "summer" or "winter").
     * @return a HashSet containing the names of nations that are considered
     * obsolete in the specified season.
     */
    public static HashSet<String> getObsoleteOlympicNations(Document parent, Document page, String season) {
        HashSet<String> obsoleteNations = new HashSet<>();
        if (page == null) {
            if (parent == null) {
                parent = WebParser.fetchParentPage(season);
            }
            if (parent != null && page == null) {
                page = fetchCurrentFromParent(parent, season);
            }
        }
        if (page != null) {
            Element table = page.select("table.wikitable").first();
            if (table != null) {
                Element tbody = table.select("tbody").first();
                if (tbody != null) {
                    Elements rows = tbody.select("tr");
                    for (Element row: rows) {
                        Element firstColumn = row.select("td[bgcolor=#e0e0e0]:first-child").first();
                        if (firstColumn !=  null) {
                            Element textBox = firstColumn.selectFirst("a");
                            if (textBox != null) {
                                String country = textBox.text();
                                obsoleteNations.add(country);
                            }
                        }
                    }
                }
            }
        }
        return obsoleteNations;
    }

    /**
     * Extracts and retrieves the HTML page for the list of participating nations in the specified
     * Olympic season. Searches for a hyperlink within the provided parent document that matches
     * the given season, constructs the appropriate URL, and fetches the page.
     *
     * @param parent the parent Document object from which the hyperlink to the
     *               desired page is to be extracted.
     *               If null, the method will return null.
     * @param season a String representing the Olympic season (e.g., "summer" or "winter").
     *               Must not be null or empty; otherwise, the method will return null.
     * @return a Document object containing the HTML content of the page
     * corresponding to the specified season,
     *         or null if the page is not found or input is invalid.
     */
    public static Document fetchCurrentFromParent(Document parent, String season) {
        if (parent == null || season == null || season.isEmpty()) {
            return null;
        }
        Element linkHolder = parent.select(
                "a[title=List of participating nations at the "
                        + Helpers.capitalize(season, true) + " Olympic Games]").last();
        if (linkHolder != null) {
            String link = linkHolder.attr("href");
            if (!link.isEmpty()) {
                return WebParser.fetchPage("https://en.wikipedia.org/" + link);
            }
        }
        return null;
    }

    /**
     * Retrieves a set of nation names that have won at least the specified number of medals
     * of a specific type in a given year, by parsing Olympic medal data from an HTML document.
     *
     * @param parent the parent Document representing the starting webpage;
     *               can be null, in which case
     *               the method attempts to fetch the corresponding page for the provided year.
     * @param threshold the minimum number of medals of the specified type a nation must win
     *                  to be included in the result.
     * @param year a String representing the year for which medal data is to be extracted.
     * @param medal an integer indicating the medal type column to check (e.g.,
     *              1 for gold, 2 for silver, 3 for bronze).
     * @return a HashSet containing the names of nations that meet the specified
     * medal-winning criteria.
     *         Returns an empty set if no nations meet the criteria or if the input is invalid.
     */
    public static HashSet<String> getMedalNations(Document parent,
        int threshold, String year, int medal) {
        HashSet<String> silverNations = new HashSet<>();
        Document target = getYearMedalNationsPage(parent, year);
        if (target != null) {
            Element table = target.select("table.wikitable").first();
            if (table != null) {
                Element tbody = table.select("tbody").first();
                if (tbody != null) {
                    Elements rows = tbody.select("tr");
                    for (Element row: rows) {
                        Elements columns = row.select("td");
                        if (columns.size() >= medal) {
                            Element silverColumn = columns.get(columns.size() - medal);
                            if (Integer.parseInt(silverColumn.text()) >= threshold) {
                                Element name = row.select("a").first();
                                if (name != null) {
                                    silverNations.add(name.text());
                                }
                            }
                        }
                    }
                }
            }
        }
        return silverNations;
    }

    /**
     * Determines if the flag bearer of a specific country and year has died,
     * based on the provided document.
     *
     * @param parent The root document containing all relevant information.
     * @param year The year for which the flag bearer's status is to be checked.
     * @param country The country of the flag bearer whose status is to be determined.
     * @return A string status indicating whether the flag bearer has died,
     * additional details, or a message stating no information is available.
     */
    public static String didFlagBearerDie(Document parent, String year, String country) {
        Document flagBearer = getFlagBearerRoot(parent, year, country);
        HashMap<String, Document> roots = getFlagBearerPage(flagBearer);
        if (roots.isEmpty()) {
            return "No flagbearers known!";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Document> root: roots.entrySet()) {
            String result = getStatus(root.getKey(), root.getValue());
            if (!result.isEmpty()) {
                sb.append("\n").append(result).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Retrieves the status of an individual based on the content of an HTML document.
     * This method searches for specific elements in the provided HTML document and
     * determines if the individual has passed away or is still alive.
     *
     * @param name the name of the individual whose status is being determined
     * @param root the root document to search within, containing HTML content
     * @return a string indicating whether the individual has passed away
     * (with additional details if available)
     *         or if they are still alive; returns an empty string
     *         if no relevant information is found
     */
    public static String getStatus(String name, Document root) {
        Elements infoboxes = root.select("table.infobox");
        if (!infoboxes.isEmpty()) {
            for (Element infobox: infoboxes) {
                Element th = infobox.selectFirst("th:matchesOwn(^Died$)");
                if (th != null) {
                    Element next = th.nextElementSibling();
                    while (next != null && !next.tagName().equals("td")) {
                        next = next.nextElementSibling();
                    }
                    if (next != null) {
                        StringBuilder combined = new StringBuilder();
                        for (org.jsoup.nodes.Node node : next.childNodes()) {
                            if (node instanceof org.jsoup.nodes.TextNode textNode) {
                                String plainText = textNode.text();
                                if (!plainText.isEmpty()) {
                                    combined.append(plainText);
                                }
                            } else if (node instanceof Element element &&
                                    element.tagName().equals("a")) {
                                String aText = element.text();
                                if (!aText.isEmpty()) {
                                    combined.append(aText);
                                }
                            } else if (node instanceof Element element &&
                                    element.tagName().equals("i")) {
                                String iText = element.text();
                                if (!iText.isEmpty()) {
                                    combined.append(iText);
                                }
                                for (org.jsoup.nodes.Node elnode : element.childNodes()) {
                                    if (elnode instanceof org.jsoup.nodes.TextNode textNode) {
                                        String plainText = textNode.text();
                                        if (!plainText.isEmpty()) {
                                            combined.append(plainText);
                                        }
                                    } else if (elnode instanceof Element elem &&
                                            element.tagName().equals("a")) {
                                        String aText = elem.text();
                                        if (!aText.isEmpty()) {
                                            combined.append(aText);
                                        }
                                    }
                                }
                            }
                        }
                        return name + " passed away: " + combined.toString();
                    }
                } else {
                    return name + " is still alive.";
                }
            }
        }
        return "";
    }

    /**
     * Extracts and retrieves a mapping of flag bearers' names to their
     * corresponding Wikipedia pages
     * from the provided document representing a Wikipedia article.
     *
     * @param flagBearer A Document object representing a Wikipedia article that potentially contains
     *                   information about flag bearers in its infobox section.
     * @return A HashMap where the key is the name of the flag bearer (as a String) and the value is
     *         a Document object representing the Wikipedia page of the corresponding flag bearer.
     *         Returns an empty HashMap if no flag bearer information
     *         is found or if the input document is null.
     */
    public static HashMap<String, Document> getFlagBearerPage(Document flagBearer) {
        HashMap<String, Document> ret = new HashMap<>();
        if (flagBearer != null) {
            Element infobox = flagBearer.selectFirst("table.infobox");
            if (infobox != null) {
                Element tbody = infobox.selectFirst("tbody");
                if (tbody != null) {
                    Elements rows = tbody.select("tr");
                    for (Element row: rows) {
                        Element th = row.selectFirst("th");
                        if (th != null) {
                            if (th.text().contains("Flag bearer")) {
                                Element peopleRow = row.selectFirst("td");
                                if (peopleRow != null) {
                                    Elements people = peopleRow.select("a");
                                    if (!people.isEmpty()) {
                                        for (Element a: people) {
                                            String link = a.attr("href");
                                            if (!link.isEmpty()) {
                                                String names = a.text();
                                                ret.put(names, fetchPage("https://en.wikipedia.org"
                                                        + link));
                                            }
                                        }
                                    }
                                }
                            } else {
                                Element name = row.select("a").first();
                                if (name != null) {
                                    if (name.text().contains("Flag bearer")) {
                                        Element peopleRow = row.selectFirst("td");
                                        if (peopleRow != null) {
                                            Elements people = peopleRow.select("a");
                                            if (!people.isEmpty()) {
                                                for (Element a: people) {
                                                    String link = a.attr("href");
                                                    if (!link.isEmpty()) {
                                                        String names = a.text();
                                                        ret.put(names, fetchPage(
                                                                "https://en.wikipedia.org" + link)
                                                        );
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Retrieves the flag bearer's root document for a given country and year by parsing
     * the medal table of a Wikipedia page.
     *
     * @param parent the parent Document from which the method begins its search
     * @param year the year of the medal event
     * @param country the name of the country whose flag bearer document is to be retrieved
     * @return a Document object representing the Wikipedia page of the country's flag bearer,
     *         or null if the relevant document could not be found
     */
    public static Document getFlagBearerRoot(Document parent,String year, String country) {
        Document medalPage = getYearMedalNationsPage(parent, year);
        if (medalPage != null) {
            Element medalTableHeading = medalPage.select("h2").stream()
                .filter(el -> el.text().equalsIgnoreCase("Medal table"))
                .findFirst()
                .orElse(null);
            if (medalTableHeading == null) {
                medalTableHeading = medalPage.select("h3").stream()
                        .filter(el -> el.text().equalsIgnoreCase("Medal table"))
                        .findFirst()
                        .orElse(null);
            }
            if (medalTableHeading == null) {
                medalTableHeading = medalPage.select("h1").stream()
                        .filter(el -> el.text().equalsIgnoreCase("Medal table"))
                        .findFirst()
                        .orElse(null);
            }

            if (medalTableHeading != null) {
                Element ancestor = medalTableHeading.parent();
                if (ancestor != null) {
                    Element next = ancestor.nextElementSibling();
                    while (next != null && !next.tagName().equals("table")) {
                        next = next.nextElementSibling();
                    }
                    if (next != null) {
                        Element tBody = next.getElementsByTag("tbody").first();
                        Elements rows = null;
                        if (tBody != null) {
                            rows = tBody.select("tr");
                            for (Element row : rows) {
                                Element title = row.select("a").first();
                                if (title != null) {
                                    String name = title.text();
                                    if (name.equals(country)) {
                                        String link = title.attr("href");
                                        if (!link.isEmpty()) {
                                            return fetchPage("https://en.wikipedia.org" + link);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Extracts medal data for a specified year and writes it to a CSV file
     * in the given directory path.
     * If the provided home document is null, it fetches the necessary page data from a global URL.
     *
     * @param home          The Document object representing the home page. If null, the
     *                      method fetches the page automatically.
     * @param year          The year for which the medal data needs to be fetched.
     * @param directoryPath The directory path where the output CSV file will be saved.
     */
    public static void getAllMedals(Document home, String year, String directoryPath) {
        if (home == null) {
            home = fetchPage(Querybase.GLOBAL_URL);
        }
        if (home != null) {
            Document medalPage = getYearMedalNationsPage(home, year);
            if (medalPage != null) {
                Element table = medalPage.select("table.wikitable").first();
                if (table != null) {
                    Element tbody = table.select("tbody").first();
                    if (tbody != null) {
                        Elements rows = tbody.select("tr");
                        File folder = new File(directoryPath);
                        File outputCSV = new File(folder, "medallookup.csv");
                        try (BufferedWriter writer = new BufferedWriter(
                                new FileWriter(outputCSV))) {
                            writer.write("Country,Gold,Silver,Bronze");
                            writer.newLine();
                            for (Element row: rows) {
                                Elements columns = row.select("td");
                                if (columns.size() >= 3) {
                                    StringBuilder csvRow = new StringBuilder();
                                    Element name = row.select("a").first();
                                    if (name != null) {
                                        csvRow.append(name.text());
                                    } else {
                                        csvRow.append("Unknown");
                                    }
                                    for (int i = 4; i > 1; i--) {
                                        csvRow.append(",");
                                        Element medalColumn = columns.get(columns.size() - i);
                                        csvRow.append(medalColumn.text());
                                    }
                                    writer.write(csvRow.toString());
                                    writer.newLine();
                                }
                            }
                            System.out.println("Medallookup saved to: " +
                                    outputCSV.getAbsolutePath());
                        } catch (IOException e) {
                            System.err.println("Error writing CSV: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieves the Wikipedia page document for nations' medals of a specific year
     * by parsing the provided parent document. If the parent document is not
     * provided, it fetches the base page from a predefined global URL.
     *
     * @param parent The parent Document representing the initial page to parse.
     *               Can be null, in which case the method fetches the base page.
     * @param year   The year as a String for which the medals' nations page
     *               needs to be retrieved.
     * @return The Document representing the page for the specified year's medals'
     *         nations. Returns null if the page cannot be found or parsed.
     */
    public static Document getYearMedalNationsPage(Document parent, String year) {
        if (parent == null) {
            parent = WebParser.fetchPage(Querybase.GLOBAL_URL);
        }
        String link = "https://en.wikipedia.org";
        if (parent != null) {
            Element table = parent.select("table.wikitable").get(3);
            Element rows = table.select("tr").last();
            if (rows != null) {
                Element plainlist = rows.selectFirst(".plainlist");
                if (plainlist != null) {
                    Element list = plainlist.selectFirst("ul");
                    if (list != null) {
                        for (Element li : list.select("li")) {
                            Element aTag = li.selectFirst("a");
                            if (aTag != null) {
                                if (year.equals(aTag.text().trim())) {
                                    link = link + aTag.attr("href");
                                    return fetchPage(link);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Fetches the parent page based on the given description.
     *
     * @param description the description specifying the type of page to fetch.
     *                     Valid values are "summer" and "winter".
     *                     If the description is null or empty, the method returns null.
     * @return a Document representing the fetched parent page, or null if the description
     *         is invalid or the page cannot be fetched.
     */
    public static Document fetchParentPage(String description) {
        if (description == null || description.isEmpty()) {
            return null;
        }
        if (description.equals("summer")) {
            return WebParser.fetchPage(Querybase.GLOBAL_URL);
        } else if (description.equals("winter")) {
            Document parent = WebParser.fetchPage(Querybase.GLOBAL_URL);
            if (parent != null) {
                return fetchCurrentFromParent(parent, "summer");
            }
        }
        return null;
    }

    /**
     * Scrapes a set of country names from a given web page that start with a specified prefix.
     * The method extracts country names from a wiki-style sortable HTML table
     * and filters them based on the provided prefix.
     *
     * @param prefix The prefix string used to filter country names.
     *               Only countries whose names start with this prefix
     *               (case-insensitive) will be included.
     * @param page   A Jsoup Document object representing the web page to scrape data from.
     *               If null, a default page is fetched to perform the operation.
     * @return A HashSet containing country names that start with the specified prefix.
     *         If no countries match the prefix or the page is
     *         null/invalid, an empty set is returned.
     */
    public static HashSet<String> scrapeCountriesStartingWith(
            String prefix, Document page) {
        HashSet<String> countries = new HashSet<>();
        prefix = prefix.toLowerCase();
        if (page == null) {
            page = fetchPage(Querybase.GLOBAL_URL);
        }
        if (page != null) {
            Element table = page.select("table.sortable.wikitable").first();
            if (table != null) {
                Elements rows = table.select("tr");
                for (int i = 1; i < rows.size(); i++) {
                    Element row = rows.get(i);
                    Element firstColumn = row.select("td:first-child").first();
                    if (firstColumn !=  null) {
                        String country = firstColumn.text().toLowerCase();
                        if (country.startsWith(prefix)) {
                            countries.add(country);
                        }
                    }
                }
            }
        }
        return countries;
    }

    /**
     * Fetches the number of unique countries passed during an Olympic torch relay
     * for a given country and year based on the provided documents.
     *
     * This method processes an HTML document to extract relevant information
     * regarding torch relay details and matches it to the specified country and year.
     * If the provided document is null, it attempts to fetch it.
     *
     * @param parent The parent document from which the target document can be derived, if null.
     * @param target The HTML document containing the torch relay data to process, if available.
     * @param country The name of the country to match against the torch relay data.
     * @param year The reference year for filtering torch relay events.
     * @return The number of unique countries encountered during the parsing of the torch relay data,
     *         or a default minimum value of 2 if no data is found.
     */
    public static int fetchNumTorchPassed(Document parent, Document target, String country, String year) {
        if (target == null) {
            if (parent == null) {
                parent = fetchPage(Querybase.GLOBAL_URL);
            }
            if (parent != null) {
                target = fetchTorchRelayPage(parent);
            }
        }
        int maxSeen = 2;
        int yearInt = -1;
        try {
            yearInt = Integer.parseInt(year);
        } catch (NumberFormatException e) {
            return yearInt;
        }

        if (target != null) {
            Element table = target.select("table.sortable.wikitable").first();
            if (table != null) {
                Element tbody = table.select("tbody").first();
                if (tbody != null) {
                    Elements rows = tbody.select("tr");
                    for (Element row: rows) {
                        Element firstColumn = row.selectFirst("td");
                        if (firstColumn != null) {
                            Element rowIcon  = firstColumn.selectFirst("span.flagicon");
                            if (rowIcon != null) {
                                Element aTag = rowIcon.selectFirst("a");
                                if (aTag != null) {
                                    String title = aTag.attr("title");
                                    if (country.equals(title)) {
                                        Element yearTag = firstColumn.select("a").last();
                                        if (yearTag != null) {
                                            String[] text = yearTag.text().split("\\s+");
                                            boolean found = false;
                                            for (String s: text) {
                                                if (Helpers.isValidYear(s)) {
                                                    int year1Int = Integer.parseInt(s);
                                                    if (year1Int >= yearInt) {
                                                        found = true;
                                                        System.out.println("Checking " + year1Int);
                                                        break;
                                                    }
                                                }
                                            }
                                            if (found) {
                                                Element lastColumn = row.select("td").last();
                                                if (lastColumn != null) {
                                                    HashSet<String> linkExtensions = new HashSet<>();
                                                    for (org.jsoup.nodes.Node node :
                                                            lastColumn.childNodes()) {
                                                        if (node instanceof
                                                                Element element &&
                                                                element.tagName().equals("a")) {
                                                            String link = element.attr("href");
                                                            if (!link.isEmpty()) {
                                                                linkExtensions.add(link);
                                                            }
                                                        } else if (node instanceof
                                                                Element element &&
                                                                element.tagName().equals("i")) {
                                                            for (org.jsoup.nodes.Node elnode :
                                                                    element.childNodes()) {
                                                                if (elnode instanceof Element elem &&
                                                                        elem.tagName().equals("a")) {
                                                                    String link = elem.attr("href");
                                                                    if (!link.isEmpty()) {
                                                                        linkExtensions.add(link);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    HashSet<String> seenCountries = new HashSet<>();
                                                    for (String s: linkExtensions) {
                                                        Document cityPage = fetchPage("https://en.wikipedia.org" + s);
                                                        String countryName = fetchCountry(cityPage);
                                                        if (countryName != null && !countryName.isEmpty()) {
                                                            seenCountries.add(countryName);
                                                        }
                                                    }
                                                    if (seenCountries.size() > maxSeen) {
                                                        maxSeen = seenCountries.size();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return maxSeen;
    }

    private static String fetchCountry(Document cityPage) {
        if (cityPage != null) {
            Element countryTh = cityPage.select("th").stream()
                    .filter(el -> el.text().equalsIgnoreCase("country"))
                    .findFirst()
                    .orElse(null);
            if (countryTh != null) {
                Element parent = countryTh.parent();
                if (parent != null) {
                    Element a = parent.select("a").last();
                    if (a != null) {
                        return a.text();
                    } else {
                        Element next = countryTh.nextElementSibling();
                        while (next != null && !(next.tagName().equals("td"))) {
                            next = next.nextElementSibling();
                        }
                        if (next != null) {
                            return next.text();
                        }
                    }
                }
                Element next = countryTh.nextElementSibling();
                while (next != null && !(next.tagName().equals("td"))) {
                    next = next.nextElementSibling();
                }
                if (next != null) {
                    return next.text();
                }
            }
            Element infoBox = cityPage.select("table.infobox").first();
            if (infoBox != null) {
                Element countryType = cityPage.select("a").stream()
                    .filter(el -> el.text().equalsIgnoreCase("prefecture"))
                    .findFirst()
                    .orElse(null);
                if (countryType == null) {
                    countryType = cityPage.select("a").stream()
                        .filter(el -> el.text().equalsIgnoreCase("sovereign state"))
                        .findFirst()
                        .orElse(null);
                } else {
                    return "Japan";
                }

                if (countryType != null) {
                    Element parent = countryType.parent();
                    Element parent2 = null;
                    if (parent != null) {
                        parent2 = parent.parent();
                    }
                    if (parent2 != null) {
                        Element a = parent2.select("a").last();
                        if (a != null) {
                            return a.text();
                        } else {
                            Element next = parent.nextElementSibling();
                            while (next != null && !(next.tagName().equals("td"))) {
                                next = next.nextElementSibling();
                            }
                            if (next != null) {
                                return next.text();
                            }
                        }
                    }
                    if (parent != null) {
                        Element next = parent.nextElementSibling();
                        while (next != null && !(next.tagName().equals("td"))) {
                            next = next.nextElementSibling();
                        }
                        if (next != null) {
                            return next.text();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Fetches the document of the Wikipedia page for the "List of Olympic torch relays".
     * It extracts the relevant link from the sidebar of the provided parent document
     * and retrieves the corresponding page content.
     *
     * @param parent The parent document from which the sidebar will be extracted.
     *               If null, the global Querybase URL will be used to fetch the parent document.
     * @return The document of the "List of Olympic torch relays" page if the link is found and valid;
     *         null if the parent document is null, the sidebar or link is not found, or the document cannot be fetched.
     */
    public static Document fetchTorchRelayPage(Document parent) {
        if (parent == null) {
            parent = fetchPage(Querybase.GLOBAL_URL);
        }
        if (parent != null) {
            Element sidebar = parent.select("table.sidebar").first();
            if (sidebar != null) {
                Element torchLink = sidebar.selectFirst("a[title=List of Olympic torch relays]");
                if (torchLink != null) {
                    String link = torchLink.attr("href");
                    return WebParser.fetchPage("https://en.wikipedia.org" + link);
                }
            }
        }
        return null;
    }
}
