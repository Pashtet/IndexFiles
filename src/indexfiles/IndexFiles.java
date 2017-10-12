/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexfiles;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author pashtet
 */
public class IndexFiles {

    /**
     * @param args the command line arguments
     * @throws java.sql.SQLException
     * @throws java.io.UnsupportedEncodingException
     */
    public static void main(String[] args) throws SQLException, UnsupportedEncodingException, ParseException {
        // TODO code application logic here

        String s = "D:\\OSC";//начальный путьD:\OSC/disk1/OSC
        // /disk1/OSC

        Pars p = new Pars(s);
        p.parsOSCAll(s);
    }

}

class Pars {//для хранения рекурсивной функции

    int PSId, tempPSId, MFId, tempMFId, tempUnitId, unitId, tempDeviceId, deviceId, oscId, fileId, lastYear, lastMonth, lastDay;
    String PS, MF, fileFullPath, deviceName, fileName, unitName, year, month, day, oscName, oscDate, lastDate;
    Date lastDateD, newDate;
    DBClass DB;
    String source;
    boolean isPS, isMF, isDateUnitDevice, isLastFile, isNewDate, isLastDate;

    public Pars(String s) throws SQLException {
        File f = new File(s);//объект типа файл для 
        if (!f.exists()) {
            System.out.println("\nNot found: " + s);
        }

        if (!f.isDirectory()) {
            System.out.println("\nNot directory: " + s);
        }
        DB = new DBClass();
        DB.createNewDBAll();
//        DB.createCon();
        source = s;
    }

    // разбор ПС до нормального имени
    void parsOSCAll(String s) throws SQLException, UnsupportedEncodingException, ParseException {
        ////уровень самый верхний ПС
        File f = new File(s);
        String[] dirList = f.list();
        for (String dirList1 : dirList) {
            String dirNextLvl = s + File.separator + dirList1;
            File f1 = new File(dirNextLvl);
            String dirName = f1.getName();
            if (!f1.isFile()) {
                switch (dirName.substring(0, 3)) {
//                    case "ОМП":
//                        parsNameMF(dirName);
//                        ifNewMFNamePutInDB();
//                        parsOMP(dirNextLvl);
//                        break;
                    case "ПС_":
                        ifNewPSNamePutInDB(dirName.substring(3));
                        parsPS(dirNextLvl);
                        break;
                    default:
                        errorNoMatch(dirName);
                        break;
                }
            } else {
                errorLvlForFile(dirName);
            }
        }
    }

    void parsOMP(String s) throws SQLException {
        File f = new File(s);
        String[] dirList = f.list();
        for (String dirList1 : dirList) {
            String dirNextLvl = s + File.separator + dirList1;
            File f1 = new File(dirNextLvl);
            String dirName = f1.getName();
            ifNewPSNamePutInDB(dirName.substring(4));
            parsOMPOSC(dirNextLvl, 0);
        }
    }

