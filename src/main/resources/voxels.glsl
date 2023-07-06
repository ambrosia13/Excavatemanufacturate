#include "noise.glsl"

/*
		public static final int AIR = 0;
		public static final int GRASS = 1;
		public static final int STONE = 2;
		public static final int WATER = 3;

*/
const int ID_GRASS = 1;
const int ID_DIRT = 20;
const int ID_STONE = 2;
const int ID_WATER = 3;
const int ID_LOG = 4;
const int ID_LEAVES = 5;

struct Hit {
    bool success;

    vec3 pos;
    vec3 normal;

    int id;
};

bool outOfVoxelBounds(in ivec3 voxelPos) {
    return modNoWrap(voxelPos, worldTextureSize) != voxelPos;
}

bool evaluateHit(inout Hit hit, in ivec3 voxelPos) {
    if(outOfVoxelBounds(voxelPos)) {
        hit.success = false;
        return hit.success;
    }
    
    uint worldSample = texelFetch(u_worldTexture, voxelPos, 0).r;
    
    hit.success = worldSample != 0u;
    hit.id = int(worldSample);
    
    return hit.success;
}

Hit raytraceDDA(vec3 startPos, vec3 endPos, int raytraceLength, bool renderLeaves) {
    Hit hit;
    hit.pos = vec3(0.0);
    hit.success = false;

//    if((rayPos + 10.0 * rayDir).y + u_cameraPosition.y > 90.0 && rayDir.y > 0.0) {
//        return hit;
//    }

    vec3 rayPos = startPos;
    vec3 rayDir = normalize(endPos - startPos);
    
    rayPos += u_cameraPosition;

    vec3 stepSizes = 1.0 / abs(rayDir);
    vec3 stepDir = sign(rayDir);
    vec3 nextDist = (stepDir * 0.5 + 0.5 - fract(rayPos)) / rayDir;

    ivec3 voxelPos = ivec3(rayPos);
    vec3 currentPos = rayPos;

    for(int i = 0; i < raytraceLength; i++) {
        float closestDist = min(nextDist.x, min(nextDist.y, nextDist.z));

        currentPos += rayDir * closestDist;

        vec3 stepAxis = vec3(lessThanEqual(nextDist, vec3(closestDist)));

        voxelPos += ivec3(stepAxis * stepDir);

        nextDist -= closestDist;
        nextDist += stepSizes * stepAxis;

        hit.normal = stepAxis;

        if(outOfVoxelBounds(voxelPos)) continue;
        if(evaluateHit(hit, voxelPos)) {
            hit.pos = currentPos - u_cameraPosition;
            hit.normal *= -stepDir;
            break;
        }
    }

    return hit;
}

vec3 getVoxelColor(Hit hit) {
    switch(hit.id) {
        case ID_GRASS: {
                           return mix(vec3(0.6, 0.4, 0.2), vec3(0.3, 1.0, 0.25), step(0.8, fract(hit.pos.y - 0.001 * hit.normal.y + fract(u_cameraPosition).y))) * 0.5;
                       }
        case ID_DIRT: return vec3(1.0, 0.75, 0.5) * 0.1;
        case ID_STONE: return vec3(0.5);
        case ID_WATER: return vec3(0.0, 0.25, 0.5);
        case ID_LOG: return vec3(0.4, 0.2, 0.1);
        case ID_LEAVES: return vec3(0.3, 0.9, 0.4) * 0.5;
        default: return vec3(1.0, 0.0, 0.0);
    }
}