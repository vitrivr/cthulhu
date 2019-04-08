package org.vitrivr.cthulhu.jobs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class StreamUtils {

  /**
   * Streams data from the input stream to the output stream.
   */
  public static long stream(InputStream input, OutputStream output) throws IOException {
    try (
        ReadableByteChannel inputChannel = Channels.newChannel(input);
        WritableByteChannel outputChannel = Channels.newChannel(output)
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

  /**
   * Copies a file from a given input stream to the output stream.
   * @param in the file to copy
   * @param out the destination of the copy
   * @throws IOException on failure to copy the file
   */
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

  /**
   * Copies a file from a given location to the file location.
   * @param file the file to copy
   * @param out the destination of the copy
   * @throws IOException on failure to copy the file
   */
  public static void copy(File file, OutputStream out) throws IOException {
    try (InputStream in = new FileInputStream(file)) {
      copy(in, out);
    }
  }

  /**
   * Copies a file from a given location to the file location.
   * @param in the file to copy
   * @param file the destination of the copy
   * @throws IOException on failure to copy the file
   */
  public static void copy(InputStream in, File file) throws IOException {
    try (OutputStream out = new FileOutputStream(file)) {
      copy(in, out);
    }
  }
}
