// Declared here because Veil 1.21.1 drops #veil:buffer directives reached through #include
#veil:buffer veil:camera VeilCamera
#include veil:color_utilities
#include veil:blend

in vec2 texCoord;

uniform sampler2D AlbedoSampler;
uniform sampler2D CompatibilitySampler;
uniform sampler2D LightSampler;

out vec4 fragColor;

void main() {
    float albedoAlpha = texture(AlbedoSampler, texCoord).a;
    vec4 diffuse = texture(LightSampler, texCoord);
    vec4 compatibility = texture(CompatibilitySampler, texCoord);
    fragColor = vec4(blend(diffuse, compatibility), albedoAlpha + compatibility.a * (1.0 - albedoAlpha));
//        fragColor = vec4(compatibility.rgb, 1);
}