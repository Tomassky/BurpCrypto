package crypto;

import utils.BurpConfig;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESParamCryptoWrapper implements IParamCrypto {
    private static String AES_KEY; // 16字节的密钥
    private static String AES_INIT_VECTOR; // 16字节的IV
    private IParamCrypto wrapped;
    public AESParamCryptoWrapper(IParamCrypto iParamCrypto){
        this.wrapped = iParamCrypto;
        BurpConfig burpConfig = new BurpConfig();
        if (burpConfig.getProperty("AES_KEY").isEmpty() & burpConfig.getProperty("AES_INIT_VECTOR").isEmpty()){
            System.out.println("AES_KEY and AES_INIT_VECTOR is NULL");
        }
        AES_KEY = burpConfig.getProperty("AES_KEY");
        AES_INIT_VECTOR = burpConfig.getProperty("AES_INIT_VECTOR");
    }

    @Override
    public String encryptParam(String unEncryptData) {
        try {
            IvParameterSpec iv = new IvParameterSpec(AES_INIT_VECTOR.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(AES_KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(unEncryptData.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String decryptParam(String inEncryptData) {
        try {
            IvParameterSpec iv = new IvParameterSpec(AES_INIT_VECTOR.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(AES_KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(inEncryptData));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
