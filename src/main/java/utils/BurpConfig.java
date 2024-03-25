package utils;

import java.io.*;
import java.util.Properties;

public class BurpConfig {
    private Properties properties;

    public BurpConfig() {
        properties = new Properties();
        // 20240326 修改获取配置的代码，从工作目录获取，跟Burpsuite同个目录，change the way to get the properties, obtain the properties from the current workspace, in the same directory of the Burpsuite
        String currentDir = System.getProperty("user.dir");
        File configFile = new File(currentDir, "burp.config");
        try (InputStream is = new FileInputStream(configFile)) {
            properties.load(is);
        } catch (FileNotFoundException e) {
            System.out.println("Property file 'burp.config' not found in the current directory: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error reading 'burp.config' from the current directory: " + e.getMessage());
        }
    }
    public String getProperty(String key){
        return properties.getProperty(key);

    }
}
