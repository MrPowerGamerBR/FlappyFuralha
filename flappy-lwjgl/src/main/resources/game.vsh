#version 330 core

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec2 aUV;
layout (location = 2) in vec2 aBlendColor;

out vec2 vUV;

uniform mat4 uProjection;

void main() {
    vUV = aUV;
    gl_Position = uProjection * vec4(aPosition, 0.0, 1.0);
}