    void parsOMPOSC(String s, int lvl) throws SQLException {
        File f = new File(s);
        String[] dirList = f.list();

        for (String dirList1 : dirList) {
            String nextDirLvl = s + File.separator + dirList1;
            File f1 = new File(nextDirLvl);
            String name = f1.getName();

            if (f1.isFile()) {
                ////////разберем пару присоединение+устройство
                unitName = PS + "_ОМП";
                deviceName = "ОМП";
                switch (PS) {
                    case "Бабаево":
                        if (name.substring(7, 8).equals("e")) {
                            unitName = name.substring(6, 15);//teshemlya
                        } else {
                            unitName = name.substring(6, 14);//timohino
                        }
                        break;
                    case "Чагода":
                        unitName = name.substring(10, 22);
                        break;
                    case "Шексна":
                        if (name.length() > 12) {
                            if (name.substring(10, 11).equals("S")) {
                                unitName = name.substring(10, 17);
                            } else {
                                unitName = name.substring(10, 17);
                            }
                        }
                        break;
                    default:
                        //System.out.println("Default unit and device in OMP");
                        break;
                }

                //проверка новое ли имя меняем на функцию
                ifNewPairUnitDevice(unitName, deviceName);

                oscName = name;
                oscDate = year + "-" + month + "-" + "1";
                oscId = DB.putInTableOSC(oscName, oscDate, deviceId);

                fileName = name;
                fileFullPath = nextDirLvl;
                fileId = DB.putInTableFile(oscId, fileName, fileFullPath);
                isDateUnitDevice = false;
            } else {
                switch (lvl) {
                    case 0: {
                        year = name;
                        break;
                    }
                    case 1: {
                        month = name;
                        isDateUnitDevice = true;
                        break;
                    }
                    case 2: {
                        day = name;
                        isDateUnitDevice = true;
                        break;
                    }
                    default: {
                        System.out.println("Error Level: " + nextDirLvl);
                        System.out.println(name);
                        break;
                    }
                }

                lvl++;
                parsOMPOSC(nextDirLvl, lvl); //рекурсивный вызов функции для следующего найденного файла/папки
                if (isDateUnitDevice) {
                    errorNoFiles("NO FILES! " + nextDirLvl);
                    unitName = deviceName = oscName = fileName = fileFullPath = "Дата есть а файла нет!";
                    //проверка новое ли имя?

                    ifNewPairUnitDevice(unitName, deviceName);

                    oscDate = year + "-" + month + "-" + "1";
                    oscId = DB.putInTableOSC(oscName, oscDate, deviceId);
                    fileId = DB.putInTableFile(oscId, fileName, fileFullPath);
                    isDateUnitDevice = false;
                }
                lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
            }
        }
    }

    void parsPS(String s) throws SQLException, UnsupportedEncodingException, ParseException {

        File f = new File(s);
        String[] dirList = f.list();
        for (String dirList1 : dirList) {

            String dirNextLvl = s + File.separator + dirList1;
            File f1 = new File(dirNextLvl);
            String dirName = f1.getName();

            parsNameMF(dirName);
            ifNewMFNamePutInDB();
            parsMF(dirNextLvl);

        }

    }

    void parsMF(String s) throws SQLException, UnsupportedEncodingException, ParseException {

        lastDate = DB.getLastDate(PS, MF);
        if (!lastDate.equals("")) {
            SimpleDateFormat format = new SimpleDateFormat();
            format.applyPattern("yyyy-MM-dd");
            lastDateD = format.parse(lastDate);
            System.out.println("последняя дата " + lastDateD);
            isLastDate = true;
        } else {
            isLastDate = false;
        }

        switch (MF) {
//            case "ABB":
//                lastDate = DB.getLastDate(PS, MF);
//                if (!lastDate.equals("")) {
//                    SimpleDateFormat format = new SimpleDateFormat();
//                    format.applyPattern("yyyy-MM-dd");
//                    lastDateD = format.parse(lastDate);
//                    System.out.println("последняя дата " + lastDateD);
//                    isLastDate = true;
//                } else {
//                    isLastDate = false;
//                }
//                System.out.println(PS + " " + MF);
//                parsABB(s, 0);
//                break;
//            case "Экра":
//                System.out.println(PS + " " + MF);
//                parsEkra(s, 0);
//                break;
//            case "Радиус":
//                System.out.println(PS + " " + MF);
//                parsRadius(s, 0);
//                break;
//            case "ДГК":
//                System.out.println(PS + " " + MF);
//                parsDGK(s, 0);
//                break;

            case "Парма":
                System.out.println(PS + " " + MF);
                parsParma(s, 0);
                break;
            default:
                System.out.println("Нет разбора для производителя оборудования: " + MF);
                break;
        }
    }

