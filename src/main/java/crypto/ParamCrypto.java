package crypto;


public class ParamCrypto implements IParamCrypto{
    @Override
    public String encryptParam(String unEncrtptData) {
        return unEncrtptData;
    }

    @Override
    public String decryptParam(String inEncryptData) {
        return inEncryptData;
    }
}
