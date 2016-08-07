package at.ac.tuwien.inso.indoor.sensorserver.services.exceptions;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.BaseResponse;
import org.apache.log4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author PatrickF
 * @since 14.01.14
 * Time: 11:56
 */
public class ExceptionHandler {
    private static Logger log = Logger.getLogger(ExceptionHandler.class);

    public static void handle(BaseResponse b, Exception e) {
        if (e instanceof InvalidAPICallException) {
            invalidCall(b, e);
        } else if (e instanceof ResourceNotFoundException) {
            notFound(b, e);
        } else if (e instanceof SensorRequestException) {
            serviceUnavailable(b,e);
        } else {
            serverError(b, e);
        }
    }

    private static void serverError(BaseResponse b, Exception e) {
        b.setStatusDescription(e.toString());
        b.setExceptionList(getExceptionList(e));
        log.error("server-error", e);
        throw new WebApplicationException(Response.status(500).entity(b).build());
    }
    private static void serviceUnavailable(BaseResponse b, Exception e) {
        b.setStatusDescription(e.toString());
        b.setExceptionList(getExceptionList(e));
        log.error("server-error: service unavailable", e);
        throw new WebApplicationException(Response.status(503).entity(b).build());
    }

    private static void notFound(BaseResponse b, Exception e) {
        b.setStatusDescription(getExceptionsDescription(e));
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(b).build());
    }

    private static void invalidCall(BaseResponse b, Exception e) {
        b.setStatusDescription(getExceptionsDescription(e));
        b.setExceptionList(getExceptionList(e));
        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(b).build());
    }

    private static void alreadyCreated(BaseResponse b, Exception e) {
        b.setStatusDescription(getExceptionsDescription(e));
        throw new WebApplicationException(Response.status(Response.Status.CONFLICT).entity(b).build());
    }
    
    private static List<String> getExceptionList(Exception e) {
        Throwable t = e;
        List<String> exceptionList = new ArrayList<String>();
        exceptionList.add(getExceptionsDescription(e));
        while((t = t.getCause()) != null) {
            exceptionList.add(getExceptionsDescription(t));
        }
        return exceptionList;
    }

    private static String getExceptionsDescription(Throwable t) {
        String description = t.getMessage();
        try {
            if(t.getStackTrace().length > 0) {
                description+=" (@"+t.getStackTrace()[0].getClassName()+" in "+t.getStackTrace()[0].getMethodName()+":"+t.getStackTrace()[0].getLineNumber()+")";
            }
        } catch (Exception e) {
            log.error("Could not get Stacktrace",e);
        }
        return description;
    }
}
