// This class loads custom filters from a JSON config file
package com.woops.filters;

import com.woops.filters.Filter;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FilterLoader {

    // Load filters from config.json
    public static List<Filter> loadFiltersFromConfig(String configPath) {
        List<Filter> filters = new ArrayList<>();
        try {
            String content = Files.readString(Paths.get(configPath));
            JSONObject json = new JSONObject(content);
            JSONArray filterClasses = json.getJSONArray("filters");

            for (int i = 0; i < filterClasses.length(); i++) {
                String className = filterClasses.getString(i);
                try {
                    Class<?> cls = Class.forName(className);
                    Object instance = cls.getDeclaredConstructor().newInstance();
                    if (instance instanceof Filter) {
                        filters.add((Filter) instance);
                        System.out.println(" Loaded filter: " + className);
                    } else {
                        System.err.println("❌ " + className + " does not implement Filter interface");
                    }
                } catch (Exception e) {
                    System.err.println(" Failed to load filter class: " + className);
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.err.println("Failed to read config file: " + e.getMessage());
        }
        return filters;
    }
}
