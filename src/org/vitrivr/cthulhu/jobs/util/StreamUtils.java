package org.vitrivr.cthulhu.jobs;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

import java.io.IOException;

public class StreamUtils {
    public static long stream(InputStream input, OutputStream output) throws IOException {
        try (
             ReadableByteChannel inputChannel = Channels.newChannel(input);
             WritableByteChannel outputChannel = Channels.newChannel(output);
             ) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(10240);
                long size = 0;
                while (inputChannel.read(buffer) != -1) {
                    buffer.flip();
                    size += outputChannel.write(buffer);
                    buffer.clear();
                }
                return size;
            }
    }
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int readCount = in.read(buffer);
            if (readCount < 0) {
                break;
            }
            out.write(buffer, 0, readCount);
        }
    }

    public static void copy(File file, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            copy(in, out);
        } finally {
            in.close();
        }
    }

    public static void copy(InputStream in, File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        try {
            copy(in, out);
        } finally {
            out.close();
        }
    }
}
