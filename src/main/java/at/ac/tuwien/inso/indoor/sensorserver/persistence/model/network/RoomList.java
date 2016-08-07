package at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.couchdb.TypeAbleCouchDBDocument;

import java.util.*;

/**
 * Created by PatrickF on 30.09.2014.
 */
public class RoomList extends TypeAbleCouchDBDocument{
    private String networkId;
    private List<Room> rooms = new ArrayList<Room>();
    private Map<String,String> macToRoomIdMap = new HashMap<String, String>();


    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public Map<String, String> getMacToRoomIdMap() {
        return macToRoomIdMap;
    }

    public void setMacToRoomIdMap(Map<String, String> macToRoomIdMap) {
        this.macToRoomIdMap = macToRoomIdMap;
    }

    public static class Room {
        private String roomId;
        private String name;
        private Date created;

        public Room() {
            roomId = UUID.randomUUID().toString();
            created=new Date();
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getCreated() {
            return created;
        }

        public void setCreated(Date created) {
            this.created = created;
        }


    }
}
