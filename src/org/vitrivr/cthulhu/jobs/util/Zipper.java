package org.vitrivr.cthulhu.jobs;

import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

import java.io.OutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import java.util.LinkedList;
import java.util.Deque;

import java.net.URI;

class Zipper { 
    public static boolean zip(File directory, OutputStream zipOutput) {
        URI base = directory.toURI();
        Deque<File> queue = new LinkedList<File>();
        queue.push(directory);
        boolean success = true;
        try {
            ZipOutputStream zout = new ZipOutputStream(zipOutput);
            while (!queue.isEmpty()) {
                directory = queue.pop();
                for (File kid : directory.listFiles()) {
                    String name = base.relativize(kid.toURI()).getPath();
                    if (kid.isDirectory()) {
                        queue.push(kid);
                        name = name.endsWith("/") ? name : name + "/";
                        zout.putNextEntry(new ZipEntry(name));
                    } else {
                        zout.putNextEntry(new ZipEntry(name));
                        StreamUtils.copy(kid, zout);
                        zout.closeEntry();
                    }
                }
            }
            zout.finish();
        } catch (Exception e) {
            success = false;
        } finally {
            return success;
        }
    }
}
