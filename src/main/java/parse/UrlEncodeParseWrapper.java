package parse;

import java.util.*;


public class UrlEncodeParseWrapper implements IParamParse{
    private String originalRoot;
    public ArrayList<String> paramList = new ArrayList<>();
    public Map<String, List<String>> paramMap = new HashMap<>();
    private IParamParse wrapped;
    public UrlEncodeParseWrapper(IParamParse wrapped){
        this.wrapped = wrapped;
    }
    @Override
    public ArrayList<String> generateParamList(Object requestData) {
        this.paramList = wrapped.generateParamList(requestData);
        try{
            this.paramList.clear();
            this.paramMap.clear();
            this.originalRoot = requestData.toString();
            traverseAndHighlight();
        }catch (Exception e){
            System.out.println("UrlParseParm Error!");
        }
        return this.paramList;
    }

    @Override
    public Map<String, List<String>> generateParamMap(Object requestData) {
        if (this.paramMap.isEmpty()){
            generateParamList(requestData);
        }
        return this.paramMap;
    }

    private void traverseAndHighlight(){
        String modifiedRoot = this.originalRoot;
        String[] pairs = modifiedRoot.split("&"); // 分割成键值对
        for (int i = 0; i < pairs.length; i++) {
            String[] keyValuePair = pairs[i].split("=", 2); // 分割成键和值
            paramMap.put(keyValuePair[0].replaceAll("\\s+", ""), Collections.singletonList(keyValuePair[1].replaceAll("\\s+", "")));
            System.out.println(paramMap);
            // 构建输出字符串，只在当前遍历的键值对上加星号
            StringBuilder output = new StringBuilder();
            for (int j = 0; j < pairs.length; j++) {
                if (j > 0) {
                    output.append("&").toString();
                }
                if (i == j) {
                    // 当前键值对，值加星号
                    output.append(keyValuePair[0]).append("=").append("*").append(keyValuePair[1]).append("*").toString();
                } else {
                    // 其它键值对保持不变
                    output.append(pairs[j]);
                }
            }
            String result = output.toString().replaceAll("\\s+", "");
            this.paramList.add(result);
        }

    }
}
