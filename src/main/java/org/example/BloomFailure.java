package org.example;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.ExhaustedAction;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.infinispan.commons.marshall.ProtoStreamMarshaller;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;


public class BloomFailure {

    public static final String CACHE_NAME = "my-cache";

    private static final ProtoStreamMarshaller marshaller = new ProtoStreamMarshaller();

    public static void main(String[] args) {

        ConfigurationBuilder remoteBuilder = new ConfigurationBuilder();
        remoteBuilder.addServer()
                .host("localhost")
                .port(11222)
                .security()
                .authentication()
                .username("admin")
                .password("admin")
                .realm("default")
                .clientIntelligence(ClientIntelligence.BASIC)
                .connectionPool()
                .maxActive(1)
                .exhaustedAction(ExhaustedAction.WAIT)
                .marshaller(marshaller)
                .remoteCache(CACHE_NAME)
                .nearCacheMode(NearCacheMode.INVALIDATED)
                .nearCacheMaxEntries(10000)
                .nearCacheUseBloomFilter(true);

        RemoteCacheManager remoteCacheManager = new RemoteCacheManager(remoteBuilder.build());

        String xml = String.format("<distributed-cache name=\"%s\" mode=\"SYNC\">" +
                "<encoding media-type=\"application/x-protostream\"/>" +
                "</distributed-cache>" , CACHE_NAME);

        CacheObject.CacheObjectSchema.INSTANCE.registerSchema(marshaller.getSerializationContext());
        CacheObject.CacheObjectSchema.INSTANCE.registerMarshallers(marshaller.getSerializationContext());
        // Cache to register the schemas with the server too
        final RemoteCache<String, String> protoMetadataCache = remoteCacheManager.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);

        // generate the message protobuf schema file and marshaller based on the annotations on Message class
        // and register it with the SerializationContext of the client
        protoMetadataCache.put(CacheObject.CacheObjectSchema.INSTANCE.getProtoFileName(), CacheObject.CacheObjectSchema.INSTANCE.getProtoFile());

        // check for definition error for the registered protobuf schemas
        String errors = protoMetadataCache.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
        if (errors != null) {
            throw new IllegalStateException("Some Protobuf schema files contain errors: " + errors + "\nSchema :\n" + CacheObject.CacheObjectSchema.INSTANCE.getProtoFile());
        }

        RemoteCache<String, CacheObject> messageCache = remoteCacheManager.administration().getOrCreateCache(CACHE_NAME, new XMLStringConfiguration(xml));

        System.out.println("Writing to cache");
        messageCache.put("1", new CacheObject("name1", 1));
        System.out.println("Removing from cache");
        messageCache.remove("1");
        System.out.println("Finished");
    }
}
