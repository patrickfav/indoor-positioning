package at.ac.tuwien.inso.indoor.sensorserver.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by PatrickF on 29.04.2015.
 */
public class CouchDBDumpConverter {

    public static void main(String[] args) {
        try {
            File file = new File("C:\\Users\\Patrick\\Desktop\\acra.couchdb");
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();


            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            Root r = mapper.readValue(file,Root.class);

            System.out.println(""+r);

            Importable i = new Importable();

            for (Row row : r.getRows()) {
                Map<Object,Object> doc = row.getDoc();
                doc.remove("_rev");
                i.getDocs().add(doc);
            }



            File out = new File("C:\\Users\\Patrick\\Desktop\\out2.txt");
            out.createNewFile();

            mapper.writeValue(out, i);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Importable {
        private List<Map<Object,Object>> docs = new ArrayList<Map<Object, Object>>();

        public List<Map<Object, Object>> getDocs() {
            return docs;
        }

        public void setDocs(List<Map<Object, Object>> docs) {
            this.docs = docs;
        }
    }

    public static class Root {
        @JsonProperty("total_rows")
        private Integer totalRows;
        private Integer offset;
        private List<Row> rows = new ArrayList<Row>();

        public Integer getTotalRows() {
            return totalRows;
        }

        public void setTotalRows(Integer totalRows) {
            this.totalRows = totalRows;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public List<Row> getRows() {
            return rows;
        }

        public void setRows(List<Row> rows) {
            this.rows = rows;
        }

        @Override
        public String toString() {
            return "Root{" +
                    "totalRows=" + totalRows +
                    ", offset=" + offset +
                    ", rows=" + rows +
                    '}';
        }
    }

    public static class Row {
        private String id;
        private String key;
        private Value value;
        private Map<Object,Object> doc;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Value getValue() {
            return value;
        }

        public void setValue(Value value) {
            this.value = value;
        }

        public Map<Object,Object> getDoc() {
            return doc;
        }

        public void setDoc(Map<Object,Object> doc) {
            this.doc = doc;
        }
    }

    public static class Value {
        private String rev;

        public String getRev() {
            return rev;
        }

        public void setRev(String rev) {
            this.rev = rev;
        }
    }

}
