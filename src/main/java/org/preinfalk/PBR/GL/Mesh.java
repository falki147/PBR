/*
 * Copyright (c) 2018 Florian Preinfalk
 *
 * Small PBR example based on the tutorials from learnopengl.com
 */

package org.preinfalk.PBR.GL;

import java.io.Closeable;

/**
 * Abstract class for implementing static meshes with positions, normals and texture coordinates.
 */
public abstract class Mesh implements Closeable {
    /**
     * Bind position data
     *
     * @param vao Vertex Array Object to bind to
     * @param name Name of the location in the shader
     */
    public abstract void bindPosition(VAO vao, String name);

    /**
     * Bind normal data
     *
     * @param vao Vertex Array Object to bind to
     * @param name Name of the location in the shader
     */
    public abstract void bindNormal(VAO vao, String name);

    /**
     * Bind texture coordinates data
     *
     * @param vao Vertex Array Object to bind to
     * @param name Name of the location in the shader
     */
    public abstract void bindTexCoord(VAO vao, String name);

    /**
     * @return number of vertices in the mesh
     */
    public abstract int getNumVertices();
}
