package parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParamParseImpl implements IParamParse{
    public ArrayList<String> paramList = new ArrayList<>();
    public Map<String, List<String>> paramMap = new HashMap<>();
    @Override
    public ArrayList<String> generateParamList(Object requestData) {
        return this.paramList;
    }

    @Override
    public Map<String, List<String>> generateParamMap(Object requestData) {
        if (this.paramMap.isEmpty()){
            generateParamList(requestData);
        }
        return this.paramMap;
    }
}