    void parsParma(String s, int lvl) throws SQLException, ParseException {
        isNewDate = true;
        File f = new File(s);
        String[] dirList = f.list();

        for (String dirList1 : dirList) {
            String nextDirLvl = s + File.separator + dirList1;
            File f1 = new File(nextDirLvl);
            String name = f1.getName();
            if (f1.isFile() && (lvl < 4)) {
                System.out.println("Error lvl file " + nextDirLvl);
            }
            if (f1.isFile()) {
                if (lvl > 3) {
                    //проверка новое ли имя?

                    ifNewPairUnitDevice(unitName, deviceName);

                    oscDate = year + "-" + month + "-" + day;
                    fileName = name;
                    fileFullPath = nextDirLvl;
                    int r = DB.newOSC(oscDate, PS + " " + oscName + " " + fileName);
                    if (r == 0) {
                        oscId = DB.putInTableOSC(PS + " " + oscName + " " + fileName, oscDate, deviceId);
                        fileId = DB.putInTableFile(oscId, fileName, fileFullPath);
                    }
//                    oscId = DB.putInTableOSC(oscName, oscDate, deviceId);
//
//                    fileId = DB.putInTableFile(oscId, fileName, fileFullPath);
                    isDateUnitDevice = false;
                }
            } else {
                switch (lvl) {
                    case 0: {
                        int length = PS.length();
                        unitName = deviceName = oscName = name.substring(length + 4);
                        lastDate = DB.getLastDate(PS, MF, deviceName);
                        if (!lastDate.equals("")) {
                            SimpleDateFormat format = new SimpleDateFormat();
                            format.applyPattern("yyyy-MM-dd");
                            lastDateD = format.parse(lastDate);
                            System.out.println("последняя дата " + lastDateD);
                            isLastDate = true;
                        } else {
                            isLastDate = false;
                        }
                        break;
                    }
                    case 1: {
                        year = name;
                        break;
                    }
                    case 2: {
                        month = name;
                        break;
                    }
                    case 3: {
                        day = name;
                        SimpleDateFormat format = new SimpleDateFormat();
                        format.applyPattern("yyyy-MM-dd");
                        newDate = format.parse(year + "-" + month + "-" + day);
                        //System.out.println("новая дата "+newDate);
                        if (isLastDate && (newDate.before(lastDateD))) {
                            //System.out.println("Дата старее!"+newDate);
                            isNewDate = false;
                        }
                        isDateUnitDevice = true;
                        break;
                    }
                    default: {
                        System.out.println("Error Level: " + nextDirLvl);
                        System.out.println(name);
                        break;
                    }
                }

                if (isNewDate) {
                    //System.out.println("Дата новее " + oscDate);
                    lvl++;
                    parsParma(nextDirLvl, lvl); //рекурсивный вызов функции для следующего найденного файла/папки
                    if (isDateUnitDevice) {
                        errorNoFiles("NO FILES! " + nextDirLvl);
                        String oscNameTemp = oscName;
                        fileName = fileFullPath = "Дата есть а файла нет!";

                        ifNewPairUnitDevice(unitName, deviceName);

                        oscDate = year + "-" + month + "-" + day;
                        oscName = "Нет файлов за дату " + oscDate;
                        int r = DB.newOSC(oscDate, oscName);
                        if (r == 0) {
                            oscId = DB.putInTableOSC(oscName, oscDate, deviceId);
                            fileId = DB.putInTableFile(oscId, fileName, fileFullPath);
                        }

                        isDateUnitDevice = false;
                        oscName = oscNameTemp;
                    }
                    lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
                } else {
                    isNewDate = true;
                }

            }
        }

    }

