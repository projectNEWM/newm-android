package io.projectnewm.shared.util

// TODO: Make this not suck.
expect class Logger(
    className: String,
) {
    fun log(msg: String)
}

fun printLogD(className: String?, message: String) {
    println("$className: $message")
}