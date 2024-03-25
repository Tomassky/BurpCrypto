package parse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

public class JsonParseWrapper implements IParamParse{
    private final ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode originalRoot = null;
    private  IParamParse wrapped;
    public ArrayList<String> paramList = new ArrayList<>();
    public Map<String, List<String>> paramMap = new HashMap<>();

    public JsonParseWrapper(IParamParse wrapped){
        this.wrapped = wrapped;
    }

    public ArrayList<String> generateParamList(Object requestData) {
        this.paramList = wrapped.generateParamList(requestData);
        try{
            this.originalRoot = objectMapper.readTree(requestData.toString());
            this.paramList.clear();
            this.paramMap.clear();
            List<String> path = new LinkedList<>();
            traverseAndHighlight(this.originalRoot, path);
        }catch (Exception e){
            System.out.println("JsonParseParm Error!");
        }
        return this.paramList;
    }

    public Map<String, List<String>> generateParamMap(Object requestData){
        if (this.paramMap.isEmpty()){
            generateParamList(requestData);
        }
        return this.paramMap;
    }

    private void traverseAndHighlight(JsonNode node, List<String> path) throws JsonProcessingException {
        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                List<String> newPath = new LinkedList<>(path);
                newPath.add(fieldName);
                traverseAndHighlight(node.get(fieldName), newPath);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                List<String> newPath = new LinkedList<>(path);
                newPath.add("[" + i + "]");
                traverseAndHighlight(node.get(i), newPath);
            }
        } else {
            highlightSingleValue(path);
        }
    }

    private void highlightSingleValue(List<String> path) throws JsonProcessingException {
        JsonNode modifiedRoot = this.originalRoot.deepCopy();
        JsonNode currentNode = modifiedRoot;
        for (int i = 0; i < path.size() - 1; i++) {
            String segment = path.get(i);
            if (segment.startsWith("[")) {
                int index = Integer.parseInt(segment.substring(1, segment.length() - 1));
                currentNode = currentNode.get(index);
            } else {
                currentNode = currentNode.get(segment);
            }
        }

        String lastSegment = path.get(path.size() - 1);
        if (lastSegment.startsWith("[")) {
            int index = Integer.parseInt(lastSegment.substring(1, lastSegment.length() - 1));
            addValue(this.paramMap, path.get(0), currentNode.get(index).asText());
            ((ArrayNode) currentNode).set(index, objectMapper.createArrayNode().textNode("*" + currentNode.get(index).asText() + "*"));
        } else {
            addValue(this.paramMap, lastSegment,currentNode.get(lastSegment).asText());
            ((ObjectNode) currentNode).put(lastSegment, "*" + currentNode.get(lastSegment).asText() + "*");
        }

        this.paramList.add(objectMapper.writeValueAsString(modifiedRoot));
    }

    public static void addValue(Map<String, List<String>> map, String key, String value) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }
}
