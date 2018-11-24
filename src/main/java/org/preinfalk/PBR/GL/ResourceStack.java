/*
 * Copyright (c) 2018 Florian Preinfalk
 *
 * Small PBR example based on the tutorials from learnopengl.com
 */

package org.preinfalk.PBR.GL;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Keeps track of Closeable interfaces and destroys them when it's destroyed.
 */
public class ResourceStack implements Closeable {
    /**
     * Adds a Closeable interface to the internal array.
     *
     * @param closeable Closeable object
     */
    public void add(Closeable closeable) {
        closeables.add(closeable);
    }

    /**
     * Close every interface which was added in reverse order.
     *
     * @throws IOException if closing an interface fails
     */
    @Override
    public void close() throws IOException {
        for (int i = closeables.size() - 1; i >= 0; --i)
            closeables.get(i).close();

        closeables.clear();
    }

    private ArrayList<Closeable> closeables = new ArrayList<>();
}
