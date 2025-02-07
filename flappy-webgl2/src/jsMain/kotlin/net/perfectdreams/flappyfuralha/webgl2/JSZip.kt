package net.perfectdreams.flappyfuralha.webgl2

import js.promise.Promise

@JsModule("jszip")
@JsNonModule
external class JSZip {
    // data = InputFileFormat
    fun loadAsync(data: dynamic): Promise<JSZip>

    fun forEach(callback: (relativePath: String, file: JSZipObject) -> (Unit))
}

external interface JSZipObject {
    val name: String
    val dir: Boolean

    fun async(type: String): Promise<dynamic>
}