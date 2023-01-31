package io.github.cryosleeper.gradle.jsonmod

import org.gradle.api.provider.Property

abstract class JsonModificationExtension {

    abstract Property<Boolean> getAllowDelete()
    abstract Property<Boolean> getAllowAdd()

    JsonModificationExtension() {
        allowAdd.convention(false)
        allowDelete.convention(false)
    }

    protected List<Modification> modifications = new LinkedList()

    Modification modification(Closure closure) {
        Modification modification = new Modification()
        closure.delegate = modification
        closure()
        modifications.add(modification)
        modification
    }
}

class Modification {
    File input
    List<File> diffs
    File output
}
