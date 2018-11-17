package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.measurement;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by PatrickF on 23.09.2014.
 */
public class NetworkGraph {
    private Set<NetworkNode> nodes = new HashSet<NetworkNode>();
    private Set<NetworkEdge> edges = new HashSet<NetworkEdge>();

    public Set<NetworkNode> getNodes() {
        return nodes;
    }

    public void setNodes(Set<NetworkNode> nodes) {
        this.nodes = nodes;
    }

    public Set<NetworkEdge> getEdges() {
        return edges;
    }

    public void setEdges(Set<NetworkEdge> edges) {
        this.edges = edges;
    }

    public static class NetworkNode {
        private String id;
        private String label;
        private Point kkLayoutPoint;
        private Point frLayoutPoint;
        private Point isomLayoutPoint;
        private Point springLayoutPoint;
        private Integer edgesCount;
        private boolean isManagedNode;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public boolean isManagedNode() {
            return isManagedNode;
        }

        public void setManagedNode(boolean isManagedNode) {
            this.isManagedNode = isManagedNode;
        }

        public Point getKkLayoutPoint() {
            return kkLayoutPoint;
        }

        public void setKkLayoutPoint(Point kkLayoutPoint) {
            this.kkLayoutPoint = kkLayoutPoint;
        }

        public Point getFrLayoutPoint() {
            return frLayoutPoint;
        }

        public void setFrLayoutPoint(Point frLayoutPoint) {
            this.frLayoutPoint = frLayoutPoint;
        }

        public Point getIsomLayoutPoint() {
            return isomLayoutPoint;
        }

        public void setIsomLayoutPoint(Point isomLayoutPoint) {
            this.isomLayoutPoint = isomLayoutPoint;
        }

        public Point getSpringLayoutPoint() {
            return springLayoutPoint;
        }

        public void setSpringLayoutPoint(Point springLayoutPoint) {
            this.springLayoutPoint = springLayoutPoint;
        }

        public Integer getEdgesCount() {
            return edgesCount;
        }

        public void setEdgesCount(Integer edgesCount) {
            this.edgesCount = edgesCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NetworkNode that = (NetworkNode) o;

            if (isManagedNode != that.isManagedNode) return false;
            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (label != null ? !label.equals(that.label) : that.label != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (label != null ? label.hashCode() : 0);
            result = 31 * result + (isManagedNode ? 1 : 0);
            return result;
        }
    }

    public static class NetworkEdge {
        private String id;
        private String source;
        private String target;
        private Double signalStrengthDbm;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public Double getSignalStrengthDbm() {
            return signalStrengthDbm;
        }

        public void setSignalStrengthDbm(Double signalStrengthDbm) {
            this.signalStrengthDbm = signalStrengthDbm;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NetworkEdge that = (NetworkEdge) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (signalStrengthDbm != null ? !signalStrengthDbm.equals(that.signalStrengthDbm) : that.signalStrengthDbm != null)
                return false;
            if (source != null ? !source.equals(that.source) : that.source != null) return false;
            if (target != null ? !target.equals(that.target) : that.target != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (source != null ? source.hashCode() : 0);
            result = 31 * result + (target != null ? target.hashCode() : 0);
            result = 31 * result + (signalStrengthDbm != null ? signalStrengthDbm.hashCode() : 0);
            return result;
        }
    }

    public static class Point {
        private double x;
        private double y;

        public Point() {
        }

        public Point(Point2D point2D) {
            this.x = point2D.getX();
            this.y = point2D.getY();
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }
}
