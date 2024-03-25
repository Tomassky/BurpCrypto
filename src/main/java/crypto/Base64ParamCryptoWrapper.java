package crypto;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Base64;
public class Base64ParamCryptoWrapper implements IParamCrypto{
    private IParamCrypto wrapped;

    // 构造函数，传入要被包装的对象
    public Base64ParamCryptoWrapper(IParamCrypto paramCrypto) {
        this.wrapped = paramCrypto;
    }

    @Override
    public String encryptParam(String unEncryptData){
        // 首先，使用原始加密方法进行加密
        String EncrtptDataJson = wrapped.encryptParam(unEncryptData);
        // 然后，对加密结果进行Base64编码
        return Base64.getEncoder().encodeToString(EncrtptDataJson.getBytes());
    }

    @Override
    public String decryptParam(String inEncryptData) {
        // 首先，对输入的数据进行Base64解码
        byte[] decodedBytes = Base64.getDecoder().decode(inEncryptData);
        String decodedString = new String(decodedBytes);
        // 然后，使用原始解密方法进行解密
        return wrapped.decryptParam(decodedString);
    }
}