    void parsRadius(String s, int lvl) throws SQLException {
        File f = new File(s);
        String[] dirList = f.list();

        for (String dirList1 : dirList) {
            String nextDirLvl = s + File.separator + dirList1;
            File f1 = new File(nextDirLvl);
            String name = f1.getName();

            if (f1.isFile()) {

                parsUnitDeviceNameAtRadius(name);

                ifNewPairUnitDevice(unitName, deviceName);

                oscName = name;
                oscDate = year + "-" + month + "-" + day;
                oscId = DB.putInTableOSC(oscName, oscDate, deviceId);

                fileName = name;
                fileFullPath = nextDirLvl;
                fileId = DB.putInTableFile(oscId, fileName, fileFullPath);
                isDateUnitDevice = false;
            } else {
                switch (lvl) {
                    case 0: {
                        year = name;
                        break;
                    }
                    case 1: {
                        month = name;
                        break;
                    }
                    case 2: {
                        day = name;
                        isDateUnitDevice = true;
                        break;
                    }
                    default: {
                        System.out.println("Error Level: " + nextDirLvl);
                        System.out.println(name);
                        break;
                    }
                }

                lvl++;
                parsRadius(nextDirLvl, lvl); //рекурсивный вызов функции для следующего найденного файла/папки
                if (isDateUnitDevice) {
                    errorNoFiles("NO FILES! " + nextDirLvl);
                    unitName = deviceName = oscName = fileName = fileFullPath = "Дата есть а файла нет!";

                    ifNewPairUnitDevice(unitName, deviceName);

                    oscDate = year + "-" + month + "-" + day;
                    oscId = DB.putInTableOSC(oscName, oscDate, deviceId);
                    fileId = DB.putInTableFile(oscId, fileName, fileFullPath);
                    isDateUnitDevice = false;
                }
                lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
            }
        }

    }

    void parsDGK(String s, int lvl) throws SQLException {
        File f = new File(s);
        String[] dirList = f.list();

        for (String dirList1 : dirList) {
            String nextDirLvl = s + File.separator + dirList1;
            File f1 = new File(nextDirLvl);
            String name = f1.getName();

            if (f1.isFile()) {

                unitName = deviceName = name;

                ifNewPairUnitDevice(unitName, deviceName);

                oscName = name;
                oscDate = year + "-" + month + "-" + "1";
                oscId = DB.putInTableOSC(oscName, oscDate, deviceId);

                fileName = name;
                fileFullPath = nextDirLvl;
                fileId = DB.putInTableFile(oscId, fileName, fileFullPath);
                isDateUnitDevice = false;
            } else {
                switch (lvl) {
                    case 0: {
                        year = name;
                        break;
                    }
                    case 1: {
                        month = name;
                        isDateUnitDevice = true;
                        break;
                    }
                    default: {
                        System.out.println("Error Level: " + nextDirLvl);
                        System.out.println(name);
                        break;
                    }
                }

                lvl++;
                parsDGK(nextDirLvl, lvl); //рекурсивный вызов функции для следующего найденного файла/папки
                if (isDateUnitDevice) {
                    errorNoFiles("NO FILES! " + nextDirLvl);
                    unitName = deviceName = oscName = fileName = fileFullPath = "Дата есть а файла нет!";

                    ifNewPairUnitDevice(unitName, deviceName);

                    oscDate = year + "-" + month + "-" + "1";
                    oscId = DB.putInTableOSC(oscName, oscDate, deviceId);
                    fileId = DB.putInTableFile(oscId, fileName, fileFullPath);
                    isDateUnitDevice = false;
                }
                lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
            }
        }
    }

    void parsEkra(String s, int lvl) throws SQLException {
        File f = new File(s);
        String[] dirList = f.list();

        for (String dirList1 : dirList) {

            String nextDirLvl = s + File.separator + dirList1;
            File f1 = new File(nextDirLvl);
            String name = f1.getName();

            if (f1.isFile()) {
                fileName = f1.getName();
                fileFullPath = nextDirLvl;
                fileId = DB.putInTableFile(oscId, fileName, fileFullPath);
                isDateUnitDevice = false;
                ///////////////исключение папок в ЭКРЕ
            } else if (!name.equals("Com_Trade_Bab") && !name.equals("ComTrade") && !name.equals("db")) {
                switch (lvl) {
                    case 0: {
                        year = name;
                        break;
                    }
                    case 1: {
                        month = name;
                        break;
                    }
                    case 2: {
                        day = name;
                        break;
                    }
                    case 3: {
                        unitName = name;
                        //isDateUnitDevice = true;
                        break;
                    }
                    case 4: {
                        deviceName = name;

                        ifNewPairUnitDevice(unitName, deviceName);

                        break;
                    }
                    case 5: {
                        oscName = name;
                        oscDate = year + "-" + month + "-" + day;
                        oscId = DB.putInTableOSC(oscName, oscDate, deviceId);
                        isDateUnitDevice = true;
                        break;
                    }
                    default: {

                        System.out.println("Error Level: " + nextDirLvl);
                        System.out.println(name);
                        break;
                    }
                }

                lvl++;
                parsEkra(nextDirLvl, lvl); //рекурсивный вызов функции для следующего найденного файла/папки
                if (isDateUnitDevice) {
                    // System.out.println("NO FILES! " + nextDirLvl);
                    errorNoFiles("NO FILES! " + nextDirLvl);
                    fileName = fileFullPath = "Дата есть а файла нет!";

                    oscDate = year + "-" + month + "-" + day;
                    oscId = DB.putInTableOSC(oscName, oscDate, deviceId);
                    fileId = DB.putInTableFile(oscId, fileName, fileFullPath);
                    isDateUnitDevice = false;
                }
                lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
            }
        }
    }

