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
                        switch (it.value.nodeType) {
                            case JsonNodeType.NULL:
                                if (isDeleting)
                                    parsedInput.delete("\$.${it.key}")
                                else
                                    System.err.println("Deletion failed for key ${it.key} - deletion forbidden!"); break
                            case JsonNodeType.BOOLEAN: parsedInput.set("\$.${it.key}", it.value.booleanValue()); break
                            case JsonNodeType.NUMBER: parsedInput.set("\$.${it.key}", it.value.numberValue()); break
                            case JsonNodeType.STRING: parsedInput.set("\$.${it.key}", it.value.textValue()); break
                            default: System.err.println("Modification failed for key ${it.key} due to using an unsupported value type")
                        }
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
}
