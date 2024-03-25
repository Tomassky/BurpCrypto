package parse;

import java.util.*;

public class HeaderParseWrapper implements IParamParse{
    public List<String> originalRoot;
    public ArrayList<String> paramList = new ArrayList<>();
    public Map<String, List<String>> paramMap = new HashMap<>();
    public IParamParse wrapped;
    public HeaderParseWrapper(IParamParse wrapped){
        this.wrapped = wrapped;
    }
    @Override
    public ArrayList<String> generateParamList(Object requestData) {
        this.paramList = wrapped.generateParamList(requestData);
        return this.paramList;
    }

    @Override
    public Map<String, List<String>> generateParamMap(Object requestData){
        this.paramMap = wrapped.generateParamMap(requestData);
        try{
            @SuppressWarnings("unchecked") // 用于抑制编译器警告
            List<String> requestDataHeader = (List<String>) requestData;
            this.paramList.clear();
            this.paramMap.clear();
            this.originalRoot = requestDataHeader;
            traverseAndHighlight();
        }catch (Exception e){
            System.out.println("HeaderParseParm Error!");
        }
        return this.paramMap;
    }

    private void traverseAndHighlight(){
        List<String> modifyRoot = this.originalRoot;
        for (int i = 1; i < modifyRoot.size(); i++) {
            String[] keyValuePair = modifyRoot.get(i).split(":", 2); // 分割成键和值
            paramMap.put(keyValuePair[0].replaceAll("\\s+", ""), Collections.singletonList(keyValuePair[1].replaceAll("\\s+", "")));
        }
    }

}
