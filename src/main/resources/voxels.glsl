#include "noise.glsl"

const int ID_GRASS = 1;
const int ID_DIRT = 2;
const int ID_STONE = 3;
const int ID_WATER = 4;
const int ID_LOG = 5;
const int ID_LEAVES = 6;

struct Hit {
    bool success;

    vec3 pos;
    vec3 normal;

    int id;
};

bool evaluateHit(inout Hit hit, vec3 voxelPos, bool renderLeaves) {
    if(voxelPos.y >= 86) return false;

    bool caseWater = voxelPos.y < 74;

    if(caseWater) voxelPos.y += 1;
    bool caseGrass = (voxelPos.y + 8.0 * fbmHash(voxelPos.xz * 0.02, 3, 2.4, 0.0)) < 80;
    bool caseStone = voxelPos.y + 4.0 *  pow(smoothstep(0.15, 0.4, fbmHash(voxelPos.xz * 0.05, 2, 3.0, 0.0)) * 2.0 - 1.0, 1.0) < 77;

    const float treeDensity = 0.998;

    #ifdef TREES
        bool caseLog = smoothHash(voxelPos.xz) > treeDensity && voxelPos.y < 83;
        bool caseLeaves = false;
    
        if(renderLeaves) {
            for(int i = -1; i <= 1; i++) {
                for(int j = -1; j <= 1; j++) {
                    for(int k = -1; k <= 1; k++) {
                        vec3 leavesPos = voxelPos + vec3(i, j, k);
    
                        if(smoothHash(leavesPos.xz) > treeDensity && leavesPos.y < 85 && leavesPos.y > 82) {
                            caseLeaves = true;
                            break;
                        }
                    }
    
                    if(caseLeaves) break;
                }
    
                if(caseLeaves) break;
            }
        }
    #else
        bool caseLeaves = false; 
        bool caseLog = false;
    #endif

	hit.success = caseGrass || caseWater || caseStone || caseLog || caseLeaves;

    if(caseGrass) {
        hit.id = ID_GRASS;
    } else if(caseWater) {
        hit.id = ID_WATER;
    } else if(caseStone) {
        hit.id = ID_STONE;
    } else if(caseLog) {
        hit.id = ID_LOG;
    } else if(caseLeaves) {
        hit.id = ID_LEAVES;
    }

    return hit.success;
}

Hit raytraceDDA(vec3 rayPos, vec3 rayDir, int raytraceLength, bool renderLeaves) {
    Hit hit;
    hit.pos = vec3(0.0);
    hit.success = false;

    if((rayPos + 10.0 * rayDir).y + u_cameraPosition.y > 90.0 && rayDir.y > 0.0) {
        return hit;
    }

    rayPos += u_cameraPosition;
    
    if(rayPos.y < 40.0) return hit;

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

        if(evaluateHit(hit, voxelPos + u_cameraPosition, renderLeaves)) {
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