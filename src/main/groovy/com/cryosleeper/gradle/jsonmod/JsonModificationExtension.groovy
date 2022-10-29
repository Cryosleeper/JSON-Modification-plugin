package com.cryosleeper.gradle.jsonmod

abstract class JsonModificationExtension {

    abstract boolean allowDelete = false

    List<Modification> modifications = new LinkedList()

    def modification(Closure closure) {
        Modification modification = new Modification()
        closure.delegate = modification
        closure()
        modifications.add(modification)
    }
}

class Modification {
    File input
    List<File> diffs
    File output
}
