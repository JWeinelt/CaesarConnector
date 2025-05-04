package de.julianweinelt.caesar.storage;

import de.julianweinelt.caesar.CaesarConnector;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public abstract class Storage {
    public Connection conn;

    public abstract void connect();
    public abstract void disconnect();

    public abstract void sendServerData();


    public void printStackStrace(SQLException e) {
        Logger l = CaesarConnector.getInstance().getLogger();
        l.severe(e.getMessage());
        for (StackTraceElement ste : e.getStackTrace()) {
            l.severe(ste.toString());
        }
        l.severe("SQLState: " + e.getSQLState());
        l.severe("VendorError: " + e.getErrorCode());
    }
}