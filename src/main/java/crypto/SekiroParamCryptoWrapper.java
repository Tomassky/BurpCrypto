package crypto;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
public class SekiroParamCryptoWrapper implements IParamCrypto{
    private final IParamCrypto wrapped;

    public SekiroParamCryptoWrapper(IParamCrypto iParamCrypto) {
        this.wrapped = iParamCrypto;
    }

    @Override
    public String encryptParam(String unEncryptData) {
        return sendRequest(unEncryptData, "encrypt");
    }

    @Override
    public String decryptParam(String inEncryptData) {
        return sendRequest(inEncryptData, "decrypt");
    }

    private String sendRequest(String data, String action) {
        try {
            URL url = new URL("http://127.0.0.1:5612/business-demo/invoke?group=sekiro&action=" + action + "&text=" + data);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    HashMap<String, Object> responseMap = objectMapper.readValue(response.toString(), HashMap.class);
                    return (String) responseMap.get("encryptText");
                }
            } else {
                System.out.println("GET request not worked for action: " + action);
            }
        } catch (Exception e) {
            System.err.println("Error during " + action + " operation: " + e.getMessage());
        }
        return null;
    }
}
