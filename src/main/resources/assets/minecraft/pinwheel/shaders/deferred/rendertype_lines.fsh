// Declared here because Veil 1.21.1 drops #veil:buffer directives reached through #include
#veil:buffer veil:camera VeilCamera
#include veil:deferred_buffers

uniform vec4 ColorModulator;

in vec4 vertexColor;

void main() {
    fragAlbedo = vertexColor * ColorModulator;
}
