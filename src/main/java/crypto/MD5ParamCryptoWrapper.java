package crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5ParamCryptoWrapper implements IParamCrypto {
    private final IParamCrypto wrapped;
    public MD5ParamCryptoWrapper(IParamCrypto iParamCrypto) {
        this.wrapped = iParamCrypto;
    }
    @Override
    public String encryptParam(String unEncryptData) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(unEncryptData.getBytes());
            byte[] digest = md.digest();
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 encryption algorithm not found");
        }
    }

    @Override
    public String decryptParam(String inEncryptData) {
        throw new UnsupportedOperationException("MD5 is irreversible and cannot decrypt");
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
