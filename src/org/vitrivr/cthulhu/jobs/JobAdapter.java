package org.vitrivr.cthulhu.jobs;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

/**
 * Implements a serializing and deserializing infrastructure for jobs and their subclasses. Requires
 * Job.type to be equal to its class name.
 */
public final class JobAdapter implements JsonSerializer<Job>, JsonDeserializer<Job> {

  /**
   * Serializes the job. Normal Gson behavior.
   */
  public JsonElement serialize(Job object, Type interfaceType, JsonSerializationContext context) {
    final JsonObject wrapper = new JsonObject();
    return context.serialize(object)
        .getAsJsonObject(); // Serialize normally - the type is part of the object
  }

  /**
   * Deserializes a JSON definition of a job. It selects the Class of the object it creates with the
   * type parameter of the JSON definition.
   * <p>
   * It REQUIRES a type parameter in the Job definition.
   * </p>
   */
  public Job deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context)
      throws JsonParseException {
    final JsonObject wrapper = (JsonObject) elem;
    final JsonElement typeName = get(wrapper, "type");
    final Type actualType = typeForName(typeName);
    return context.deserialize(elem, actualType);
  }

  private Type typeForName(final JsonElement typeElem) {
    try {
      return Class.forName("org.vitrivr.cthulhu.jobs." + typeElem.getAsString());
    } catch (ClassNotFoundException e) {
      throw new JsonParseException(e);
    }
  }

  private JsonElement get(final JsonObject wrapper, String memberName) {
    final JsonElement elem = wrapper.get(memberName);
    if (elem == null) {
      throw new JsonParseException(
          "no '" + memberName + "' member found in what was expected to be an interface wrapper");
    }
    return elem;
  }
}
