package org.vitrivr.cthulhu.jobs.util;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper {

  public static boolean zip(File directory, OutputStream zipOutput) {
    URI base = directory.toURI();
    Deque<File> queue = new LinkedList<>();
    queue.push(directory);
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
      return false;
    }
    return true;
  }
}
