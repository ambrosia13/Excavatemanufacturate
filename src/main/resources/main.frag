#version 460 core

layout(location = 0) uniform ivec2 u_resolution;
layout(location = 1) uniform float u_pitch;
layout(location = 2) uniform float u_yaw;
layout(location = 3) uniform vec3 u_cameraPosition;
layout(location = 4) uniform double u_randomSeed;
layout(location = 5) uniform usampler3D u_worldTexture;

ivec3 worldTextureSize;

#include "common.glsl"
#include "voxels.glsl"

layout(location = 0) out vec4 fragColor;

void main() {
    worldTextureSize = textureSize(u_worldTexture, 0);
    
    vec2 texcoord = gl_FragCoord.xy / u_resolution;
    texcoord = texcoord * 2.0 - 1.0; // center the camera in the screen
    texcoord.x *= float(u_resolution.x) / u_resolution.y; // aspect ratio correction
    
    vec3 color = vec3(0.0);
    
    vec3 dir = normalize(vec3(texcoord, 1.0));
    
    dir = (vec4(dir, 1.0) * rotation3d(vec3(-1.0, 0.0, 0.0), radians(u_pitch))).xyz;
    dir = (vec4(dir, 1.0) * rotation3d(vec3(0.0, 1.0, 0.0), radians(u_yaw))).xyz;
    
    color = mix(vec3(0.8), vec3(0.15, 0.35, 1.0), pow(clamp(dir.y, 0.0, 1.0), 1.0 / 2.0));

    float distToLowerBoundingPlane = rayPlaneIntersection(vec3(0.0), dir, vec3(0.0, 1.0, 0.0), 70.0);
    float distToUpperBoundingPlane = rayPlaneIntersection(vec3(0.0), dir, vec3(0.0, 1.0, 0.0), 100.0);
    
    vec3 startPos = dir * max(0.0, distToUpperBoundingPlane);
    vec3 endPos = dir * max(0.0, distToLowerBoundingPlane);
    
    if(distToUpperBoundingPlane > distToLowerBoundingPlane) {
        vec3 temp = startPos;
        
        startPos = endPos;
        endPos = temp;
    }
    
    startPos = vec3(0.0);
    endPos = dir * 1000.0;
    
    Hit hit = raytraceDDA(startPos, endPos, 150, false);
    if(hit.success) {
        color = getVoxelColor(hit);
        
        vec3 ambient = vec3(hit.normal.y * 0.4 + 0.6) * 0.25;
        vec3 direct = vec3(0.0);
        
        // Shadows
        vec3 sunVector = normalize(vec3(0.5, 0.8, 0.25));
        
        float shadowFactor = 0.0;
        vec3 shadowPos = hit.pos + hit.normal * 0.001;
        
        const int shadowSamples = 1;
        for(int i = 0; i < shadowSamples; i++) {
            Hit shadowHit = raytraceDDA(shadowPos, shadowPos + generateCosineVector(sunVector, 0.0), 25, false);
            if (!shadowHit.success) shadowFactor += 1.0 / shadowSamples;
        }
        direct += clamp(dot(hit.normal, sunVector), 0.0, 1.0) * shadowFactor;
        
        vec3 totalLighting = ambient + direct;
        color *= totalLighting;
    }
    
    color = tanh(color);
    color = pow(color, vec3(1.0 / 2.2));
    
    //color = texelFetch(u_worldTexture, ivec3(ivec2(gl_FragCoord.xy * 0.1) % worldTextureSize.xy, 0), 0).r != 0u ? vec3(0.0) : vec3(1.0);
    
    fragColor = vec4(color, 1.0);
}