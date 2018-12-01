/*
 * Copyright (c) 2018 Florian Preinfalk
 *
 * Small PBR example based on the tutorials from learnopengl.com
 */

package org.preinfalk.PBR.GL;

import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.io.Closeable;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * Shader which contains an OpenGL program with vertex and fragment shaders.
 *
 * It contains a reference counter which can be in/decreased. When the resource hits zero it is freed. To increase it
 * call {@link #addRef() addRef}, to decrease it {@link #close() close}. If the object is stored in a class, the
 * reference counter should be increased and decreased when destroying said class.
 */
public class Shader implements Closeable {
    private class TextureEntry {
        TextureEntry(int index, Texture texture) {
            this.index = index;
            this.texture = texture;
        }

        int index;
        Texture texture;
    }

    /**
     * Create shader from vertex and fragment GLSL code.
     *
     * @param vert code for vertex shader
     * @param frag code for fragment shader
     */
    public Shader(String vert, String frag) {
        int shdvert = glCreateShader(GL_VERTEX_SHADER);
        int shdfrag = glCreateShader(GL_FRAGMENT_SHADER);
        int prog = glCreateProgram();

        try {
            glShaderSource(shdvert, vert);
            glCompileShader(shdvert);

            if (glGetShaderi(shdvert, GL_COMPILE_STATUS) == GL_FALSE)
                throw new RuntimeException("Failed to compile vertex shader:\n" + glGetShaderInfoLog(shdvert));

            glShaderSource(shdfrag, frag);
            glCompileShader(shdfrag);

            if (glGetShaderi(shdfrag, GL_COMPILE_STATUS) == GL_FALSE)
                throw new RuntimeException("Failed to compile fragment shader:\n" + glGetShaderInfoLog(shdfrag));

            glAttachShader(prog, shdvert);
            glAttachShader(prog, shdfrag);
            glLinkProgram(prog);

            if (glGetProgrami(prog, GL_LINK_STATUS) == GL_FALSE)
                throw new RuntimeException("Failed to link program:\n" + glGetProgramInfoLog(prog));

            glDeleteShader(shdfrag);
            glDeleteShader(shdvert);

            program = prog;
        } catch (Throwable th) {
            glDeleteShader(shdvert);
            glDeleteShader(shdfrag);
            glDeleteProgram(prog);
            throw th;
        }
    }

    /**
     * Sets the uniform to one or more floats
     *
     * @param name name of the uniform
     * @param values values
     */
    public void setFloat(String name, float... values) {
        glUseProgram(program);
        glUniform1fv(glGetUniformLocation(program, name), values);
    }

    /**
     * Sets the uniform to one or more vec2
     *
     * @param name name of the uniform
     * @param values values
     */
    public void setVec2(String name, Vector2f... values) {
        glUseProgram(program);

        try (MemoryStack stack = stackPush()) {
            FloatBuffer data = stack.mallocFloat(values.length * 2);

            for (Vector2f vec : values)
                vec.get(data);

            glUniform2fv(glGetUniformLocation(program, name), data);
        }
    }

    /**
     * Sets the uniform to one or more vec3
     *
     * @param name name of the uniform
     * @param values values
     */
    public void setVec3(String name, Vector3f... values) {
        glUseProgram(program);

        try (MemoryStack stack = stackPush()) {
            FloatBuffer data = stack.mallocFloat(values.length * 3);

            for (Vector3f vec : values)
                vec.get(data);

            glUniform3fv(glGetUniformLocation(program, name), data);
        }
    }

    /**
     * Sets the uniform to one or more vec4
     *
     * @param name name of the uniform
     * @param values values
     */
    public void setVec4(String name, Vector4f... values) {
        glUseProgram(program);

        try (MemoryStack stack = stackPush()) {
            FloatBuffer data = stack.mallocFloat(values.length * 4);

            for (Vector4f vec : values)
                vec.get(data);

            glUniform4fv(glGetUniformLocation(program, name), data);
        }
    }

    /**
     * Sets the uniform to one mat3
     *
     * @param name name of the uniform
     * @param value values
     */
    public void setMat3(String name, Matrix3f value) {
        setMat3(name, value, false);
    }

    /**
     * Sets the uniform to one mat3 and optionally transpose it
     *
     * @param name name of the uniform
     * @param value values
     * @param transpose transpose the matrix
     */
    public void setMat3(String name, Matrix3f value, boolean transpose) {
        glUseProgram(program);

        try (MemoryStack stack = stackPush()) {
            FloatBuffer data = stack.mallocFloat(9);
            value.get(data);
            glUniformMatrix3fv(glGetUniformLocation(program, name), transpose, data);
        }
    }

    /**
     * Sets the uniform to one mat4
     *
     * @param name name of the uniform
     * @param value values
     */
    public void setMat4(String name, Matrix4f value) {
        setMat4(name, value, false);
    }

    /**
     * Sets the uniform to one mat4 and optionally transpose it
     *
     * @param name name of the uniform
     * @param value values
     * @param transpose transpose the matrix
     */
    public void setMat4(String name, Matrix4f value, boolean transpose) {
        glUseProgram(program);

        try (MemoryStack stack = stackPush()) {
            FloatBuffer data = stack.mallocFloat(16);
            value.get(data);
            glUniformMatrix4fv(glGetUniformLocation(program, name), transpose, data);
        }
    }

    /**
     * Sets the uniform (sampler) to the texture
     *
     * @param name name of the uniform
     * @param texture texture to be assigned
     */
    public void setTexture(String name, Texture texture) {
        TextureEntry entry = textures.get(name);

        if (entry != null && entry.texture == texture)
            return;

        if (entry == null) {
            int loc = glGetUniformLocation(program, name);

            if (loc >= 0) {
                if (numIndices >= GL_MAX_TEXTURE_UNITS)
                    throw new RuntimeException("too many textures set");

                textures.put(name, new TextureEntry(numIndices, texture));

                glUseProgram(program);
                glUniform1i(loc, numIndices);

                ++numIndices;
            }
        } else
            entry.texture = texture;
    }

    /**
     * Decreases the reference count and destroys the resource when it hits zero
     */
    @Override
    public void close() {
        if (references == 0) {
            glDeleteProgram(program);
            program = 0;
        }

        --references;
    }

    /**
     * Increases the reference count
     */
    public void addRef() {
        ++references;
    }

    /**
     * Bind shader and textures
     */
    protected void use() {
        glUseProgram(program);

        textures.forEach((s, entry) -> {
            glActiveTexture(GL_TEXTURE0 + entry.index);
            entry.texture.bind(GL_TEXTURE_2D);
        });
    }

    /**
     * @param name name of the attribute
     * @return location of the attribute
     */
    protected int getAttribLocation(String name) {
        return glGetAttribLocation(program, name);
    }

    private int references = 0;
    private int program;
    private int numIndices = 0;
    private Map<String, TextureEntry> textures = new HashMap<>();
}
