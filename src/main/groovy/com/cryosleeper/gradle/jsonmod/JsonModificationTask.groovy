package com.cryosleeper.gradle.jsonmod

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
        if (!key.startsWith("\$")) {
            modifiedKey = "\$.$key"
        } else {
            modifiedKey = key
        }
        modifiedKey
    }
}
