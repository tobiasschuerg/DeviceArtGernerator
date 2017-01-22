import com.google.gson.stream.JsonReader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by Tobias on 22.01.2017.
 */
public class Main {

    private static final int LINE_SPACING_PX = 30;
    public static final Color BACKGROUND_COLOR = Color.decode("#ffde31");
    public static final Color TEXT_COLOR = Color.decode("#386C0B");
    public static final float FONT_SIZE = 120f;
    private static File fontFile;

    public static void main(String[] args) throws IOException {

        Files.walk(Paths.get("metadata/"), 1)
                .forEach(path -> {
                    if (Files.isDirectory(path) && Files.exists(Paths.get(path.toString(), "screenshot_titles.json"))) {
                        System.out.println("Processing " + path.toString());
                        fontFile = new File(path.toFile() + "/font.ttf");
                        List<Path> pngs = listFiles(path);
                        try {
                            Map<String, String> titles = readJson(path.toFile() + "/screenshot_titles.json");
                            frameWithCaption(pngs, titles);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private static Map<String, String> readJson(String jsonFile) throws IOException {
        JsonReader jsonReader = new JsonReader(new FileReader(jsonFile));

        jsonReader.beginObject();

        Map<String, String> titleMap = new HashMap<>();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            String value = jsonReader.nextString();
            titleMap.put(name, value);
        }

        jsonReader.endObject();
        jsonReader.close();

        return titleMap;
    }

    private static void frameWithCaption(List<Path> pngs, Map<String, String> titles) {
        pngs.stream()
                .map(path -> {
                    try {
                        BufferedImage bufferedImage = ImageIO.read(path.toFile());
                        System.out.println("height: " + bufferedImage.getHeight());
                        String title = titles.get(path.toFile().getName());
                        if (title == null) {
                            throw new NullPointerException(path.toFile().getName() + " has no text");
                        }
                        return process(bufferedImage, title);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .forEach(Main::save);
    }

    private static void save(BufferedImage bufferedImage) {
        try {
            File outputfile = new File("output/image" + System.currentTimeMillis() + ".png");
            ImageIO.write(bufferedImage, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage process(BufferedImage old, String title) {
        int w = old.getWidth();
        int h = old.getHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        float padPercentage = 0.025f;

        Graphics2D g2d = img.createGraphics();
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, w, h);
        final int screenX = (int) (w * padPercentage);
        final int moveY = (int) (7 * h * padPercentage);
        int screenWidth = (int) (w * (1 - 2 * padPercentage));
        int screenHeight = (int) (h * (1 - 2 * padPercentage));
        g2d.drawImage(old, screenX, moveY, screenWidth, screenHeight, null);

        g2d.setPaint(TEXT_COLOR);

        // InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("fonts/Raleway-Medium.ttf");
        Font font = null;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(FONT_SIZE);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        drawStringMultiLine(g2d, title, w, moveY, fm);
        g2d.dispose();
        return img;
    }

    public static void drawStringMultiLine(Graphics2D g, String text, int w, int screenY, FontMetrics fm) {
        String[] lines = text.split("\n");
        int y = screenY / 2 - ((lines.length - 1) * fm.getHeight() / 2);
        for (String line : lines) {
            int x = w / 2 - (fm.stringWidth(line) / 2);
            g.drawString(line, x, y);
            y = y + fm.getHeight() + LINE_SPACING_PX;
        }
    }

    private static List<Path> listFiles(Path path) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.png");

        List<Path> files = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(path)) {
            paths.forEach(filePath -> {
                System.out.println(filePath);
                if (Files.isRegularFile(filePath)) {
                    if (matcher.matches(filePath)) {
                        System.out.println("# " + filePath);
                        files.add(filePath);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

}
