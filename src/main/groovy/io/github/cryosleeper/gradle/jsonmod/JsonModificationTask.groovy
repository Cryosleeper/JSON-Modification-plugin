package io.github.cryosleeper.gradle.jsonmod

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class JsonModificationTask extends DefaultTask {
    @Internal
    boolean isDeleting
    @Internal
    boolean isAdding
    @Internal
    List<Modification> modifications

    JsonModificationTask() {}

    @TaskAction
    def modify() {
        modifications.forEach {
            DocumentContext parsedInput = JsonPath.parse(it.input.text)
            it.diffs.forEach { diff ->
                JsonNode node = new ObjectMapper().readTree(diff.text)
                node.fields().forEachRemaining {
                    try {
                        if (isAdding) {
                            makeSureEntryExists(parsedInput, it.key)
                        }
                        applySingleChange(parsedInput, it.key, it.value)
                    } catch (Exception e) {
                        System.err.println("Modification failed for key ${it.key} with $e")
                    }
                }
            }
            String result = parsedInput.jsonString()
            it.output.delete()
            it.output.write(result)
            println result
        }
    }

    void makeSureEntryExists(DocumentContext input, String path) {
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

    void applySingleChange(DocumentContext input, String key, JsonNode value) {
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

    private String convertToJsonPath(String key) {
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
