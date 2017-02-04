package config;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by Tobias on 04.02.2017.
 */
public class Config {

    private final Path fontPath;
    private final Float textSize;

    public BufferedImage backgroundImage;
    public Color backgroudnColor;
    public Color textColor;
    private Font font;

    public Config(Map<String, String> configMap, Path root) {
        backgroudnColor = Color.decode(configMap.get("background_color"));
        textColor = Color.decode(configMap.get("text_color"));
        try {
            Path imagePath = Paths.get(root.toString(), configMap.get("background_image"));
            if (imagePath.toFile().exists()) {
                backgroundImage = ImageIO.read(imagePath.toFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        fontPath = Paths.get(root.toString(), configMap.get("font"));
        textSize = Float.valueOf(configMap.get("text_size"));
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, fontPath.toFile()).deriveFont(textSize);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Font getFont() {
        return font;
    }
}
