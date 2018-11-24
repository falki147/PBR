/*
 * Copyright (c) 2018 Florian Preinfalk
 *
 * Small PBR example based on the tutorials from learnopengl.com
 */

package org.preinfalk.PBR.GL;

import org.joml.Vector3f;

import java.io.Closeable;

import static org.lwjgl.opengl.GL33.GL_FLOAT;

/**
 * Simple sphere mesh
 *
 * It must be closed with {@link #close() close} when done working with it.
 */
public class Sphere extends Mesh implements Closeable {
    /**
     * Creates a sphere
     *
     * @param pos position of the center
     * @param radius radius of the sphere
     * @param steps determines the amount of vertices used
     * @param createNormals add normals to mesh
     * @param createTexCoords add texture coordinates to mesh
     */
    public Sphere(Vector3f pos, Vector3f radius, int steps, boolean createNormals, boolean createTexCoords) {
        hasNormals = createNormals;
        hasTexCoords = createTexCoords;

        int subSteps = (steps + 1) / 2;
        numVertices = 6 * steps * subSteps;
        buffer = new Buffer(getVertexSize() * numVertices);

        try {
            for (float i = 0; i < steps; ++i) {
                float cos0 = (float) Math.cos(2 * Math.PI * i / steps);
                float sin0 = (float) Math.sin(2 * Math.PI * i / steps);
                float cos1 = (float) Math.cos(2 * Math.PI * (i + 1) / steps);
                float sin1 = (float) Math.sin(2 * Math.PI * (i + 1) / steps);

                for (float j = 0; j < subSteps; ++j) {
                    float subCos0 = (float) Math.cos(Math.PI * j / subSteps);
                    float subSin0 = (float) Math.sin(Math.PI * j / subSteps);
                    float subCos1 = (float) Math.cos(Math.PI * (j + 1) / subSteps);
                    float subSin1 = (float) Math.sin(Math.PI * (j + 1) / subSteps);

                    addVertex(pos, radius.x * cos0 * subSin0, radius.y * sin0 * subSin0, -radius.z * subCos0, i / steps, 1 - j / subSteps);
                    addVertex(pos, radius.x * cos1 * subSin0, radius.y * sin1 * subSin0, -radius.z * subCos0, (i + 1) / steps, 1 - j / subSteps);
                    addVertex(pos, radius.x * cos1 * subSin1, radius.y * sin1 * subSin1, -radius.z * subCos1, (i + 1) / steps, 1 - (j + 1) / subSteps);

                    addVertex(pos, radius.x * cos1 * subSin1, radius.y * sin1 * subSin1, -radius.z * subCos1, (i + 1) / steps, 1 - (j + 1) / subSteps);
                    addVertex(pos, radius.x * cos0 * subSin1, radius.y * sin0 * subSin1, -radius.z * subCos1, i / steps, 1 - (j + 1) / subSteps);
                    addVertex(pos, radius.x * cos0 * subSin0, radius.y * sin0 * subSin0, -radius.z * subCos0, i / steps, 1 - j / subSteps);
                }
            }
        } catch (Throwable th) {
            buffer.close();
            throw th;
        }

        buffer.flip();
    }

    /**
     * Adds a vertex to the buffer. The normal is calculated by normalizing the offset
     *
     * @param center center; It gets added to the offset.
     * @param x x offset
     * @param y y offset
     * @param z z offset
     * @param tx x texture coordinate
     * @param ty y texture coordinate
     */
    private void addVertex(Vector3f center, float x, float y, float z, float tx, float ty) {
        buffer.putFloat(center.x + x);
        buffer.putFloat(center.y + y);
        buffer.putFloat(center.z + z);

        if (hasNormals) {
            Vector3f normal = new Vector3f(x, y, z).normalize();

            buffer.putFloat(normal.x);
            buffer.putFloat(normal.y);
            buffer.putFloat(normal.z);
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
        return numVertices;
    }

    /**
     * Close internal buffer
     */
    @Override
    public void close() {
        buffer.close();
    }

    private Buffer buffer;
    private int numVertices;
    private boolean hasNormals;
    private boolean hasTexCoords;
}
