/*
 * Copyright (c) 2018 Florian Preinfalk
 *
 * Small PBR example based on the tutorials from learnopengl.com
 */

package org.preinfalk.PBR.GL;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.io.Closeable;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;

/**
 * GLFW window which has OpenGL capabilities
 */
public class Window implements Closeable {
    /**
     * Creates a GLFW Window
     *
     * @param width width of the window
     * @param height height of the window
     * @param name title of the window
     */
    public Window(int width, int height, String name) {
        if (glfwInitialized == 0 && !glfwInit())
            throw new RuntimeException("failed to initialize GLFW");

        ++glfwInitialized;

        try {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            glfwWindowHint(GLFW_RESIZABLE, 0);
            glfwWindowHint(GLFW_SAMPLES, 2);

            window = glfwCreateWindow(width, height, name, 0, 0);

            if (window == 0)
                throw new RuntimeException("failed to initialize window");

            long con = glfwGetCurrentContext();
            glfwMakeContextCurrent(window);
            capabilities = createCapabilities();

            if (con != 0)
                glfwMakeContextCurrent(con);

            glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
                @Override
                public void invoke(long window, int button, int action, int mods) {
                    if (button == GLFW_MOUSE_BUTTON_LEFT)
                        mouseDown = action != GLFW_RELEASE;
                }
            });

            glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
                @Override
                public void invoke(long window, double xpos, double ypos) {
                    mousePosition = new Vector2f((float) xpos, (float) ypos);
                }
            });
        } catch (Throwable th) {
            glfwTerminate();
            throw th;
        }
    }

    /**
     * Makes the OpenGL context of the window current
     */
    public void makeCurrent() {
        glfwMakeContextCurrent(window);
        GL.setCapabilities(capabilities);
    }

    /**
     * Polls the events and checks if the window should be closed
     * @return if the window should be closed
     */
    public boolean pollEvents() {
        glfwPollEvents();
        return glfwWindowShouldClose(window);
    }

    /**
     * Swaps the backbuffer and frontbuffer
     */
    public void swap() {
        glfwSwapBuffers(window);
    }

    /**
     * @return current mouse location inside the window
     */
    public Vector2f getMousePosition() {
        return mousePosition;
    }

    /**
     * @return if the mouse is pressed
     */
    public boolean isMouseDown() {
        return mouseDown;
    }

    /**
     * Destroys the window
     */
    @Override
    public void close() {
        glfwDestroyWindow(window);

        --glfwInitialized;
        if (glfwInitialized == 0)
            glfwTerminate();
    }

    private static int glfwInitialized = 0;

    private long window;
    private GLCapabilities capabilities;
    private Vector2f mousePosition = new Vector2f();
    private boolean mouseDown;
}
