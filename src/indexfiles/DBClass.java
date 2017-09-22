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

    private static final String url = "jdbc:postgresql://localhost:5432/rza_test";
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
            stmt.executeUpdate("DROP TABLE IF EXISTS ps CASCADE;"
                    + "DROP TABLE IF EXISTS mf CASCADE;"
                    + "DROP TABLE IF EXISTS unit CASCADE;"
                    + "DROP TABLE IF EXISTS device CASCADE;"
                    + "DROP TABLE IF EXISTS osc CASCADE;"
                    + "DROP TABLE IF EXISTS file CASCADE; "
                    + "CREATE TABLE ps ("
                    + "ps_id serial PRIMARY KEY,"
                    + "ps_name varchar(15) UNIQUE"
                    + ");"
                    + "CREATE TABLE mf ("
                    + "mf_id serial PRIMARY KEY,"
                    + "mf_name varchar(100)"
                    + ");"
                    + "CREATE TABLE unit ("
                    + "unit_id serial PRIMARY KEY,"
                    + "ps_id integer references ps(ps_id),"
                    + "unit_name varchar(100)"
                    + ");"
                    + "CREATE TABLE device ("
                    + "device_id serial PRIMARY KEY, "
                    + "mf_id integer references mf(mf_id), "
                    + "device_name varchar(100), "
                    + "unit_id integer references unit(unit_id)"
                    + ");"
                    + "CREATE TABLE osc ("
                    + "osc_id serial PRIMARY KEY,"
                    + "osc_name varchar(100),"
                    + "osc_date date,"
                    + "device_id integer references device(device_id)"
                    + ");"
                    + "CREATE TABLE file ("
                    + "file_id serial PRIMARY KEY,"
                    + "osc_id integer references osc(osc_id),"
                    + "file_name varchar(100),"
                    + "file_full_path varchar(255)"
                    + ");"
                    );
            
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }

    int putInTablePS(String psName) throws SQLException {
        rs=stmt.executeQuery("INSERT INTO ps (ps_name) VALUES ('" + psName + "' ) RETURNING ps_id;");
        while(rs.next())
            return rs.getInt(1);
        return -1;
    }

    int putInTableMF(String MF) throws SQLException {
        rs=stmt.executeQuery("INSERT INTO mf (mf_name) VALUES ('" + MF + "' ) RETURNING mf_id;");
        while(rs.next())
            return rs.getInt(1);
        return -1;
    }

    int putInTableUnit(int PSId, String unitName) throws SQLException {
        rs=stmt.executeQuery("INSERT INTO unit (ps_id,unit_name) VALUES (" + PSId + ", '" + unitName + "') RETURNING unit_id;");
        while(rs.next())
            return rs.getInt(1);
        return -1;
    }

    int putInTableDevice(int MFId, String deviceName, int unitId) throws SQLException {
        rs=stmt.executeQuery("INSERT INTO device (mf_id, device_name, unit_id) VALUES ("+ MFId + ", '" + deviceName + "', " + unitId + ") RETURNING device_id;");
        while(rs.next())
            return rs.getInt(1);
        return -1;
    }
    
    int putInTableFile(int oscId, String fileName, String fullPath) throws SQLException {
         rs=stmt.executeQuery("INSERT INTO file(osc_id, file_name, file_full_path) VALUES (" + oscId + ", '" + fileName + "', '" + fullPath + "') RETURNING file_id;");while(rs.next())
            return rs.getInt(1);
        return -1;
    }
    
    int putInTableOSC(String oscName, String date, int deviceId) throws SQLException {
        rs=stmt.executeQuery("INSERT INTO osc (osc_name, osc_date, device_id) VALUES ('" + oscName + "', '" + date + "', " + deviceId +") RETURNING osc_id;");
        while(rs.next())
            return rs.getInt(1);
        return -1;
    }
    
    int newPS(String ps) throws SQLException{
        String s = "SELECT ps_id "
                + "FROM ps "
                + "WHERE ps_name = '" + ps + "';";
        rs = stmt.executeQuery(s);
        int a=0;
        while(rs.next())
            a = rs.getInt(1);

        return a;
    }
    
    int newMF(String mf) throws SQLException{
        String s = "SELECT mf_id "
                + "FROM mf "
                + "WHERE mf_name = '" + mf + "';";
        rs = stmt.executeQuery(s);
        while(rs.next())
            return rs.getInt(1);
        return 0;
    }
    
    int[] newPairUnitDeviceOnPS(String un, String dn, String ps) throws SQLException{
        
        
        String s = "SELECT unit.unit_id, device_id "
                + "FROM unit, device, ps "
                + "WHERE unit_name = '" + un + "' AND device_name = '" + dn +"' AND ps_name='" + ps + "' AND ps.ps_id=unit.ps_id AND device.unit_id = unit.unit_id;";
        rs = stmt.executeQuery(s);
        int[] ab=new int[2];
        ab[0]=ab[1]=0;
        while (rs.next()) {
            ab[0] = rs.getInt(1);
            ab[1] = rs.getInt(2);
        }
        return ab;
        
//        if(a==0&&b==0){
//            return true;
//                    }
//        return false;
    }

}
