#version 330 core

layout (location = 0) in vec4 vertex; // <vec2 position, vec2 texCoords>

out vec2 TexCoords;

uniform mat4 model;
uniform mat4 projection;

void main()
{
    // Using xy also works but that's due to our vertex matching the UV coordinates perfectly lol
    TexCoords = vertex.zw;
    gl_Position = projection * model * vec4(vertex.x, vertex.y, 0.0, 1.0);
}