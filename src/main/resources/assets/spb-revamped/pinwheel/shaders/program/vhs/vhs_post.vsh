// Declared here because Veil 1.21.1 drops #veil:buffer directives reached through #include
#veil:buffer veil:camera VeilCamera

uniform float DistortionStrength;

out vec2 texCoord;

void main() {
    // Veil 1.21.1's screen quad has no vertex buffer: it is a single fullscreen triangle generated from gl_VertexID
    // (same as veil:blit_screen). 1.20.1 read a "Position" attribute from a quad mesh instead.
    vec2 uv = vec2(gl_VertexID & 1, gl_VertexID & 2);
    gl_Position = vec4(uv * vec2(3.0) - vec2(1.0), 0.0, mix(1.0, 0.70, DistortionStrength));
    texCoord = uv * vec2(1.5);
}
