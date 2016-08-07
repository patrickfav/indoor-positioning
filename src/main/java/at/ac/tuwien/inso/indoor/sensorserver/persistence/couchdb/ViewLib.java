package at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb;

import org.ektorp.support.DesignDocument;

/**
 * Created by PatrickF on 30.01.14.
 */
public class ViewLib {
    public static ViewWrapper getByNetworkId(String name, Class clazz) {
        return new ViewWrapper(name,new DesignDocument.View("function(doc) { if (doc.dbType == '"+clazz.getSimpleName()+"' ) emit( doc.networkId, doc._id )}"));
    }
    public static ViewWrapper getByNodeId(String name, Class clazz) {
        return new ViewWrapper(name,new DesignDocument.View("function(doc) { if (doc.dbType == '"+clazz.getSimpleName()+"' ) emit( doc.nodeId, doc._id )}"));
    }
    public static ViewWrapper getByJobId(String name, Class clazz) {
        return new ViewWrapper(name,new DesignDocument.View("function(doc) { if (doc.dbType == '"+clazz.getSimpleName()+"' ) emit( doc.jobId, doc._id )}"));
    }

    public static ViewWrapper getByNetworkIdSortByDateAndLimit(String name, Class clazz) {
        return new ViewWrapper(name,new DesignDocument.View("function(doc) { if (doc.dbType == '"+clazz.getSimpleName()+"' ) emit([doc.networkId,doc.created], doc._id )}"));
    }

    public static ViewWrapper getByNodeIdAndAdapterSortByDateAndLimit(String name, Class clazz) {
        return new ViewWrapper(name,new DesignDocument.View("function(doc) { if (doc.dbType == '"+clazz.getSimpleName()+"' ) emit([doc.nodeId,doc.adapter,doc.created], doc._id )}"));
    }
    public static ViewWrapper getByNodeIdSortByDateAndLimit(String name, Class clazz) {
        return new ViewWrapper(name,new DesignDocument.View("function(doc) { if (doc.dbType == '"+clazz.getSimpleName()+"' ) emit([doc.nodeId,doc.created], doc._id )}"));
    }
    public static ViewWrapper getByAnalysisId(String name, Class clazz) {
        return new ViewWrapper(name,new DesignDocument.View("function(doc) { if (doc.dbType == '"+clazz.getSimpleName()+"' ) emit( doc.analysisId, doc._id )}"));
    }
    public static ViewWrapper getByPinnedActive(String name, Class clazz) {
        return new ViewWrapper(name,new DesignDocument.View("function(doc) { if (doc.dbType == '"+clazz.getSimpleName()+"' ) emit( doc.pinnedActive, doc._id )}"));
    }

    public static class ViewWrapper {
        private final String name;
        private final DesignDocument.View view;

        public ViewWrapper(String name, DesignDocument.View view) {
            this.name = name;
            this.view = view;
        }

        public String getName() {
            return name;
        }

        public DesignDocument.View getView() {
            return view;
        }
    }
}