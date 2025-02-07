#version 300 es // Required to be set to es
precision highp float; // Required for es

in vec2 TexCoords;
out vec4 color;

uniform sampler2D image;

void main()
{
    color = texture(image, TexCoords);
}  