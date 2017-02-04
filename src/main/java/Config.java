import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by Tobias on 04.02.2017.
 */
public class Config {

    BufferedImage backgroundImage;
    Color backgroudnColor;
    Color textColor;

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
    }
}