    void parsABB(String s, int lvl) throws SQLException, ParseException {

        isNewDate = true;
        //int lvl = 0;
        File f = new File(s);
        String[] dirList = f.list();

        for (String dirList1 : dirList) {

            String nextDirLvl = s + File.separator + dirList1;
            File f1 = new File(nextDirLvl);
            String name = f1.getName();
            // System.out.println(dirList1 + "\n" + nextDirLvl + "\n" + lvl);
            if (f1.isFile()) {
                oscName = fileName = f1.getName();
                //fileFullPath = new String(nextDirLvl.getBytes("WINDOWS-1251"),"UTF-8");
                fileFullPath = nextDirLvl;

                oscDate = year + "-" + month + "-" + day;
                int r = DB.newOSC(oscDate, oscName);
                if (r == 0) {
                    oscId = DB.putInTableOSC(oscName, oscDate, deviceId);
                    fileId = DB.putInTableFile(oscId, fileName, fileFullPath);
                }
                isDateUnitDevice = false;

            } else {

                switch (lvl) {
                    case 0: {
                        year = name;
                        break;
                    }
                    case 1: {
                        month = name;
                        break;
                    }
                    case 2: {
                        day = name;
                        SimpleDateFormat format = new SimpleDateFormat();
                        format.applyPattern("yyyy-MM-dd");
                        newDate = format.parse(year + "-" + month + "-" + day);
                        //System.out.println("новая дата "+newDate);
                        if (isLastDate && (newDate.before(lastDateD))) {
                            isNewDate = false;
                        }

                        break;

                    }
                    case 3: {
                        parsUnitAndDeviceInABB(name);
                        ifNewPairUnitDevice(unitName, deviceName);
                        isDateUnitDevice = true;
                        break;
                    }
                    default: {
                        System.out.println("Error Level: " + nextDirLvl);
                        System.out.println(name);
                        break;
                    }
                }

                if (isNewDate) {
                    lvl++;
                    parsABB(nextDirLvl, lvl); //рекурсивный вызов функции для следующего найденного файла/папки
                    if (isDateUnitDevice) {
                        errorNoFiles("NO FILES! " + nextDirLvl);
                        oscDate = year + "-" + month + "-" + day;
                        oscName = MF + " " + oscDate;
                        unitName = deviceName = fileName = fileFullPath = "Дата есть а файла нет!";
                        ifNewPairUnitDevice(unitName, deviceName);
                        int r = DB.newOSC(oscDate, oscName);
                        if (r == 0) {
                            oscId = DB.putInTableOSC(oscName, oscDate, deviceId);
                            fileId = DB.putInTableFile(oscId, fileName, fileFullPath);
                        }
                        isDateUnitDevice = false;

                    }
                    lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень

                } else {
                    isNewDate = true;
                }

            }

        }
    }

