package io.github.cryosleeper.gradle.jsonmod

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
                JsonModTools.applyDiff(parsedInput, diff.text, isAdding, isDeleting)
            }
            String result = parsedInput.jsonString()
            it.output.delete()
            it.output.write(result)
            println result
        }
    }
}
