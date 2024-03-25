package utils;

import java.io.*;
import java.util.Properties;

public class BurpConfig {
    private Properties properties;

    public BurpConfig() {
        properties = new Properties();
        ClassLoader classLoader = BurpConfig.class.getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("burp.config")) {
            if (is != null) {
                properties.load(is);
            } else {
                throw new FileNotFoundException("Property file 'burp.config' not found in the classpath");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public String getProperty(String key){
        return properties.getProperty(key);

    }
}
