import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * A utility class for handling operations related to downloading, processing,
 * and managing flag images from various sources.
 */
public class SrcGetter {
    /**
     * Downloads all the flags provided in the list of flag source URLs and saves
     * them into a directory named "flags"
     * located in the parent directory of the current working directory. If the
     * directory does not exist, it is created.
     *
     * @param flagSrcs a list of URLs pointing to the sources of the flags to be downloaded.
     *                 Should not be null or empty.
     * @return the absolute path to the "flags" directory where the flags have been
     * downloaded. Returns an empty string if the list is null or empty.
     */
    public static String downloadAllFlags(ArrayList<String> flagSrcs) {
        if (flagSrcs == null || flagSrcs.isEmpty()) {
            return "";
        }

        File javaDir = new File(System.getProperty("user.dir")); //Get current directory
        File parentDir = javaDir.getParentFile();
        File flagsDirectory = new File(parentDir, "flags");
        if (!flagsDirectory.exists()) {
            flagsDirectory.mkdir(); //make new directory called flags if it doesn't already exist
        }
        System.out.println("Downloading flags. This might take a while...");
        for (String flagSrc : flagSrcs) {
            String countryName = Helpers.extractCountryName(flagSrc);
            downloadFlag(flagSrc, new File(flagsDirectory, countryName).getPath());
        }
        System.out.println("Flags downloaded!");
        return flagsDirectory.getAbsolutePath();
    }

    /**
     * Downloads a flag image from the specified URL and saves it to the specified file path
     * as a PNG file.
     *
     * @param flagUrl the URL pointing to the source of the flag image. Should not be null or empty.
     * @param filePath the file path (excluding the extension) where the flag image will be saved.
     *                 Should not be null or empty.
     */
    public static void downloadFlag(String flagUrl, String filePath) {
        String fileName = filePath + ".png";
        try (InputStream in = new URL(flagUrl).openStream();
             FileOutputStream out = new FileOutputStream(fileName)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            System.err.println("Failed to download " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Creates a collage of PNG flag images from a specified directory, arranging them in a grid
     * structure, and saves the resulting collage as a PNG file named "flagcollage.png" in
     * the same directory.
     * The provided flag size determines the dimensions of each resized flag within the collage.
     *
     * @param filepath the path to the directory containing PNG flag images. Should not
     *                 be null or empty.
     * @param flagSize the desired width and height (in pixels) for each resized flag in
     *                 the collage. Must be greater than zero.
     */
    public static void createCollage(String filepath, int flagSize) {
        File folder = new File(filepath);
        File[] flagSrcs = folder.listFiles((_, name) -> name.toLowerCase().endsWith(".png"));

        if (flagSrcs == null || flagSrcs.length == 0) {
            System.err.println("No flag images found in the directory.");
            return;
        }

        int flagSrcCount = flagSrcs.length;
        int gridSize = (int) Math.ceil(Math.sqrt(flagSrcCount));

        int collageWidth = gridSize * flagSize;
        int collageHeight = gridSize * flagSize;

        BufferedImage collage = new BufferedImage(collageWidth,
                collageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = collage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, collageWidth, collageHeight);

        int xIndex = 0;
        int yIndex = 0;
        for (File flagSrc : flagSrcs) {
            try {
                BufferedImage currentFlag = ImageIO.read(flagSrc);
                BufferedImage resizedFlag = resizeImage(currentFlag, flagSize, flagSize);
                g.drawImage(resizedFlag, xIndex * flagSize, yIndex * flagSize, null);
                xIndex++;
                if (xIndex >= gridSize) {
                    xIndex = 0;
                    yIndex++;
                }
            } catch (IOException e) {
                System.err.println("Failed to read: " + flagSrc.getName());
            }
        }

        g.dispose();
        try {
            File out = new File(folder, "flagcollage.png");
            ImageIO.write(collage, "png", out);
            System.out.println("Collage saved to: " + out.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save collage: " + e.getMessage());
        }
    }

    /**
     * Converts flag images in a specified directory to a CSV file where each row represents
     * a flag image. Each row contains the image file name (without extension) followed by
     * pixel color values in hexadecimal format for each row of the image.
     *
     * Only processes PNG files in the provided directory that do not contain "flagcollage"
     * in their file names.
     *
     * If no valid images are found in the directory, an error message is printed to the console.
     * If the CSV file generation is successful, its file path is printed to the console.
     *
     * @param directoryPath the path to the directory containing the flag images to be processed.
     *                      Should not be null or empty.
     */
    public static void convertFlagsToCSV(String directoryPath) {
        File folder = new File(directoryPath);
        File[] flagFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png")
                && !name.toLowerCase().contains("flagcollage"));

        if (flagFiles == null || flagFiles.length == 0) {
            System.err.println("No flag images found in the directory.");
            return;
        }

        File outputCSV = new File(folder, "cheatsheet.csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputCSV))) {
            for (File flagFile : flagFiles) {
                String countryName = flagFile.getName().split(".png")[0];
                BufferedImage image = ImageIO.read(flagFile);

                int width = image.getWidth();
                int height = image.getHeight();

                StringBuilder csvRow = new StringBuilder(countryName);

                for (int y = 0; y < height; y++) {
                    csvRow.append(",");
                    for (int x = 0; x < width; x++) {
                        int rgb = image.getRGB(x, y) & 0xFFFFFF;
                        csvRow.append(String.format("%06X", rgb)).append(" ");
                    }
                    csvRow.setLength(csvRow.length() - 1);
                }
                writer.write(csvRow.toString());
                writer.newLine();
            }
            System.out.println("CSV file saved: " + outputCSV.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing CSV: " + e.getMessage());
        }
    }

    /**
     * Resizes the given image to the specified width and height.
     *
     * @param image the original image to be resized. Must not be null.
     * @param width the desired width of the resized image. Must be greater than zero.
     * @param height the desired height of the resized image. Must be greater than zero.
     * @return a new BufferedImage object representing the resized image with
     * the specified dimensions.
     */
    public static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }
}
