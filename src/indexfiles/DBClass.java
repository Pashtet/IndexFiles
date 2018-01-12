/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexfiles;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author pashtet
 */
public class DBClass {

    private static final String url = "jdbc:postgresql://localhost:5433/rza";
    private static final String user = "netbeans";
    private static final String password = "netbeans";

    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;

    PreparedStatement ps;
//создаем таблицы

    void createNewDBAll() {
        try {
            con = DriverManager.getConnection(url, user, password);//dbh
            stmt = con.createStatement();
            stmt.executeUpdate(
                    "DROP TABLE IF EXISTS ps CASCADE;"
                    + "DROP TABLE IF EXISTS mf CASCADE;"
                    + "DROP TABLE IF EXISTS unit CASCADE;"
                    + "DROP TABLE IF EXISTS device CASCADE;"
                    + "DROP TABLE IF EXISTS osc CASCADE;"
                    + "DROP TABLE IF EXISTS file CASCADE;"
                    + "DROP TABLE IF EXISTS file CASCADE; "
                    + "CREATE TABLE IF NOT EXISTS ps ("
                    + "ps_id serial PRIMARY KEY,"
                    + "ps_name varchar(15) UNIQUE"
                    + ");"
                    + "CREATE TABLE IF NOT EXISTS mf ("
                    + "mf_id serial PRIMARY KEY,"
                    + "mf_name varchar(100)"
                    + ");"
                    + "CREATE TABLE IF NOT EXISTS unit ("
                    + "unit_id serial PRIMARY KEY,"
                    + "ps_id integer references ps(ps_id),"
                    + "unit_name varchar(100)"
                    + ");"
                    + "CREATE TABLE IF NOT EXISTS device ("
                    + "device_id serial PRIMARY KEY, "
                    + "mf_id integer references mf(mf_id), "
                    + "device_name varchar(100), "
                    + "unit_id integer references unit(unit_id)"
                    + ");"
                    + "CREATE TABLE IF NOT EXISTS osc_file ("
                    + "file_id serial PRIMARY KEY, "
                    + "osc_name varchar(150), "
                    + "osc_date date, "
                    + "file_name varchar(100), "
                    + "full_path varchar(255) UNIQUE, "
                    + "device_id integer references device(device_id)"
                    + ");"
            );

        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }

    void createCon() throws SQLException {
        con = DriverManager.getConnection(url, user, password);//dbh
        stmt = con.createStatement();
    }

    int putInTablePS(String psName) throws SQLException {
        rs = stmt.executeQuery("INSERT INTO ps (ps_name) VALUES ('" + psName + "' ) RETURNING ps_id;");
        while (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    int putInTableMF(String MF) throws SQLException {
        rs = stmt.executeQuery("INSERT INTO mf (mf_name) VALUES ('" + MF + "' ) RETURNING mf_id;");
        while (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    int putInTableUnit(int PSId, String unitName) throws SQLException {
        rs = stmt.executeQuery("INSERT INTO unit (ps_id,unit_name) VALUES (" + PSId + ", '" + unitName + "') RETURNING unit_id;");
        while (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    int putInTableDevice(int MFId, int unitId, String deviceName) throws SQLException {
        rs = stmt.executeQuery("INSERT INTO device (mf_id, device_name, unit_id) VALUES (" + MFId + ", '" + deviceName + "', " + unitId + ") RETURNING device_id;");
        while (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    int putInTableOsc_file(String oscName, String date, String fileName, String fullPath, int deviceId) throws SQLException {
        rs = stmt.executeQuery("INSERT INTO osc_file(osc_name, osc_date, file_name, full_path, device_id) "
                + "VALUES ('" + oscName + "', '" + date + "', '" + fileName + "', '" + fullPath + "', '" + deviceId + "' ) "
                + "ON CONFLICT (full_path) DO NOTHING RETURNING file_id;");
        while (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    int putInTableFile(int oscId, String fileName, String fullPath) throws SQLException {
        rs = stmt.executeQuery("INSERT INTO file(osc_id, file_name, file_full_path) VALUES (" + oscId + ", '" + fileName + "', '" + fullPath + "') "
                + "ON CONFLICT (file_full_path) DO NOTHING RETURNING file_id;");
//         
        while (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    int putInTableOSC(String oscName, String date, int deviceId) throws SQLException {
        rs = stmt.executeQuery("INSERT INTO osc (osc_name, osc_date, device_id) VALUES ('" + oscName + "', '" + date + "', " + deviceId + ") "
                + "ON CONFLICT (osc_name) DO NOTHING RETURNING osc_id;");
//         
        while (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    int newPS(String ps) throws SQLException {
        String s = "SELECT ps_id "
                + "FROM ps "
                + "WHERE ps_name = '" + ps + "';";
        rs = stmt.executeQuery(s);
        int a = 0;
        while (rs.next()) {
            a = rs.getInt(1);
        }

        return a;
    }

    int newMF(String mf) throws SQLException {
        String s = "SELECT mf_id "
                + "FROM mf "
                + "WHERE mf_name = '" + mf + "';";
        rs = stmt.executeQuery(s);
        while (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    int newOSC(String oscDate, String fileFullPath) throws SQLException {
        rs = stmt.executeQuery("SELECT osc_id FROM osc, file WHERE osc_date='" + oscDate + "' AND file_full_path='" + fileFullPath + "' AND osc.osc_id=file.osc_id;");
        while (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    int newFile(String fileFullPath) throws SQLException {
        rs = stmt.executeQuery("SELECT file_id FROM file WHERE file_full_path='" + fileFullPath + "';");
        while (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    int newOSC(String PS, String MF, String unit, String device, String oscDate, String oscName) throws SQLException {
        String s = "SELECT osc_id FROM device, osc "
                + "WHERE device_name='" + device + "' AND osc_date='" + oscDate + "' AND osc_name= '" + oscName + "' "
                + "AND osc.device_id=device.device_id;";
        rs = stmt.executeQuery(s);
        while (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    int newOSC(String oscName, String oscDate, int deviceId) throws SQLException {
        String s = "SELECT osc_id FROM osc "
                + "WHERE device_id='" + deviceId + "' AND osc_date='" + oscDate + "' AND osc_name= '" + oscName + "';";
        rs = stmt.executeQuery(s);
        while (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    int[] newPairUnitDeviceOnPS(String un, String dn, String ps) throws SQLException {

        String s = "SELECT unit.unit_id, device_id "
                + "FROM unit, device, ps "
                + "WHERE unit_name = '" + un + "' AND device_name = '" + dn + "' AND ps_name='" + ps + "' AND ps.ps_id=unit.ps_id AND device.unit_id = unit.unit_id;";
        rs = stmt.executeQuery(s);
        int[] ab = new int[2];
        ab[0] = ab[1] = 0;
        while (rs.next()) {
            ab[0] = rs.getInt(1);
            ab[1] = rs.getInt(2);
        }
        return ab;
    }

    int newUnit(int PSId, String unitName) throws SQLException {
        rs = stmt.executeQuery("SELECT unit.unit_id "
                + "FROM unit "
                + "WHERE unit_name='" + unitName + "' AND ps_id = '" + PSId + "';");
        while (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    int newDevice(int MFId, int unitId, String deviceName) throws SQLException {
        rs = stmt.executeQuery("SELECT device_id "
                + "FROM device "
                + "WHERE device_name='" + deviceName + "' AND mf_id = '" + MFId + "' AND unit_id='" + unitId + "';");
        while (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    String getLastDate(String ps, String mf) throws SQLException {
        rs = stmt.executeQuery("SELECT max(osc_date) "
                + "FROM osc_file, device, unit, mf, ps "
                + "WHERE ps_name='" + ps + "' AND mf_name='" + mf + "' "
                + "AND osc_file.device_id=device.device_id AND unit.unit_id=device.unit_id AND ps.ps_id=unit.ps_id AND mf.mf_id=device.mf_id;");
        while (rs.next()) {
            String t = rs.getString(1);
            if (t != null) {
                return t;
            } else {
                return "";
            }
        }

        return "";
    }

    String getLastDate(String ps, String mf, String deviceName) throws SQLException {
        rs = stmt.executeQuery("SELECT max(osc_date) "
                + "FROM osc_file, device, unit, mf, ps "
                + "WHERE ps_name='" + ps + "' AND mf_name='" + mf + "' AND device_name='" + deviceName + "' "
                + "AND osc_file.device_id=device.device_id AND unit.unit_id=device.unit_id AND ps.ps_id=unit.ps_id AND mf.mf_id=device.mf_id;");
        while (rs.next()) {
            String t = rs.getString(1);
            if (t != null) {
                return t;
            } else {
                return "";
            }
        }

        return "";
    }
}
