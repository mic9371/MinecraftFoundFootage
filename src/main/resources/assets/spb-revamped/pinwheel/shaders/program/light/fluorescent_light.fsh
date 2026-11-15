// Declared here because Veil 1.21.1 drops #veil:buffer directives reached through #include
#veil:buffer veil:camera VeilCamera
uniform sampler2D Sampler0;
uniform mat4 viewRix;
uniform mat4 orthoMatrix;

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;
in vec4 viewPos;

out vec4 fragColor;

void main() {
	fragColor = vec4(1, 1, 1, 1);
}