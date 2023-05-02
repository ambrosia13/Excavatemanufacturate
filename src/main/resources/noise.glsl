// --------------------------------------------------------------------------------------------------------
// Hash Without Sine from https://www.shadertoy.com/view/4djSRW
// Code released under the MIT license.
// --------------------------------------------------------------------------------------------------------
// 2 out, 2 in...
vec2 hash22(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * vec3(.1031, .1030, .0973));
    p3 += dot(p3, p3.yzx+33.33);
    return fract((p3.xx+p3.yz)*p3.zy);
}

// 1 out, 2 in...
float hash12(vec2 p) {
    vec3 p3  = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}
//  1 out, 3 in...
float hash13(vec3 p3) {
    p3  = fract(p3 * 0.1031);
    p3 += dot(p3, p3.zyx + 31.32);
    return fract((p3.x + p3.y) * p3.z);
}

//  2 out, 1 in...
vec2 hash21(float p) {
    vec3 p3 = fract(vec3(p) * vec3(.1031, .1030, .0973));
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.xx+p3.yz)*p3.zy);
}

//  3 out, 1 in...
vec3 hash31(float p) {
    vec3 p3 = fract(vec3(p) * vec3(.1031, .1030, .0973));
    p3 += dot(p3, p3.yzx+33.33);
    return fract((p3.xxy+p3.yzz)*p3.zyx);
}

vec3 hash33(vec3 p3) {
    p3 = fract(p3 * vec3(.1031, .1030, .0973));
    p3 += dot(p3, p3.yxz+33.33);
    return fract((p3.xxy + p3.yxx)*p3.zyx);
}

// --------------------------------------------------------------------------------------------------------
// PBR-related noise functions.
// --------------------------------------------------------------------------------------------------------
vec2 sincos(float x) {
    return vec2(sin(x), cos(x));
}
vec2 diskSampling(float i, float n, float phi) {
    float theta = (i + phi) / n;
    return sincos(theta * TAU * n * 1.618033988749894) * theta;
}

// Provided by Belmu
// Noise distribution: https://www.pcg-random.org/
void pcg(inout uint seed) {
    uint state = seed * 747796405u + 2891336453u;
    uint word = ((state >> ((state >> 28u) + 4u)) ^ state) * 277803737u;
    seed = (word >> 22u) ^ word;
}

uint rngState = 185730u + uint(gl_FragCoord.x + gl_FragCoord.y * u_resolution.x);
//                    ^ multiply by frame counter here when implemented

float randF() {
    pcg(rngState);
    return float(rngState) / float(0xffffffffu);
}

// From Jessie
vec3 generateUnitVector(vec2 xy) {
    xy.x *= TAU; xy.y = 2.0 * xy.y - 1.0;
    return vec3(sincos(xy.x) * sqrt(1.0 - xy.y * xy.y), xy.y);
}

vec3 generateCosineVector(vec3 vector, vec2 xy) {
    return normalize(vector + generateUnitVector(xy));
}
// -----------------------------------------------------------------------------------------------

vec3 generateCosineVector(vec3 vector, float roughness) {
    return normalize(
        vector +
        roughness * generateUnitVector(
            vec2(
                randF(), randF()
            )
        )
    );
}
vec3 generateCosineVector(vec3 vector) {
    return generateCosineVector(vector, 1.0);
}

// --------------------------------------------------------------------------------------------------------
// Generative noise functions
// --------------------------------------------------------------------------------------------------------
const mat2 ROTATE_30_DEGREES = mat2(
    0.99995824399, 0.00913839539,
    -0.00913839539, 0.99995824399
);

// 2D noise
float smoothHash(in vec2 st) {
    // "Value Noise" from Inigo Quilez
    // https://www.shadertoy.com/view/lsf3WH
    vec2 i = (floor(st));
    vec2 f = fract(st);

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(
        mix(
            hash12(i + vec2(0.0,0.0)),
            hash12(i + vec2(1.0,0.0)),
            u.x
        ),
        mix(
            hash12(i + vec2(0.0,1.0)),
            hash12(i + vec2(1.0,1.0)),
            u.x
        ),
        u.y
    );
}

float fbmHash(vec2 uv, int octaves, float lacunarity, float t) {
    float noise = 0.01;
    float amp = 0.5;

    for (int i = 0; i < octaves; i++) {
        noise += amp * (smoothHash(uv));
        uv = ROTATE_30_DEGREES * uv * lacunarity + mod(1.0 * t, 1000.0);
        amp *= 0.5;
    }

    return noise * (octaves + 1.0) / octaves;
}
float fbmHash(vec2 uv, int octaves, float t) {
    return fbmHash(uv, octaves, 2.0, t);
}
float fbmHash(vec2 uv, int octaves) {
    return fbmHash(uv, octaves, 0.0);
}

// 3D noise
float smoothHash(in vec3 st) {
    // "Value Noise" from Inigo Quilez, modified
    // https://www.shadertoy.com/view/lsf3WH
    vec3 i = (floor(st));
    vec3 f = fract(st);

    vec3 u = f * f * (3.0 - 2.0 * f);

    return mix(
        mix(
            mix(
                hash13(i + vec3(0.0, 0.0, 0.0)),
                hash13(i + vec3(1.0, 0.0, 0.0)),
                u.x
            ),
            mix(
                hash13(i + vec3(0.0, 1.0, 0.0)),
                hash13(i + vec3(1.0, 1.0, 0.0)),
                u.x
            ),
            u.y
        ),
        mix(
            mix(
                hash13(i + vec3(0.0, 0.0, 1.0)),
                hash13(i + vec3(1.0, 0.0, 1.0)),
                u.x
            ),
            mix(
                hash13(i + vec3(0.0, 1.0, 1.0)),
                hash13(i + vec3(1.0, 1.0, 1.0)),
                u.x
            ),
            u.y
        ),
        u.z
    );
}

float fbmHash3DBlocky(vec3 uv, int octaves, float lacunarity, float t) {
    float noise = 0.01;
    float amp = 0.5;

    for (int i = 0; i < octaves; i++) {
        noise += amp * (hash13(floor(uv * 2.0) / 2.0));
        uv = 10.0 + uv * lacunarity + mod(1.0 * t, 1000.0);
        amp *= 0.5;
    }

    return noise * (octaves + 1.0) / octaves;
}

float fbmHash3D(vec3 uv, int octaves, float lacunarity, float t) {
    float noise = 0.01;
    float amp = 0.5;

    for (int i = 0; i < octaves; i++) {
        noise += amp * (smoothHash(uv));
        uv = 10.0 + uv * lacunarity + mod(1.0 * t, 1000.0);
        amp *= 0.5;
    }

    return noise * (octaves + 1.0) / octaves;
}
float fbmHash3D(vec3 uv, int octaves, float t) {
    return fbmHash3D(uv, octaves, 2.0, t);
}
float fbmHash3D(vec3 uv, int octaves) {
    return fbmHash3D(uv, octaves, 0.0);
}