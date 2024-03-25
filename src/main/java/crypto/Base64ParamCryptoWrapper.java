package crypto;

import java.util.Base64;
public class Base64ParamCryptoWrapper implements IParamCrypto{
    private IParamCrypto wrapped;
    public Base64ParamCryptoWrapper(IParamCrypto paramCrypto) {
        this.wrapped = paramCrypto;
    }

    @Override
    public String encryptParam(String unEncryptData){
        String EncrtptDataJson = wrapped.encryptParam(unEncryptData);
        return Base64.getEncoder().encodeToString(EncrtptDataJson.getBytes());
    }

    @Override
    public String decryptParam(String inEncryptData) {
        byte[] decodedBytes = Base64.getDecoder().decode(inEncryptData);
        String decodedString = new String(decodedBytes);
        return wrapped.decryptParam(decodedString);
    }
}
