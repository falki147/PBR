/*
 * Copyright (c) 2018 Florian Preinfalk
 *
 * Small PBR example based on the tutorials from learnopengl.com
 */

package org.preinfalk.PBR.GL;

import org.lwjgl.system.MemoryStack;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * Texture, which contains an image.
 *
 * It contains a reference counter which can be in/decreased. When the resource hits 0 it is freed. To increase it call
 * {@link #addRef() addRef}, to decrease it {@link #close() close}. If the object is stored in a class, the reference
 * counter should be increased and decreased when destroying said class.
 */
public class Texture implements Closeable {
    /**
     * Create an empty texture
     */
    public Texture() {
        texture = glGenTextures();
    }

    /**
     * Create a texture from a file
     *
     * @param file file name of the image
     */
    public Texture(String file) {
        this();

        try {
            load(file);
        } catch (Throwable th) {
            glDeleteTextures(texture);
            throw th;
        }
    }

    /**
     * Create a texture from stream
     *
     * @param stream stream of the image file
     */
    public Texture(InputStream stream) {
        this();

        try {
            load(stream);
        } catch (Throwable th) {
            glDeleteTextures(texture);
            throw th;
        }
    }

    /**
     * Load a texture from a file
     *
     * @param path file name of the image
     */
    public void load(String path) {
        ByteBuffer image = null;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            stbi_set_flip_vertically_on_load(true);
            image = stbi_load(path, w, h, comp, 4);

            if (image == null)
                throw new RuntimeException("Failed to load a texture file\n" + stbi_failure_reason());

            loadRaw(image, w.get(), h.get());
        } finally {
            if (image != null)
                stbi_image_free(image);
        }
    }

    /**
     * Load a texture from a ByteBuffer
     *
     * @param buffer buffer containing the image data
     */
    public void load(ByteBuffer buffer) {
        ByteBuffer image = null;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            stbi_set_flip_vertically_on_load(true);
            image = stbi_load_from_memory(buffer, w, h, comp, 4);

            if (image == null)
                throw new RuntimeException("Failed to load a texture file\n" + stbi_failure_reason());

            loadRaw(image, w.get(), h.get());
        } finally {
            if (image != null)
                stbi_image_free(image);
        }
    }

    /**
     * Load a texture from a stream
     *
     * @param stream stream of the image file
     */
    public void load(InputStream stream) {
        byte[] data;

        try {
            data = new byte[stream.available()];
            stream.read(data);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        try {
            FileOutputStream s = new FileOutputStream("D:\\test.png");
            try {
                s.write(data);
            } finally {
                s.close();
            }
        }
        catch (Throwable th) {}

        ByteBuffer buffer = memAlloc(data.length);

        try {
            buffer.put(data);
            buffer.rewind();
            load(buffer);
        } finally {
            memFree(buffer);
        }
    }

    /**
     * Load a texture from raw image data (RGBA, unsigned bytes)
     *
     * @param buffer buffer containing the data
     * @param width width of the image
     * @param height height of the image
     */
    public void loadRaw(ByteBuffer buffer, int width, int height) {
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glGenerateMipmap(GL_TEXTURE_2D);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }

    /**
     * Decreases the reference count and destroys the resource when it hits zero
     */
    @Override
    public void close() {
        if (references == 0) {
            glDeleteTextures(texture);
            texture = 0;
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
     * Bind the texture to a target
     *
     * @param target GL_TEXTURE_1D, GL_TEXTURE_2D, GL_TEXTURE_3D, or GL_TEXTURE_1D_ARRAY, GL_TEXTURE_2D_ARRAY,
     *               GL_TEXTURE_RECTANGLE, GL_TEXTURE_CUBE_MAP, GL_TEXTURE_BUFFER, GL_TEXTURE_2D_MULTISAMPLE or
     *               GL_TEXTURE_2D_MULTISAMPLE_ARRAY constant
     */
    protected void bind(int target) {
        glBindTexture(target, texture);
    }

    private int references = 0;
    private int texture;
}
