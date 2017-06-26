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
                    + "ps_id integer primary key,"
                    + "ps_name varchar(15)"
                    + ");"
                    + "CREATE TABLE mf ("
                    + "mf_id integer primary key,"
                    + "mf_name varchar(100)"
                    + ");"
                    + "CREATE TABLE unit ("
                    + "unit_id integer primary key,"
                    + "ps_id integer references ps(ps_id),"
                    + "unit_name varchar(100)"
                    + ");"
                    + "CREATE TABLE device ("
                    + "device_id integer primary key, "
                    + "mf_id integer references mf(mf_id), "
                    + "device_name varchar(100), "
                    + "unit_id integer references unit(unit_id)"
                    + ");"
                    + "CREATE TABLE osc ("
                    + "osc_id integer primary key,"
                    + "osc_name varchar(100),"
                    + "osc_date date,"
                    + "device_id integer references device(device_id)"
                    + ");"
                    + "CREATE TABLE file ("
                    + "file_id integer primary key,"
                    + "osc_id integer references osc(osc_id),"
                    + "file_name varchar(100),"
                    + "file_full_path varchar(255)"
                    + ");"
                    );
            
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }

    void createCon() {
        try {
            con = DriverManager.getConnection(url, user, password);//dbh
            stmt = con.createStatement();
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }

    void PutInTablePS(int PSId, String psName) throws SQLException {
        stmt.executeUpdate("INSERT INTO ps (ps_id, ps_name) VALUES (" + PSId + ", '" + psName + "' );");
    }

    void PutInTableMF(int MFId, String MF) throws SQLException {
        stmt.executeUpdate("INSERT INTO mf (mf_id, mf_name) VALUES (" + MFId + ", '" + MF + "' );");
    }

    void putInTableUnit(int unitId, int PSId, String unitName) throws SQLException {
        stmt.executeUpdate("INSERT INTO unit VALUES (" + unitId + "," + PSId + ", '" + unitName + "');");
    }

    void putInTableDevice(int d, int m, String name, int u) throws SQLException {
        stmt.executeUpdate("INSERT INTO device VALUES (" + d + "," + m + ", '" + name + "', " + u + ");");
    }

    void putInTableDeviceUnit(int u, int d) throws SQLException{
        stmt.executeUpdate("INSERT INTO device_unit VALUES (" + u + "," + d + ");");
    }
    
    void putInTableFile(int f, int o, String n, String p) throws SQLException {
        stmt.executeUpdate("INSERT INTO file VALUES (" + f +", " + o + ", '" + n + "', '" + p + "');");
    }
    
    void putInTableOSC(int o, String n, String date, int d) throws SQLException {
        stmt.executeUpdate("INSERT INTO osc VALUES (" + o + ",'" + n + "', '" + date + "', " + d +");");
    }

    void closeCon() {

        //close connection ,stmt and resultset here
        try {
            con.close();
        } catch (SQLException se) {
            /*can't do anything */ }

    }
    
    boolean newPS(String ps) throws SQLException{
        String s = "SELECT ps_id "
                + "FROM ps "
                + "WHERE ps_name = '" + ps + "';";
        rs = stmt.executeQuery(s);
        int a=0;
        while(rs.next())
            a = rs.getInt(1);
        if (a==0)
            return true;
        return false;
    }
    
    int getPSId(String ps) throws SQLException{
        String s = "SELECT ps_id "
                + "FROM ps "
                + "WHERE ps_name = '" + ps + "';";
        rs=stmt.executeQuery(s);
        int a=0;
        while (rs.next()){
            a=rs.getInt(1);
        }
        return a;
    }
    
    boolean newMF(String mf) throws SQLException{
        String s = "SELECT mf_id "
                + "FROM mf "
                + "WHERE mf_name = '" + mf + "';";
        rs = stmt.executeQuery(s);
        int a=0;
        while(rs.next())
            a = rs.getInt(1);
        if (a==0)
            return true;
        return false;
    }
    
    int getMFId(String mf) throws SQLException{
        String s = "SELECT mf_id "
                + "FROM mf "
                + "WHERE mf_name = '" + mf + "';";
        rs=stmt.executeQuery(s);
        int a=0;
        while (rs.next()){
            a=rs.getInt(1);
        }
        return a;
    }
    
    boolean newPairUnitDeviceOnPS(String un, String dn, String ps) throws SQLException{
        String s = "SELECT unit.unit_id, device_id "
                + "FROM unit, device, ps "
                + "WHERE unit_name = '" + un + "' AND device_name = '" + dn +"' AND ps_name='"+ps+"' AND ps.ps_id=unit.ps_id AND device.unit_id = unit.unit_id;";
        rs = stmt.executeQuery(s);
        
        int a=0, b=0;
        while (rs.next()) {
            a = rs.getInt(1);
            b = rs.getInt(2);
        }
        if(a==0&&b==0){
            return true;
                    }
        return false;
    }
    
    int[] getUnitAndDeviceId(String un, String dn) throws SQLException{
        String s = "SELECT unit.unit_id, device_id "
                + "FROM unit, device "
                + "WHERE unit_name = '" + un + "' AND device_name = '" + dn +"';";
        rs = stmt.executeQuery(s);
        
        int a=0, b=0;
        while (rs.next()) {
            a = rs.getInt(1);
            b = rs.getInt(2);
        }
        int[] ab=new int[2];
        ab[0] = a;
        ab[1]=b;
        return ab;
    }
    
    void createConAbb() {
        try {
            // opening database connection to MySQL server
            con = DriverManager.getConnection(url, user, password);//dbh
            // getting Statement object to execute query
            stmt = con.createStatement();
            stmt.executeUpdate("DROP TABLE IF EXISTS abb ");
            stmt.executeUpdate("CREATE TABLE abb ("
                    + "fullPath VARCHAR(255),"
                    + "mf VARCHAR(30),"
                    + "ps VARCHAR(50),"
                    + "date DATE,"
                    + "nameProt VARCHAR(100),"
                    + "nameDev VARCHAR(100),"
                    + "nameFile VARCHAR (150))");
            ps = con.prepareStatement("INSERT INTO abb VALUES(?,?,?,?,?,?,?)");
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }

    void createConParma() throws SQLException {
        // opening database connection to MySQL server
        con = DriverManager.getConnection(url, user, password);//dbh

        // getting Statement object to execute query
        stmt = con.createStatement();
        stmt.executeUpdate("DROP TABLE IF EXISTS parma ");
        stmt.executeUpdate("CREATE TABLE parma ("
                + "fullPath VARCHAR(255),"
                + "mf VARCHAR(30),"
                + "ps VARCHAR(50),"
                + "date DATE,"
                + "nameProt VARCHAR(100),"
                + "nameDev VARCHAR(100),"
                + "nameFile VARCHAR (150))");
        ps = con.prepareStatement("INSERT INTO Parma VALUES(?,?,?,?,?,?,?)");
    }

    void PutInTableEvent(String[] s, int eventId, int MFId) throws SQLException {

        ps = con.prepareStatement("INSERT INTO event VALUES(?,?,?,?,?,?,?)");
        ps.setInt(1, eventId);
        ps.setInt(2, MFId);
        ps.setDate(3, java.sql.Date.valueOf(s[0] + "-" + s[1] + "-" + s[2]));//ложим дату отдельно

        for (int i = 4; i <= 7; i++) {
            ps.setString(i, s[i - 1]);
        }
        ps.executeUpdate();

    }

    void PutInTableAbb(String[] s, int i) throws SQLException {

        for (int j = 1; j <= (s.length - 6); j++) {//Смещение 
            ps.setString((j), s[j - 1]);
        }

        ps.setDate(4, java.sql.Date.valueOf(s[3] + "-" + s[4] + "-" + s[5]));//ложим дату отдельно

        for (int j = 5; j <= (s.length - 2); j++) {// и остальные записи отдельно
            ps.setString((j), s[j + 1]);
        }

        ps.executeUpdate();

    }

    void PutInTableParma(String[] s, int i) throws SQLException {

        for (int j = 1; j <= (s.length - 6); j++) {//Смещение 
            ps.setString((j), s[j - 1]);
        }

        ps.setDate(4, java.sql.Date.valueOf(s[3] + "-" + s[4] + "-" + s[5]));//ложим дату отдельно
        for (int j = 5; j <= (s.length - 2); j++) {// и остальные записи отдельно
            ps.setString((j), s[j + 1]);
        }
        ps.executeUpdate();

    }
}
