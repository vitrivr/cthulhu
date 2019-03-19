package org.vitrivr.cineast.core.data;

public enum MediaType {
  VIDEO(0, "v", "video"),
  IMAGE(1, "i", "image"),
  AUDIO(2, "a", "audio"),
  MODEL3D(3, "m", "3dmodel");

  private final int id;
  private final String prefix;
  private final String name;

  MediaType(int id, String prefix, String name) {
    this.id = id;
    this.prefix = prefix;
    this.name = name.trim();
  }

  public int getId() {
    return id;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getName() {
    return name;
  }
}
