package com.cryosleeper.gradle.jsonmod

abstract class JsonModificationExtension {

    abstract boolean allowDelete = false

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
