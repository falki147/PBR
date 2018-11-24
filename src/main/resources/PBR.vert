// Original code is from Joey De Vries (learnopengl.com)
// All the input, output and uniform names were changed, AO was removed and only one directional light is used.
// Source (24.11.2018): https://learnopengl.com/code_viewer_gh.php?code=src/6.pbr/1.2.lighting_textured/1.2.pbr.vs

#version 330 core

in vec3 inPos;
in vec2 inTexCoord;
in vec3 inNormal;

out vec2 vTexCoord;
out vec3 vWorldPos;
out vec3 vNormal;

uniform mat4 uViewProjection;
uniform mat4 uModel;
uniform mat3 uNormalMat;

void main() {
    vTexCoord = inTexCoord;
    vWorldPos = vec3(uModel * vec4(inPos, 1.0));
    vNormal = uNormalMat * inNormal;

    gl_Position = uViewProjection * vec4(vWorldPos, 1.0);
}
