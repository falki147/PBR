/*
 * Copyright (c) 2018 Florian Preinfalk
 *
 * Small PBR example based on the tutorials from learnopengl.com
 */

package org.preinfalk.PBR.GL;

import java.io.Closeable;
import java.nio.*;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Buffer, which contains the vertices to be drawn.
 *
 * It has a fixed size. When the size is exceeded the class throws an exception.
 *
 * It contains a reference counter which can be in/decreased. When the resource hits 0 it is freed. To increase it call
 * {@link #addRef() addRef}, to decrease it {@link #close() close}. If the object is stored in a class, the reference
 * counter should be increased and decreased when destroying said class.
 */
public class Buffer implements Closeable {
    /**
     * Constructor which creates a Buffer with a specified size and the GL_STATIC_DRAW usage
     *
     * @param size size of the buffer
     */
    public Buffer(int size) {
        this(size, GL_STATIC_DRAW);
    }

    /**
     * Constructor which creates a Buffer with a specified size and usage
     *
     * @param size size of the buffer
     * @param usage usage of the buffer; This should be one of the GL_STATIC_*, GL_DYNAMIC_* or GL_STREAM_* constants.
     */
    public Buffer(int size, int usage) {
        buffer = glGenBuffers();
        data = memAlloc(size);
        this.usage = usage;
    }

    /**
     * Writes a value into the buffer and increases the position.
     *
     * @param value Value to store in the buffer
     * @return itself
     */
    public Buffer putFloat(float value) {
        changed = true;
        data.putFloat(value);
        return this;
    }

    /**
     * Sets the buffer's position to zero
     *
     * @return itself
     */
    public Buffer flip() {
        changed = true;
        data.flip();
        return this;
    }

    /**
     * Decreases the reference count and destroys the resource when it hits zero
     */
    @Override
    public void close() {
        if (references == 0) {
            glDeleteBuffers(buffer);
            memFree(data);
            buffer = 0;
            data = null;
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
     * Binds the buffer to a target and uploads the modified data if necessary
     *
     * @param target GL_ARRAY_BUFFER, GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, GL_ELEMENT_ARRAY_BUFFER,
     *               GL_PIXEL_PACK_BUFFER, GL_PIXEL_UNPACK_BUFFER, GL_TEXTURE_BUFFER, GL_TRANSFORM_FEEDBACK_BUFFER, or
     *               GL_UNIFORM_BUFFER constant.
     */
    protected void bind(int target) {
        glBindBuffer(target, buffer);

        if (changed) {
            changed = false;
            glBufferData(target, data, usage);
        }
    }

    private int references = 0;
    private int buffer;
    private int usage;
    private boolean changed = false;
    private ByteBuffer data;
}
