package util;

import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tobias on 04.02.2017.
 */
public class JsonHelper {

    public static Map<String, String> readJson(String jsonFile) throws IOException {
        JsonReader jsonReader = new JsonReader(new FileReader(jsonFile));

        jsonReader.beginObject();

        Map<String, String> jsonMap = new HashMap<>();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            String value = jsonReader.nextString();
            jsonMap.put(name, value);
        }

        jsonReader.endObject();
        jsonReader.close();

        return jsonMap;
    }

}
