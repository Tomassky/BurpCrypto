/***
 * SSRF-King
 * Author: zoid
 * Description:
 * SSRF Plugin for burp that Automates SSRF Detection in all of the Request
 */

package burp;


import java.io.PrintWriter;
import java.util.*;

import cn.iinti.sekiro3.open.Bootstrap;
import crypto.*;
import parse.*;
import utils.BurpConfig;
import utils.ReplaceUtils;


/***
 * This is the main extension class.
 * @author User
 *
 */
public class BurpExtender implements IBurpExtender, IExtensionStateListener {
    private PrintWriter stdout;
    public IBurpExtenderCallbacks callback;
	public IExtensionHelpers helpers;
	public ArrayList<String> paramBodyList;
	public ArrayList<String> paramUrlList;
	public ArrayList<String> paramHeaderList;
	public Map<String, List<String>> paramBodyMap = new HashMap<>();
	public Map<String, List<String>> paramHeaderMap = new HashMap<>();
	public Map<String, List<String>> paramUrlMap = new HashMap<>();

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
    	this.stdout = new PrintWriter(callbacks.getStdout(), true);
    	this.callback=callbacks;
    	helpers=callbacks.getHelpers();
        callbacks.setExtensionName("BurpCrypto");

		/* 可以实现启动和卸载sekiro服务，但是sekiro服务无法被正常访问到
        try {
            Bootstrap.main(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
		callbacks.registerExtensionStateListener(this::extensionUnloaded);*/
        callbacks.registerHttpListener(this::processHttpMessage);
    }
	@Override
	public void extensionUnloaded() {
		Bootstrap.shutdown(); // 停止sekiro服务
		System.out.println("Sekiro service has been successfully shutdown.");
	}

	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse iHttpRequestResponse) {

		// 处理请求响应内容，请求包转换为3个Map，响应包转换为2个Map
		if (messageIsRequest && toolFlag == IBurpExtenderCallbacks.TOOL_REPEATER) {
			IRequestInfo iRequestInfo = this.helpers.analyzeRequest(iHttpRequestResponse);
			IParamParse iParamParseHeader = new ParamParseImpl();
			IParamParse iParamParseUrl = new ParamParseImpl();

			iParamParseHeader = new HeaderParseWrapper(iParamParseHeader);
			iParamParseUrl = new UrlEncodeParseWrapper(iParamParseUrl);

			// Header -> List<String>、Url -> String
			this.paramHeaderMap = iParamParseHeader.generateParamMap(iRequestInfo.getHeaders());
			this.paramUrlList = iParamParseUrl.generateParamList(iRequestInfo.getUrl().getQuery());
			this.paramUrlMap = iParamParseUrl.generateParamMap(iRequestInfo.getUrl().getQuery());
			this.stdout.println(this.paramHeaderMap);
			this.stdout.println(this.paramUrlMap);

			byte contentType = iRequestInfo.getContentType();
			byte[] reqByte = iHttpRequestResponse.getRequest();
			// Body -> String
			String requestData = helpers.bytesToString(Arrays.copyOfRange(reqByte, iRequestInfo.getBodyOffset(), reqByte.length));
			IParamParse iParamParse = new ParamParseImpl();
			if (contentType == 4){
				iParamParse = new JsonParseWrapper(iParamParse);
			} else if (contentType == 1) {
				iParamParse = new UrlEncodeParseWrapper(iParamParse);
			}
			this.paramBodyMap = iParamParse.generateParamMap(requestData);
			this.paramBodyList = iParamParse.generateParamList(requestData);
			this.stdout.println(this.paramBodyMap);

		} else if(!messageIsRequest && toolFlag == IBurpExtenderCallbacks.TOOL_REPEATER){
			IResponseInfo iResponseInfo = this.helpers.analyzeResponse(iHttpRequestResponse.getResponse());
			IParamParse iParamParseHeader = new ParamParseImpl();

			iParamParseHeader = new HeaderParseWrapper(iParamParseHeader);
			this.paramHeaderMap = iParamParseHeader.generateParamMap(iResponseInfo.getHeaders());
			this.stdout.println(this.paramHeaderMap);

			byte[] reqByte = iHttpRequestResponse.getResponse();
			String responseData = helpers.bytesToString(Arrays.copyOfRange(reqByte, iResponseInfo.getBodyOffset(), reqByte.length));
			IParamParse iParamParse = new ParamParseImpl();
			if (iResponseInfo.getInferredMimeType().toLowerCase().contains("json")){
				iParamParse = new JsonParseWrapper(iParamParse);
				this.paramBodyMap = iParamParse.generateParamMap(responseData);
			} else if (iResponseInfo.getInferredMimeType().toLowerCase().contains("text")) {
				this.paramBodyMap.put("all", Collections.singletonList(responseData));
			}
			this.stdout.println(this.paramBodyMap);

		}


		BurpConfig burpConfig = new BurpConfig();
		String cryptoForRequest = burpConfig.getProperty("cryptoForRequest");
		String cryptoForResponse = burpConfig.getProperty("cryptoForResponse");
		if (cryptoForRequest.contains("true") && messageIsRequest && toolFlag == IBurpExtenderCallbacks.TOOL_REPEATER){
			String returnValue = dynamicCryptoRequestProcess(iHttpRequestResponse, this.paramBodyMap, this.paramHeaderMap, this.paramUrlMap);
			byte[] newRequest = dynamicReplaceRequestProcess(returnValue, iHttpRequestResponse, this.paramBodyMap, this.paramHeaderMap, this.paramUrlMap);
			iHttpRequestResponse.setRequest(newRequest);
		} else if (cryptoForResponse.contains("true") && !messageIsRequest && toolFlag == IBurpExtenderCallbacks.TOOL_REPEATER) {
			String returnValue = dynamicCryptoResponseProcess(this.paramBodyMap, this.paramHeaderMap);
			byte[] newResponse = dynamicReplaceResponseProcess(returnValue, iHttpRequestResponse,paramBodyMap, paramHeaderMap);
			iHttpRequestResponse.setResponse(newResponse);
		}

	}

	// 20240325 动态替换请求中的值
	private byte[] dynamicReplaceRequestProcess(String replaceValues, IHttpRequestResponse iHttpRequestResponse, Map<String, List<String>> paramBodyMap, Map<String, List<String>> paramHeaderMap, Map<String, List<String>> paramUrlMap){
		IRequestInfo iRequestInfo = this.helpers.analyzeRequest(iHttpRequestResponse);
		List<String> headers = iRequestInfo.getHeaders();
		byte[] reqByte = iHttpRequestResponse.getRequest();
		String body = helpers.bytesToString(Arrays.copyOfRange(reqByte, iRequestInfo.getBodyOffset(), reqByte.length));
		String bodyString = new String();

		BurpConfig burpConfig = new BurpConfig();
		String[] cryptoRequestString = burpConfig.getProperty("cryptoRequestString").split(",");

		for (String param : cryptoRequestString) {
			String[] parts = param.split("\\(");
			String type = parts[0];
			String key = parts[1].substring(0, parts[1].length() - 1);

			String matchValues = new String();
			switch (type) {
				case "url":
					matchValues = String.join("", paramUrlMap.get(key));
					ReplaceUtils.replaceUrl(headers, matchValues, replaceValues);
					break;
				case "header":
					matchValues = String.join("", paramHeaderMap.get(key));
					ReplaceUtils.replaceHeaders(headers, matchValues, replaceValues);
					break;
				case "body":
					matchValues = String.join("", paramBodyMap.get(key));
					bodyString = ReplaceUtils.replaceBody(body, matchValues, replaceValues);
					break;
			}
		}
		if (bodyString.isEmpty()){
			return this.helpers.buildHttpMessage(headers, body.getBytes());
		}
        return this.helpers.buildHttpMessage(headers, bodyString.getBytes());
	}

	// 20240325 动态替换响应中的值
	private byte[] dynamicReplaceResponseProcess(String replaceValues, IHttpRequestResponse iHttpRequestResponse, Map<String, List<String>> paramBodyMap, Map<String, List<String>> paramHeaderMap){
		IResponseInfo iResponseInfo = this.helpers.analyzeResponse(iHttpRequestResponse.getResponse());
		List<String> headers = iResponseInfo.getHeaders();
		byte[] reqByte = iHttpRequestResponse.getResponse();
		String body = helpers.bytesToString(Arrays.copyOfRange(reqByte, iResponseInfo.getBodyOffset(), reqByte.length));
		String bodyString = new String();

		BurpConfig burpConfig = new BurpConfig();
		String[] cryptoRequestString = burpConfig.getProperty("cryptoResponseString").split(",");
		for (String param : cryptoRequestString) {
			String[] parts = param.split("\\(");
			String type = parts[0];
			String key = parts[1].substring(0, parts[1].length() - 1);

			String matchValues = new String();
			switch (type) {
				case "header":
					matchValues = String.join("", paramHeaderMap.get(key));
					ReplaceUtils.replaceHeaders(headers, matchValues, replaceValues);
					break;
				case "body":
					matchValues = String.join("", paramBodyMap.get(key));
					bodyString = ReplaceUtils.replaceBody(body, matchValues, replaceValues);
					break;
			}
		}
		return this.helpers.buildHttpMessage(headers, bodyString.getBytes());

	}

	// 20240325 动态加密请求中的值
	private String dynamicCryptoRequestProcess(IHttpRequestResponse iHttpRequestResponse, Map<String, List<String>> paramBodyMap, Map<String, List<String>> paramHeaderMap, Map<String, List<String>> paramUrlMap){
		IRequestInfo iRequestInfo = this.helpers.analyzeRequest(iHttpRequestResponse.getRequest());
		byte[] reqByte = iHttpRequestResponse.getRequest();
		String body = helpers.bytesToString(Arrays.copyOfRange(reqByte, iRequestInfo.getBodyOffset(), reqByte.length));

		String returnValue = new String();

		BurpConfig burpConfig = new BurpConfig();
		String cryptoRequestMethod = burpConfig.getProperty("cryptoRequestMethod");
		String[] cryptoRequestParams = burpConfig.getProperty("cryptoRequestParams").split(",");

		IParamCrypto iParamCrypto = new ParamCrypto();
		if ("Base64".equals(cryptoRequestMethod)) {
			iParamCrypto = new Base64ParamCryptoWrapper(iParamCrypto);
		} else if ("AES".equals(cryptoRequestMethod)) {
			iParamCrypto = new AESParamCryptoWrapper(iParamCrypto);
		}else if ("Sekiro".equals(cryptoRequestMethod)){
			iParamCrypto = new SekiroParamCryptoWrapper(iParamCrypto);
		}
		StringBuilder allParams = new StringBuilder();
		for (String param : cryptoRequestParams) {
			String[] parts = param.split("\\(");
			String type = parts[0];
			String key = parts[1].substring(0, parts[1].length() - 1);
			List<String> values = null;
			switch (type) {
				case "url":
					values = paramUrlMap.get(key);
					break;
				case "header":
					values = paramHeaderMap.get(key);
					break;
				case "body":
					values = paramBodyMap.get(key);
					break;
				case "all":
					values = Collections.singletonList(body);
					break;
				case "text":
					values = Collections.singletonList(key);
			}
			if (values != null) {
				allParams.append(String.join("", values));
			}
		}
		try {
			returnValue = iParamCrypto.encryptParam(allParams.toString());
		} catch (Exception e) {
			System.out.println(e);
		}
		return returnValue;
	}

	// 20240325 动态解密响应中的值
	private String dynamicCryptoResponseProcess(Map<String, List<String>> paramBodyMap, Map<String, List<String>> paramHeaderMap){
		String returnValue = new String();
		BurpConfig burpConfig = new BurpConfig();
		String cryptoResponseMethod = burpConfig.getProperty("cryptoResponseMethod");
		String[] cryptoReParams = burpConfig.getProperty("cryptoResponseParams").split(",");

		IParamCrypto iParamCrypto = new ParamCrypto();
		if ("Base64".equals(cryptoResponseMethod)) {
			iParamCrypto = new Base64ParamCryptoWrapper(iParamCrypto);
		} else if ("AES".equals(cryptoResponseMethod)) {
			iParamCrypto = new AESParamCryptoWrapper(iParamCrypto);
		}else if ("Sekiro".equals(cryptoResponseMethod)){
			iParamCrypto = new SekiroParamCryptoWrapper(iParamCrypto);
		}

		StringBuilder allParams = new StringBuilder();
		for (String param : cryptoReParams) {
			String[] parts = param.split("\\(");
			String type = parts[0];
			String key = parts[1].substring(0, parts[1].length() - 1);

			List<String> values = null;
			switch (type) {
				case "header":
					values = paramHeaderMap.get(key);
					break;
				case "body":
					values = paramBodyMap.get(key);
					break;
			}
			if (values != null) {
				allParams.append(String.join("", values));
			}
		}
		try {
			returnValue = iParamCrypto.encryptParam(allParams.toString());
		} catch (Exception e) {
			System.out.println(e);
		}
		return returnValue;

	}
}