package org.jenkinsci.plugins.accurevclient

class AccurevException : RuntimeException {
    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}
