/*
 * Copyright (c) 2018 Florian Preinfalk
 *
 * Small PBR example based on the tutorials from learnopengl.com
 */

package org.preinfalk.PBR.GL;

import org.joml.Vector3f;

import static org.lwjgl.opengl.GL33.GL_FLOAT;

/**
 * Simple cube mesh
 *
 * It must be closed with {@link #close() close} when done working with it.
 */
public class Cube extends Mesh {
    /**
     * Creates a cube
     *
     * @param pos position of the center
     * @param radius half of the cubes size
     * @param createNormals add normals to mesh
     * @param createTexCoords add texture coordinates to mesh
     */
    public Cube(Vector3f pos, Vector3f radius, boolean createNormals, boolean createTexCoords) {
        hasNormals = createNormals;
        hasTexCoords = createTexCoords;

        Vector3f p1 = new Vector3f(pos).sub(radius);
        Vector3f p2 = new Vector3f(pos).add(radius);

        buffer = new Buffer(getVertexSize() * 36);

        try {
            addVertex(p1.x, p1.y, p2.z, 0, 0, 1, 0, 0);
            addVertex(p2.x, p1.y, p2.z, 0, 0, 1, 1, 0);
            addVertex(p2.x, p2.y, p2.z, 0, 0, 1, 1, 1);
            addVertex(p2.x, p2.y, p2.z, 0, 0, 1, 1, 1);
            addVertex(p1.x, p2.y, p2.z, 0, 0, 1, 0, 1);
            addVertex(p1.x, p1.y, p2.z, 0, 0, 1, 0, 0);
            addVertex(p1.x, p1.y, p1.z, 0, 0, -1, 0, 0);
            addVertex(p1.x, p2.y, p1.z, 0, 0, -1, 0, 1);
            addVertex(p2.x, p2.y, p1.z, 0, 0, -1, 1, 1);
            addVertex(p2.x, p2.y, p1.z, 0, 0, -1, 1, 1);
            addVertex(p2.x, p1.y, p1.z, 0, 0, -1, 1, 0);
            addVertex(p1.x, p1.y, p1.z, 0, 0, -1, 0, 0);
            addVertex(p1.x, p2.y, p1.z, 0, 1, 0, 0, 0);
            addVertex(p1.x, p2.y, p2.z, 0, 1, 0, 0, 1);
            addVertex(p2.x, p2.y, p2.z, 0, 1, 0, 1, 1);
            addVertex(p2.x, p2.y, p2.z, 0, 1, 0, 1, 1);
            addVertex(p2.x, p2.y, p1.z, 0, 1, 0, 1, 0);
            addVertex(p1.x, p2.y, p1.z, 0, 1, 0, 0, 0);
            addVertex(p2.x, p1.y, p1.z, 0, -1, 0, 0, 0);
            addVertex(p2.x, p1.y, p2.z, 0, -1, 0, 0, 1);
            addVertex(p1.x, p1.y, p2.z, 0, -1, 0, 1, 1);
            addVertex(p1.x, p1.y, p2.z, 0, -1, 0, 1, 1);
            addVertex(p1.x, p1.y, p1.z, 0, -1, 0, 1, 0);
            addVertex(p2.x, p1.y, p1.z, 0, -1, 0, 0, 0);
            addVertex(p1.x, p1.y, p1.z, -1, 0, 0, 0, 0);
            addVertex(p1.x, p1.y, p2.z, -1, 0, 0, 0, 1);
            addVertex(p1.x, p2.y, p2.z, -1, 0, 0, 1, 1);
            addVertex(p1.x, p2.y, p2.z, -1, 0, 0, 1, 1);
            addVertex(p1.x, p2.y, p1.z, -1, 0, 0, 1, 0);
            addVertex(p1.x, p1.y, p1.z, -1, 0, 0, 0, 0);
            addVertex(p2.x, p2.y, p1.z, 1, 0, 0, 0, 0);
            addVertex(p2.x, p2.y, p2.z, 1, 0, 0, 0, 1);
            addVertex(p2.x, p1.y, p2.z, 1, 0, 0, 1, 1);
            addVertex(p2.x, p1.y, p2.z, 1, 0, 0, 1, 1);
            addVertex(p2.x, p1.y, p1.z, 1, 0, 0, 1, 0);
            addVertex(p2.x, p2.y, p1.z, 1, 0, 0, 0, 0);
        } catch (Throwable th) {
            buffer.close();
            throw th;
        }

        buffer.flip();
    }

    /**
     * Adds a vertex to the buffer
     *
     * @param px x position
     * @param py y position
     * @param pz z position
     * @param nx x normal
     * @param ny y normal
     * @param nz z normal
     * @param tx x texture coordinate
     * @param ty y texture coordinate
     */
    private void addVertex(float px, float py, float pz, float nx, float ny, float nz, float tx, float ty) {
        buffer.putFloat(px);
        buffer.putFloat(py);
        buffer.putFloat(pz);

        if (hasNormals) {
            buffer.putFloat(nx);
            buffer.putFloat(ny);
            buffer.putFloat(nz);
        }

        if (hasTexCoords) {
            buffer.putFloat(tx);
            buffer.putFloat(ty);
        }
    }

    /**
     * Calculate size of the vertex based on if normals and texture coordinates are stored
     *
     * @return size of a single vertex
     */
    private int getVertexSize() {
        int vertexSize = 12;

        if (hasNormals)
            vertexSize += 12;

        if (hasTexCoords)
            vertexSize += 8;

        return vertexSize;
    }

    /**
     * @return offset in vertex to normal
     */
    private int getNormalOffset() {
        return 12;
    }

    /**
     * @return offset in vertex to texture coordinate
     */
    private int getTexCoordOffset() {
        return hasNormals ? 24 : 12;
    }

    @Override
    public void bindPosition(VAO vao, String name) {
        vao.bindBuffer(buffer, name, 3, GL_FLOAT, false, getVertexSize(), 0);
    }

    @Override
    public void bindNormal(VAO vao, String name) {
        if (hasNormals)
            vao.bindBuffer(buffer, name, 3, GL_FLOAT, false, getVertexSize(), getNormalOffset());
    }

    @Override
    public void bindTexCoord(VAO vao, String name) {
        if (hasTexCoords)
            vao.bindBuffer(buffer, name, 2, GL_FLOAT, false, getVertexSize(), getTexCoordOffset());
    }

    @Override
    public int getNumVertices() {
        return 36;
    }

    /**
     * Close internal buffer
     */
    @Override
    public void close() {
        buffer.close();
    }

    private Buffer buffer;
    private boolean hasNormals;
    private boolean hasTexCoords;
}
