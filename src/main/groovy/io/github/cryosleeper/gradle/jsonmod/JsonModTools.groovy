package io.github.cryosleeper.gradle.jsonmod

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath

class JsonModTools {

    static void applyDiff(DocumentContext parsedInput, String diff, boolean isAdding, boolean isDeleting) {
        JsonNode node = new ObjectMapper().readTree(diff)
        node.fields().forEachRemaining {
            try {
                if (isAdding) {
                    makeSureEntryExists(parsedInput, it.key)
                }
                applySingleChange(parsedInput, it.key, it.value, isDeleting)
            } catch (Exception e) {
                System.err.println("Modification failed for key ${it.key} with $e")
            }
        }
    }

    static void makeSureEntryExists(DocumentContext input, String path) {
        path = path.replace(".['", ".").replace("']", "").replace("['", "")
        String key = path.split("[.]").last()
        String parent = path.substring(0, path.lastIndexOf(key))
        if (parent.endsWith(".")) {
            parent = parent.substring(0, parent.length()-1)
        }
        parent = convertToJsonPath(parent)
        if (!JsonPath.isPathDefinite(path)) {
            System.err.println("Path ${path} is not definite, can't create an entry if required!")
        }
        input.put(parent, key, "")
    }

    static void applySingleChange(DocumentContext input, String key, JsonNode value, boolean isDeleting) {
        String modifiedKey = convertToJsonPath(key)
        switch (value.nodeType) {
            case JsonNodeType.NULL:
                if (isDeleting)
                    input.delete(modifiedKey)
                else
                    System.err.println("Deletion failed for key ${key} - deletion forbidden!"); break
            case JsonNodeType.BOOLEAN: input.set(modifiedKey, value.booleanValue()); break
            case JsonNodeType.NUMBER: input.set(modifiedKey, value.numberValue()); break
            case JsonNodeType.STRING: input.set(modifiedKey, value.textValue()); break
            default: System.err.println("Modification failed for key ${key} due to using an unsupported value type")
        }
    }

    private static String convertToJsonPath(String key) {
        String modifiedKey
        if (key.isEmpty()) {
            modifiedKey = "\$"
        } else if (!key.startsWith("\$")) {
            modifiedKey = "\$.$key"
        } else {
            modifiedKey = key
        }
        modifiedKey
    }
}
