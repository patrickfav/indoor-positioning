package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.Survey;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement.SurveyStatistics;

import java.util.List;

/**
 * Created by PatrickF on 18.09.2014.
 */
public class SurveyStatWrapper {
    private List<Survey> surveyList;
    private List<SurveyStatistics> statistics;

    public List<Survey> getSurveyList() {
        return surveyList;
    }

    public void setSurveyList(List<Survey> surveyList) {
        this.surveyList = surveyList;
    }

    public List<SurveyStatistics> getStatistics() {
        return statistics;
    }

    public void setStatistics(List<SurveyStatistics> statistics) {
        this.statistics = statistics;
    }
}
