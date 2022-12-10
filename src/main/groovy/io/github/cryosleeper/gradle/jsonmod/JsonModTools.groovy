package io.github.cryosleeper.gradle.jsonmod

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath

class JsonModTools {

    static void applyDiff(DocumentContext parsedInput, String diff, boolean isAdding, boolean isDeleting) {
        JsonNode node = new ObjectMapper().readTree(diff)
        node.fields().forEachRemaining {
            String key = JsonPath.compile(it.key).path
            try {
                if (isAdding) {
                    makeSureEntryExists(parsedInput, key)
                }
                applySingleChange(parsedInput, key, it.value, isDeleting)
            } catch (Exception e) {
                System.err.println("Modification failed for key ${key} with $e")
            }
        }
    }

    static void makeSureEntryExists(DocumentContext input, String path) {
        String formattedPath = JsonPath.compile(path).path
        String key = formattedPath.substring(formattedPath.lastIndexOf("['"))
        String possibleArrayIndex = key.find("\\[\\d+\\]\$")
        String parent = formattedPath.replace(key, "")
        System.err.println("Path parent is $parent")
        System.err.println("Path key is $key")

        if (possibleArrayIndex != null) {
            String arrayKey = formattedPath.substring(0, formattedPath.length() - possibleArrayIndex.length())
            Integer index = possibleArrayIndex.replace("[","").replace("]","").toInteger()
            System.err.println("Path to array is $arrayKey")
            List arrayNode = input.read(arrayKey)
            arrayNode[index] = ""
            input.set(arrayKey, arrayNode)
        } else {
            String cleanKey = key.replace("['", "").replace("']", "")
            input.put(parent, cleanKey, "")
        }
    }

    static void applySingleChange(DocumentContext input, String key, JsonNode value, boolean isDeleting) {
        switch (value.nodeType) {
            case JsonNodeType.MISSING: System.err.println("Modification failed for key ${key} due to using a missing value type"); break
            case JsonNodeType.NULL:
                if (isDeleting)
                    input.delete(key)
                else
                    System.err.println("Deletion failed for key ${key} - deletion forbidden!"); break
            case JsonNodeType.ARRAY: input.set(key, ((ArrayNode)value).toList()); break
            default: input.set(key, value); break
        }
    }
}
