package at.ac.tuwien.inso.indoor.sensorserver.util;

import at.ac.tuwien.inso.indoor.sensorserver.services.ServerConfig;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Created by PatrickF on 11.03.14.
 */
public class CacheUtil {

    private static MessageDigest messageDigest;


    public static EntityTag getEtag(Object obj) {
        if(obj instanceof Iterable) {
            StringBuilder s = new StringBuilder();
            for (Object o : (Iterable) obj) {
                s.append(obj.hashCode());
            }
            return new EntityTag(getEtag(md5Hex(s.toString()),false));
        }

        return new EntityTag(getEtag(obj,false));
    }

    public static EntityTag getEtag(String etag) {
        return new EntityTag(md5Hex(etag+ ServerConfig.getInstance().getEtagSalt()));
    }

    public static String getEtag(Object obj,boolean quote) {
        return quoteIfNeeded(md5Hex(String.valueOf(obj.hashCode())+ ServerConfig.getInstance().getEtagSalt()),quote);
    }

    public static Date getModifiedSince(Object obj) {
        return new Date(ServerConfig.getInstance().getIfModifiedSinceDate());
    }

    public static CacheControl getCacheControl(int defaultMaxAge) {
        CacheControl cc = new CacheControl();
        int configMaxAge = ServerConfig.getInstance().getMaxAgeCacheControl();

        if(configMaxAge < 0) {
            cc.setMaxAge(defaultMaxAge);
        } else {
            cc.setMaxAge(configMaxAge);
        }
        return cc;
    }


    private static String quoteIfNeeded(String unqoted,boolean quote) {
        if(quote) {
            unqoted = "\""+unqoted+"\"";
        }
        return unqoted;
    }

    public static String md5Hex(String subject) {
        getMessageDigest().update(subject.getBytes());
        byte[] byteData = getMessageDigest().digest();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private static MessageDigest getMessageDigest() {
        if (messageDigest == null) {
            try {
                messageDigest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Error getting message digest md5",e);
            }
        }
        return messageDigest;
    }
}
