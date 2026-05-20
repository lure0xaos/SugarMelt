package utils

import java.io.File

fun File.toLinkedString(): String =
    toURI().toURL().toExternalForm().replace("file:/", "file:///")