import config.Config;
import config.ConfigHelper;
import util.JsonHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by Tobias on 22.01.2017.
 */
public class Main {

    private static final int LINE_SPACING_PX = 30;

    private static Config config;


    public static void main(String[] args) throws IOException {
        Path workingDirectory = Paths.get(args[0]);
        if (!workingDirectory.toFile().exists()) {
            throw new IOException(workingDirectory + " does not exist");
        }

        config = ConfigHelper.from(workingDirectory);

        Files.walk(workingDirectory, 1)
                .forEach(path -> {
                    if (Files.isDirectory(path) && Files.exists(Paths.get(path.toString(), "screenshot_titles.json"))) {
                        System.out.println("Processing > " + path.toString());
                        List<Path> pngs = listFiles(path);
                        try {
                            Map<String, String> titles = JsonHelper.readJson(path.toFile() + "/screenshot_titles.json");
                            frameWithCaption(pngs, titles);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
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
        final int w = old.getWidth();
        final int h = old.getHeight();
        final BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2d = img.createGraphics();

        float padPercentage = 0.025f;

        g2d.setColor(config.backgroudnColor);
        g2d.fillRect(0, 0, w, h);

        BufferedImage background = config.backgroundImage.getSubimage(0, 0, w, h);
        g2d.drawImage(background, 0, 0, null);

        final int screenX = (int) (w * padPercentage);
        final int imageMarginTop = (int) (7 * h * padPercentage);
        int screenWidth = (int) (w * (1 - 2 * padPercentage));
        int screenHeight = (int) (h * (1 - 2 * padPercentage));
        g2d.drawImage(old, screenX, imageMarginTop, screenWidth, screenHeight, null);

        g2d.setPaint(config.textColor);

        g2d.setFont(config.getFont());
        FontMetrics fm = g2d.getFontMetrics();
        drawStringMultiLine(g2d, title, w, imageMarginTop, fm);
        g2d.dispose();
        return img;
    }

    public static void drawStringMultiLine(Graphics2D g, String text, int w, int imageMarginTop, FontMetrics fm) {
        String[] lines = text.split("\n");
        int halfLineHeight = fm.getHeight() / 5 * 2;

        g.drawLine(0, imageMarginTop / 2, w, imageMarginTop / 2);

        int y = imageMarginTop / 2 + halfLineHeight - ((lines.length - 1) * halfLineHeight * 2);
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
