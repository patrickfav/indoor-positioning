package at.ac.tuwien.inso.indoor.sensorserver.persistence;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.AverageWlanScanMeasurement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.Attachment;
import org.ektorp.CouchDbConnector;
import org.ektorp.impl.StdObjectMapperFactory;
import org.ektorp.support.Revisions;

import java.util.Map;

/**
 * @author PatrickF
 * @since 02.12.13
 *        Time: 14:31
 */
public class ObjectMapperManager extends StdObjectMapperFactory {

    public static ObjectMapper createMapperForAPI() {
        ObjectMapper mapper =  new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper = addMixinsForAPI(mapper);
        return mapper;
    }

    public static ObjectMapper addMixinsForAPI(ObjectMapper mapper) {
        //mapper.addMixInAnnotations(Parcel.class, EktropMappingIgnoreMixin.class);
        return mapper;
    }

    @Override
    public synchronized ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = addMixinsForDB(super.createObjectMapper());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    @Override
    public ObjectMapper createObjectMapper(CouchDbConnector connector) {
        ObjectMapper objectMapper = addMixinsForDB(super.createObjectMapper(connector));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    public static ObjectMapper addMixinsForDB(ObjectMapper mapper) {
        mapper.addMixInAnnotations(AverageWlanScanMeasurement.class,AverageWlanScanMeasurement.AverageWlanScanMeasurementDBMixin.class);
        return mapper;
    }

    public abstract static class EktropMappingIgnoreMixin {
        @JsonIgnore
        String dbType;
        @JsonIgnore public abstract String getDbType();
        @JsonIgnore public abstract void setDbType(String s);
        @JsonIgnore
        String id;
        @JsonIgnore public abstract String getId();
        @JsonIgnore public abstract void setId(String s);
        @JsonIgnore
        String revision;
        @JsonIgnore public abstract String getRevision();
        @JsonIgnore public abstract void setRevision(String s);
        @JsonIgnore private Revisions revisions;
        @JsonIgnore abstract void setRevisions(Revisions r);
        @JsonIgnore
        String attachments;
        @JsonIgnore abstract Map<String, Attachment> getAttachments();
        @JsonIgnore abstract void setAttachments(Map<String, Attachment> attachments);
    }
}
