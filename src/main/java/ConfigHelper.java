import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by Tobias on 04.02.2017.
 */
public class ConfigHelper {


    public static Config from(Path workingDirectory) throws IOException {
        Path configFile = Paths.get(workingDirectory.toString(), "config.json");
        if (!configFile.toFile().exists()) {
            throw new IOException("Config file " + configFile + " does not exist!");
        }

        Map<String, String> configMap = JsonHelper.readJson(configFile.toString());

        return new Config(configMap, workingDirectory);
    }
}
