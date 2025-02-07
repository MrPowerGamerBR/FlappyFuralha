package net.perfectdreams.flappyfuralha.webgl2

import web.html.Image

class VirtualFileSystem(
    val images: Map<String, Image>,
    val files: Map<String, ByteArray>
)