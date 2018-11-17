package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PatrickF on 08.09.2014.
 */
public class BaseResponse {
    private String statusDescription = "OK";
    private List<String> exceptionList = new ArrayList<String>();

    public BaseResponse() {
    }

    public List<String> getExceptionList() {
        return exceptionList;
    }

    public void setExceptionList(List<String> exceptionList) {
        this.exceptionList = exceptionList;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }
}
