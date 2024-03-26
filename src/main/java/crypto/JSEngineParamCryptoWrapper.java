package crypto;

import utils.BurpConfig;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JSEngineParamCryptoWrapper implements IParamCrypto {
    private IParamCrypto wrapped;
    private ScriptEngine engine;
    private String encryptScript;
    private String decryptScript;

    public JSEngineParamCryptoWrapper(IParamCrypto iParamCrypto) {
        this.wrapped = iParamCrypto;
        // 初始化ScriptEngine
        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName("JavaScript");

        BurpConfig burpConfig = new BurpConfig();
        // 优化配置加载逻辑，允许同时设置加密和解密脚本
        this.encryptScript = burpConfig.getProperty("encryptScript");
        this.decryptScript = burpConfig.getProperty("decryptScript");
    }

    @Override
    public String encryptParam(String unEncryptData) {
        try {
            engine.eval(encryptScript);
            Invocable invocable = (Invocable) engine;
            return (String) invocable.invokeFunction("encrypt", unEncryptData);
        } catch (ScriptException | NoSuchMethodException e) {
            throw new RuntimeException("Error during encryption: " + e.getMessage(), e);
        }
    }

    @Override
    public String decryptParam(String inEncryptData) {
        try {
            engine.eval(decryptScript);
            Invocable invocable = (Invocable) engine;
            return (String) invocable.invokeFunction("decrypt", inEncryptData);
        } catch (ScriptException | NoSuchMethodException e) {
            throw new RuntimeException("Error during decryption: " + e.getMessage(), e);
        }
    }
}
