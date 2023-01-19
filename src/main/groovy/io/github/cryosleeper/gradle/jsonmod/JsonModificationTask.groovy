package io.github.cryosleeper.gradle.jsonmod

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.spi.json.JacksonJsonProvider
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class JsonModificationTask extends DefaultTask {
    @Input
    abstract Property<Boolean> getIsDeleting()
    @Input
    abstract Property<Boolean> getIsAdding()
    @Internal
    List<Modification> modifications

    JsonModificationTask() {}

    @TaskAction
    def modify() {
        modifications.forEach {
            Configuration configuration = Configuration.defaultConfiguration().jsonProvider(new JacksonJsonProvider())
            DocumentContext parsedInput = JsonPath.using(configuration).parse(it.input.text)
            it.diffs.forEach { diff ->
                JsonModTools.applyDiff(parsedInput, diff.text, isAdding.get(), isDeleting.get())
            }
            String result = parsedInput.jsonString()
            it.output.delete()
            it.output.write(result)
            println result
        }
    }
}
