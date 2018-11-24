/*
 * Copyright (c) 2018 Florian Preinfalk
 *
 * Small PBR example based on the tutorials from learnopengl.com
 */

package org.preinfalk.PBR.GL;

import java.io.Closeable;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL33.*;

/**
 * Vertex Array Object which binds the buffer to the shader.
 *
 * It keeps track of the resources, so the buffer or shader can be closed.
 *
 * It contains a reference counter which can be in/decreased. When the resource hits 0 it is freed. To increase it call
 * {@link #addRef() addRef}, to decrease it {@link #close() close}. If the object is stored in a class, the reference
 * counter should be increased and decreased when destroying said class.
 */
public class VAO implements Closeable {
    /**
     * @param shader shader to be used for drawing
     */
    public VAO(Shader shader) {
        vao = glGenVertexArrays();
        this.shader = shader;
        shader.addRef();
    }

    /**
     * Binds a buffer to the attribute
     *
     * @param buffer buffer to bind
     * @param name name of the attribute
     * @param size number of values
     * @param type type of value; GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT, GL_UNSIGNED_SHORT, GL_INT, GL_UNSIGNED_INT,
     *             GL_HALF_FLOAT, GL_FLOAT, GL_DOUBLE, GL_INT_2_10_10_10_REV and GL_UNSIGNED_INT_2_10_10_10_REV
     * @param normalized normalize input (convert values to a range from -1 to 1)
     * @param stride value which is added to the pointer for each vertex
     * @param pointer offset to data in first vertex
     */
    public void bindBuffer(Buffer buffer, String name, int size, int type, boolean normalized, int stride, long pointer) {
        glBindVertexArray(vao);
        buffer.bind(GL_ARRAY_BUFFER);

        buffers.add(buffer);
        buffer.addRef();

        int loc = shader.getAttribLocation(name);
        glVertexAttribPointer(loc, size, type, normalized, stride, pointer);
        glEnableVertexAttribArray(loc);
    }

    /**
     * @param mode GL_POINTS, GL_LINE_STRIP, GL_LINE_LOOP, GL_LINES, GL_LINE_STRIP_ADJACENCY, GL_LINES_ADJACENCY,
     *             GL_TRIANGLE_STRIP, GL_TRIANGLE_FAN, GL_TRIANGLES, GL_TRIANGLE_STRIP_ADJACENCY and GL_TRIANGLES_ADJACENCY
     * @param first first vertex
     * @param count number of vertices
     */
    public void draw(int mode, int first, int count) {
        shader.use();
        glBindVertexArray(vao);
        glDrawArrays(mode, first, count);
    }

    /**
     * Decreases the reference count and destroys the resource when it hits zero
     */
    @Override
    public void close() {
        if (references == 0) {
            glDeleteVertexArrays(vao);
            vao = 0;

            shader.close();
            shader = null;

            for (Buffer buffer : buffers)
                buffer.close();

            buffers.clear();
        }

        --references;
    }

    /**
     * Increases the reference count
     */
    public void addRef() {
        ++references;
    }

    private int references = 0;
    private int vao;
    private Shader shader;
    private ArrayList<Buffer> buffers = new ArrayList<>();
}
