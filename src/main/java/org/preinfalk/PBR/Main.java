/*
 * Copyright (c) 2018 Florian Preinfalk
 *
 * Small PBR example based on the tutorials from learnopengl.com
 */

package org.preinfalk.PBR;

import org.joml.*;
import org.preinfalk.PBR.GL.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Math;
import java.util.Scanner;

import static org.lwjgl.opengl.GL33.*;

/**
 * Main class of the PBR example
 */
public class Main {
    /**
     * Main function of the PBR example
     *
     * @param args command line arguments; These are ignored.
     * @throws IOException thrown if a file cannot be read or a Closeable interface fails
     */
    public static void main(String[] args) throws IOException {
        try (ResourceStack stack = new ResourceStack()) {
            // Create OpenGL capable window
            Window wnd = new Window(640, 480, "Simple Example");
            stack.add(wnd);
            wnd.makeCurrent();

            // Load all the textures
            Texture baseColor = new Texture(new FileInputStream("dist/scuffed-plastic-alb.png"));
            stack.add(baseColor);

            Texture metallic = new Texture(new FileInputStream("dist/scuffed-plastic-metal.png"));
            stack.add(metallic);

            Texture normal = new Texture(new FileInputStream("dist/scuffed-plastic-normal.png"));
            stack.add(normal);

            Texture roughness = new Texture(new FileInputStream("dist/scuffed-plastic-rough.png"));
            stack.add(roughness);

            // Create mesh
            // Mesh mesh = new Sphere(new Vector3f(), new Vector3f(1), 128, true, true);
            Mesh mesh = new Cube(new Vector3f(), new Vector3f(1), true, true);
            stack.add(mesh);

            // Create and setup shader
            Shader shader = new Shader(getResource("/PBR.vert"), getResource("/PBR.frag"));
            stack.add(shader);

            shader.setVec3("uLightDir", new Vector3f(0, 0, -1));

            shader.setVec3("uLightColor", new Vector3f(1));

            shader.setTexture("uAlbedo", baseColor);
            shader.setTexture("uNormalMap", normal);
            shader.setTexture("uMetallicMap", metallic);
            shader.setTexture("uRoughnessMap", roughness);

            // Bind mesh and shader to Vertex Array Object (VAO)
            VAO vao = new VAO(shader);
            stack.add(vao);
            mesh.bindPosition(vao, "inPos");
            mesh.bindNormal(vao, "inNormal");
            mesh.bindTexCoord(vao, "inTexCoord");

            glEnable(GL_DEPTH_TEST);

            Vector2f last = wnd.getMousePosition();
            Vector3f cameraPos = new Vector3f(5, 0, 0);
            Quaternionf quat = new Quaternionf();

            while (!wnd.pollEvents()) {
                glClearColor(0.8f, 0.8f, 0.8f, 1.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                // Calculate rotation of mesh based on the coordinates of the mouse
                Vector2f now = wnd.getMousePosition();
                Vector2f delta = new Vector2f(now).sub(last);
                last = now;

                if (wnd.isMouseDown()) {
                    quat.premul(new Quaternionf().fromAxisAngleDeg(0, 1, 0, delta.y));
                    quat.premul(new Quaternionf().fromAxisAngleDeg(0, 0, 1, delta.x));
                    quat.normalize();
                }

                // Calculate View Projection, Model and Normal matrix
                Matrix4f matVP = new Matrix4f()
                        .perspective((float) Math.toRadians(45.0f), 640.0f / 480.0f, 0.01f, 100.0f)
                        .lookAt(cameraPos, new Vector3f(), new Vector3f(0, 0, 1));

                Matrix4f matModel = new Matrix4f().rotate(quat);

                Matrix3f matNormal = new Matrix3f();
                matModel.get3x3(matNormal);

                shader.setMat4("uViewProjection", matVP);
                shader.setMat4("uModel", matModel);
                shader.setMat3("uNormalMat", matNormal);
                shader.setVec3("uCamPos", cameraPos);

                vao.draw(GL_TRIANGLES, 0, mesh.getNumVertices());

                wnd.swap();
            }
        }
    }


    /**
     * Loads the content of a resource as a string
     *
     * @param file file name of the resource
     * @return content of the resource
     */
    private static String getResource(String file) {
        Scanner s = new Scanner(Main.class.getResourceAsStream(file)).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
