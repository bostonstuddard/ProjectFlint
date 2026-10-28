#version 330

layout(std140) uniform LightmapInfo {
    float AmbientLightFactor;
    float SkyFactor;
    float BlockFactor;
    float NightVisionFactor;
    float DarknessScale;
    float DarkenWorldFactor;
    float BrightnessFactor;
    vec3 SkyLightColor;
    vec3 AmbientColor;
} lightmapInfo;

in vec2 texCoord;

out vec4 fragColor;

float get_brightness(float level) {
    return level / (4.0 - 3.0 * level);
}

vec3 notGamma(vec3 color) {
    float maxComponent = max(max(color.x, color.y), color.z);
    if (maxComponent <= 0.0) {
        return color;
    }
    float maxInverted = 1.0 - maxComponent;
    float maxScaled = 1.0 - maxInverted * maxInverted * maxInverted * maxInverted;
    return color * (maxScaled / maxComponent);
}

vec3 vanillaColor(float block_brightness, float sky_brightness) {
    vec3 color = vec3(
        block_brightness,
        block_brightness * ((block_brightness * 0.6 + 0.4) * 0.6 + 0.4),
        block_brightness * (block_brightness * block_brightness * 0.6 + 0.4)
    );
    color = mix(color, lightmapInfo.AmbientColor, lightmapInfo.AmbientLightFactor);
    color += lightmapInfo.SkyLightColor * sky_brightness;
    color = mix(color, vec3(0.75), 0.04);
    return color;
}

void main() {
    float block_brightness = get_brightness(floor(texCoord.x * 16.0) / 15.0) * lightmapInfo.BlockFactor;
    float raw_sky_factor = clamp(lightmapInfo.SkyFactor, 0.0, 1.0);
    float raw_sky_brightness = get_brightness(floor(texCoord.y * 16.0) / 15.0) * raw_sky_factor;

    // Actual Night Vision and Creative both bypass Project Flint's hardened-darkness
    // branch, but Creative uses a negative sentinel so it keeps normal vanilla lighting
    // instead of receiving Night Vision/fullbright normalization.
    bool creative_bypass = lightmapInfo.NightVisionFactor < 0.0;
    if (creative_bypass || lightmapInfo.NightVisionFactor > 0.0) {
        vec3 color = vanillaColor(block_brightness, raw_sky_brightness);
        if (lightmapInfo.AmbientLightFactor == 0.0) {
            vec3 darkened_color = color * vec3(0.7, 0.6, 0.6);
            color = mix(color, darkened_color, lightmapInfo.DarkenWorldFactor);
        }
        if (!creative_bypass) {
            float max_component = max(color.r, max(color.g, color.b));
            if (max_component > 0.0 && max_component < 1.0) {
                vec3 bright_color = color / max_component;
                color = mix(color, bright_color, lightmapInfo.NightVisionFactor);
            }
        }
        if (lightmapInfo.AmbientLightFactor == 0.0) {
            color = color - vec3(lightmapInfo.DarknessScale);
        }
        color = clamp(color, 0.0, 1.0);
        vec3 gamma_color = notGamma(color);
        color = mix(color, gamma_color, lightmapInfo.BrightnessFactor);
        color = mix(color, vec3(0.75), 0.04);
        fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
        return;
    }

    // Survival hardcore darkness: daylight remains normal while moonlight/night sky
    // rapidly collapses toward zero. Block light is never distance-clipped, so fires,
    // torches, and lit structures remain useful navigation landmarks.
    float hardened_sky_factor = smoothstep(0.28, 0.88, raw_sky_factor);
    hardened_sky_factor *= hardened_sky_factor;
    float sky_brightness = get_brightness(floor(texCoord.y * 16.0) / 15.0) * hardened_sky_factor;
    float dominant_light = clamp(max(block_brightness, sky_brightness), 0.0, 1.0);

    vec3 color = vec3(
        sky_brightness * 0.95 + block_brightness,
        sky_brightness * 0.95 + block_brightness * 0.82,
        sky_brightness + block_brightness * 0.67
    );
    color = mix(color, lightmapInfo.AmbientColor, lightmapInfo.AmbientLightFactor);

    if (lightmapInfo.AmbientLightFactor == 0.0) {
        vec3 darkened_color = color * vec3(0.7, 0.6, 0.6);
        color = mix(color, darkened_color, lightmapInfo.DarkenWorldFactor);
        color = color - vec3(lightmapInfo.DarknessScale);
    }

    color = clamp(color, 0.0, 1.0);
    vec3 gamma_color = notGamma(color);
    color = mix(color, gamma_color, lightmapInfo.BrightnessFactor * dominant_light);
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
