package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper;

/**
 * Created by PatrickF on 13.09.2014.
 */
public class SuccessResponse extends BaseResponse {
    private boolean success = false;
    private String updatedRev;

    public String getUpdatedRev() {
        return updatedRev;
    }

    public void setUpdatedRev(String updatedRev) {
        this.updatedRev = updatedRev;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