    void parsUnitDeviceNameAtRadius(String name) {

        switch (PS) {

            case "Бабаево":
                switch (name.substring(4, 6)) {
                    case "Te":
                        unitName = deviceName = name.substring(4, 33);
                        break;
                    case "Ti":
                        unitName = deviceName = name.substring(4, 32);
                        break;
                    case "DZ":
                        unitName = deviceName = name.substring(4, 26);
                        break;
                    case "MV":
                        unitName = deviceName = name.substring(4, 29);
                        break;
                    case "Vv":
                        unitName = deviceName = name.substring(4, 32);
                        break;
                    case "VL":
                        unitName = deviceName = name.substring(4, 35);
                        break;
                    case "SE":
                        unitName = deviceName = name.substring(4, 26);
                        break;
                    default:
                        unitName = deviceName = name;
                        break;
                }
                break;
            case "Домозерово":
                unitName = deviceName = name;
                break;
            default:
                unitName = deviceName = name;
                break;
        }
    }

    void parsUnitAndDeviceInABB(String s) {

        if (PS.equals("Енюково")) {
            //System.out.println(s);
            switch (s.substring(0, 3)) {
                case "VV_":
                    unitName = s.substring(0, 8);
                    deviceName = "REF615";
                    break;
                case "DZT":
                    unitName = s.substring(0, 6);
                    deviceName = "RET615";
                    break;
                case "RZT":
                    unitName = s.substring(0, 6);
                    deviceName = "REU615";
                    break;
                case "SVV":
                    unitName = s.substring(0, 6);
                    deviceName = "REF615";
                    break;
                case "ATR":
                    unitName = s.substring(0, 7);
                    deviceName = "REF615";
                    break;
                default:
                    errorNoMatch(s);
                    break;
            }

        } else if (PS.equals("Климовская")) {
            deviceName = s.substring(0, 6);
            switch (s.substring(6, 10)) {
                case "_АЧР":
                    unitName = "АЧР";
                    break;
                case "ВЛ35":
                    unitName = s.substring(6, 15);
                    break;
                case "_35_":
                    unitName = "ВВ" + s.substring(6, 12);
                    break;
                case "_10_":
                    unitName = "ВВ" + s.substring(6, 12);
                    break;
                default:
                    unitName = s.substring(7, 13);
                    break;
            }
        }
    }

    void parsNameMF(String s) {
        switch (s.substring(0, 2).toLowerCase()) {
            case "db":
                this.MF = "db";
                break;
            case "br":
                this.MF = "Бреслер";
                break;
            case "эк":
                this.MF = "Экра";
                break;
            case "па":
                this.MF = "Парма";
                break;
            case "ра": {
                if (s.equals("Радиус_Старт")) {
                    this.MF = "Радиус_Старт";
                } else {
                    this.MF = "Радиус";
                }
                break;
            }
            case "ст": {
                this.MF = "Старт_db";
                break;
            }
            case "дг":
                this.MF = "ДГК";
                break;
            case "ab":
                this.MF = "ABB";
                break;
            case "sk":
                this.MF = "SKI";
                break;
            case "ом":
                this.MF = "ОМП";
                break;
            default:
                MF = "default";
                errorNoMatch(s);
                break;
        }
    }

    void ifNewMFNamePutInDB() throws SQLException {
        int r = DB.newMF(MF);
        if (r == 0) {
            MFId = DB.putInTableMF(MF);
        } else {
            MFId = r;
        }
    }

    void ifNewPSNamePutInDB(String s) throws SQLException {
        int r = DB.newPS(s);
        if (r == 0) {
            PSId = DB.putInTablePS(s);
        } else {
            PSId = r;
        }
        PS = s;
        System.out.println(PS);
    }

    void ifNewPairUnitDevice(String unitName, String deviceName) throws SQLException {

        int[] ab = DB.newPairUnitDeviceOnPS(unitName, deviceName, PS);
        if (ab[0] == 0 || ab[1] == 0) {
            unitId = DB.putInTableUnit(PSId, unitName);
            deviceId = DB.putInTableDevice(MFId, deviceName, unitId);
        } else {
            unitId = ab[0];
            deviceId = ab[1];
        }

    }

    void errorNoFiles(String message) {
        System.out.println(message);
    }

    void errorNoMatch(String message) {
        System.out.println("Нет разбора для " + message);
    }

    void errorLvlForFile(String message) {
        System.out.println("Файл не на том уровен: " + message);
    }

}
