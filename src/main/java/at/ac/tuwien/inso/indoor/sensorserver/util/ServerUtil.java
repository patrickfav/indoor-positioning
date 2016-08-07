package at.ac.tuwien.inso.indoor.sensorserver.util;

import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.InvalidAPICallException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author PatrickF
 * @since 18.11.13
 * Time: 16:02
 */
public class ServerUtil {
    private static Logger log = Logger.getLogger(ServerUtil.class);

    public static String prettyPrint(String uglyJson) {
        try {
            return jsonPrettyPrint(uglyJson.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return jsonPrettyPrint(uglyJson.getBytes());
        }
    }

    public static String createISO8601UTCDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    public static String jsonPrettyPrint(byte[] uglyJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            JsonNode tree = objectMapper.readTree(new String(uglyJson, "UTF-8"));
            return objectMapper.writeValueAsString(tree);
        } catch (Exception e) {
            return new String(uglyJson);
        }
    }

    public static byte[] decompressGzip(byte[] compressedContent) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPInputStream gzipInputStream = null;
        byte[] returnBuffer = {};
        try {
            int len;
            byte buffer[] = new byte[2048];
            gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressedContent));

            while ((len = gzipInputStream.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }

            gzipInputStream.close();
            returnBuffer = bos.toByteArray();
            bos.close();
        } catch (Exception e) {
            log.warn("error while decompress gzip", e);
        }

        return returnBuffer;
    }


    public static byte[] compressGzip(byte[] content) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(content.length);
        GZIPOutputStream gzipOutputStream = null;
        byte[] returnBuffer = {};
        try {
            gzipOutputStream = new GZIPOutputStream(bos);
            gzipOutputStream.write(content);
            gzipOutputStream.close();
            returnBuffer = bos.toByteArray();
            bos.close();
        } catch (Exception e) {
            log.warn("error while compress gzip", e);
        }

        return returnBuffer;
    }

    public static String foldTooLongString(String content, int maxLength) {
        if (content.length() > maxLength) {
            String cutOutJson = content.substring(0, maxLength / 2) + "\r\n\r\n...\r\n\r\n";
            cutOutJson += content.substring(content.length() - (maxLength / 2), content.length());
            return cutOutJson;
        } else {
            return content;
        }
    }

    public static String implode(String seperator, Object[] objectArray) {
        List<String> list = new ArrayList<String>();
        for (Object o : objectArray) {
            list.add(o.toString());
        }

        return implode(seperator, list);
    }

    public static String implode(String separator, List<String> data) {
        StringBuilder sb = new StringBuilder();
        if (data.size() > 0) {
            for (int i = 0; i < data.size() - 1; i++) {
                if (!data.get(i).isEmpty()) {
                    sb.append(data.get(i));
                    sb.append(separator);
                }
            }
            sb.append(data.get(data.size() - 1));
        }
        return sb.toString();
    }


    public static String readFile(File f, boolean keepNewlines) {
        StringBuilder contents = new StringBuilder();

        if (!f.exists()) {
            log.error("File " + f + " does not exist");
            return "";
        }

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    if (keepNewlines) {
                        contents.append("\n");
                    }
                }
            } finally {
                input.close();
            }
        } catch (IOException e) {
            log.warn("could not read file " + f, e);
        }
        return contents.toString();
    }

    public static List<String> readFileAndGetAllLines(File f,boolean ommitHashComments) {
        List<String> lines = new ArrayList<String>();
        if (!f.exists()) {
            log.error("File " + f + " does not exist");
            return lines;
        }

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    if(!ommitHashComments || !line.startsWith("#")) {
                        lines.add(line.trim());
                    }
                }
            } finally {
                input.close();
            }
        } catch (IOException e) {
            log.warn("could not read file " + f, e);
        }
        return lines;
    }

    public static String getImplementationVersionFromManifest(ServletContext context) {
        Properties prop = new Properties();
        try {
            prop.load(new FileReader(new File(context.getRealPath("/") + "META-INF/MANIFEST.MF")));
        } catch (IOException e) {
            log.error("Could not read", e);
            return "";
        }
        return prop.getProperty("Implementation-Version", "");
    }

    public static Map<String, String> getQueryStringMap(String urlString) {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        try {
            URL url = new URL(urlString);
            String query = url.getQuery();
            if(query != null && !query.isEmpty()) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            }
        } catch (Exception e) {
            log.error("Could not getQueryStringMap for "+urlString,e);
        }
        return query_pairs;
    }

    public static String createQueryString(Map<String,String> queryMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("?");
        for (String key : queryMap.keySet()) {
            try {
                sb.append(URLEncoder.encode(key, "UTF-8")+"="+ URLEncoder.encode(queryMap.get(key), "UTF-8")+"&");
            } catch (Exception e) {
                log.warn("could not url encode ("+key+"="+queryMap.get(key)+")",e);
            }
        }
        return sb.toString().substring(0,sb.toString().length()-1);
    }

    public static String getStringFromInputStream(InputStream is,boolean keepNewlines) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
                if(keepNewlines) {
                    sb.append("\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

    public static String getDateTimeString(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss");
        return sdf.format(d);
    }
    public static String getDateString(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd");
        return sdf.format(d);
    }

    public static void checkParameter(RestParam... nonNullParams) throws InvalidAPICallException {
        for (RestParam nonNullParam : nonNullParams) {
            if (nonNullParam.getValue() == null) {
                throw new InvalidAPICallException(nonNullParam.getName() + " was null");
            }
        }
    }

    public static String getFileExtension(String filename) {
        if(filename == null || filename.isEmpty()) {
            return "";
        }

        String extension = "";
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i+1);
        }
        return extension;
    }

	public static double round(double value, int digits) {
		BigDecimal bd = new BigDecimal(value).setScale(digits, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

    public static class RestParam {
        private final String name;
        private final Object value;

        public RestParam(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }
    }
}
