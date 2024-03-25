package parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IParamParse {

    ArrayList<String> generateParamList(Object requestData);
    Map<String, List<String>> generateParamMap(Object requestData);


}
