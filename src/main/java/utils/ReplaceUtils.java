package utils;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class ReplaceUtils {
    public static void replaceHeaders(List<String> headers, String matchValues, String replaceValues){
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if (header.contains(matchValues)) {
                headers.set(i, header.replace(matchValues, replaceValues));
            }
        }
    }
    public static String replaceBody(String body, String matchValues, String replaceValues){
        if (body.contains(matchValues)) {
            body = body.replace(matchValues, replaceValues);
        }
        return body;
    }
    // 20240325代码中默认考虑请求url中这个参数是存在的
    public static void replaceUrl(List<String> headers, String matchValues, String replaceValues){
        String url = headers.get(0);
        if (url.contains(matchValues)){
            url = url.replace(matchValues, replaceValues);
        }
        headers.set(0, url);
    }
}
