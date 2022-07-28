package org.example;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;
import org.infinispan.protostream.annotations.ProtoField;

public class CacheObject {

    @ProtoField(number = 1)
    public String name;

    @ProtoField(number = 2)
    public Integer value;

    public CacheObject(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    public CacheObject() {
    }

    @AutoProtoSchemaBuilder(includeClasses = CacheObject.class, schemaFileName = "cache-object-schema.proto")
    public interface CacheObjectSchema extends GeneratedSchema {

        CacheObjectSchema INSTANCE = new CacheObjectSchemaImpl();

    }
}
