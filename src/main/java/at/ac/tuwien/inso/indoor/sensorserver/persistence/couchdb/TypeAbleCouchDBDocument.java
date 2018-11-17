package at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ektorp.support.CouchDbDocument;
import org.ektorp.support.TypeDiscriminator;

/**
 * @author PatrickF
 * @since 10.12.13
 * Time: 17:17
 */
public class TypeAbleCouchDBDocument extends CouchDbDocument {
    @JsonProperty("dbType")
    @TypeDiscriminator
    private String dbType = "undefined";

    public TypeAbleCouchDBDocument() {
        super();
        setDbType(this.getClass().getSimpleName());
    }

    @JsonProperty("dbType")
    public String getDbType() {
        return dbType;
    }

    @JsonProperty("dbType")
    public void setDbType(String dbType) {
        this.dbType = dbType;
    }
}
