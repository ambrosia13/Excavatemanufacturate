#version 460 core

layout(location = 0) uniform ivec2 u_resolution;
layout(location = 1) uniform float u_pitch;
layout(location = 2) uniform float u_yaw;
layout(location = 3) uniform vec3 u_cameraPosition;

#include "common.glsl"
#include "voxels.glsl"

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 texcoord = gl_FragCoord.xy / u_resolution;
    vec3 color = vec3(0.0);
    
    vec3 dir = normalize(vec3(texcoord - 0.5, 0.5));
    //dir = vec3(dir.z, dir.y, -dir.x);
    
    dir = (vec4(dir, 1.0) * rotation3d(vec3(-1.0, 0.0, 0.0), u_pitch)).xyz;
    dir = (vec4(dir, 1.0) * rotation3d(vec3(0.0, 1.0, 0.0), u_yaw)).xyz;
    
    color = mix(vec3(0.8), vec3(0.15, 0.35, 1.0), pow(clamp(dir.y, 0.0, 1.0), 1.0 / 2.0));
    
    Hit hit = raytraceDDA(vec3(0.0), dir, 50, false);
    if(hit.success) {
        color = getVoxelColor(hit) * (hit.normal.y * 0.25 + 0.75);
        
    }
    
    color = pow(color, vec3(1.0 / 2.2));
    //color = dir;
    
    fragColor = vec4(color, 1.0);
}