#version 460 core

layout(location = 0) uniform ivec2 u_resolution;
layout(location = 1) uniform float u_pitch;
layout(location = 2) uniform float u_yaw;
layout(location = 3) uniform vec3 u_cameraPosition;
layout(location = 4) uniform double u_randomSeed;

#include "common.glsl"
#include "voxels.glsl"

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 texcoord = gl_FragCoord.xy / u_resolution;
    texcoord = texcoord * 2.0 - 1.0; // center the camera in the screen
    texcoord.x *= float(u_resolution.x) / u_resolution.y; // aspect ratio correction
    
    vec3 color = vec3(0.0);
    
    vec3 dir = normalize(vec3(texcoord, 1.0));
    
    dir = (vec4(dir, 1.0) * rotation3d(vec3(-1.0, 0.0, 0.0), radians(u_pitch))).xyz;
    dir = (vec4(dir, 1.0) * rotation3d(vec3(0.0, 1.0, 0.0), radians(u_yaw))).xyz;
    
    color = mix(vec3(0.8), vec3(0.15, 0.35, 1.0), pow(clamp(dir.y, 0.0, 1.0), 1.0 / 2.0));
    
    float startDist = rayPlaneIntersection(vec3(0.0), dir, vec3(0.0, 1.0, 0.0), 100.0);
    vec3 startPos = dir * startDist;
    
    Hit hit = raytraceDDA(startPos, dir, 200, false);
    if(hit.success) {
        color = getVoxelColor(hit) * (hit.normal.y * 0.25 + 0.75) * 0.15;
        
        vec3 sunVector = normalize(vec3(0.5, 1.0, 0.25));
        
        Hit shadowHit = raytraceDDA(hit.pos + hit.normal * 0.0001, sunVector, 10, false);
        if(!shadowHit.success) color *= 5.0 + clamp(dot(hit.normal, sunVector), 0.0, 1.0);
        
    } /*else {
        hit = raytraceDDA(dir * 200.0, dir, 200, false);
        if(hit.success) color = getVoxelColor(hit);
    }*/
    
    color = pow(color, vec3(1.0 / 2.2));
    //color = dir;
    
    fragColor = vec4(color, 1.0);
}