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

        String s = "D:\\OSC";//начальный путьD:\OSC/disk1/OSC
        // /disk1/OSC

        Pars p = new Pars(s);
        p.parsOSCAll(s);
    }

}

class Pars {//для хранения рекурсивной функции

    int PSId, tempPSId, MFId, tempMFId, tempUnitId, unitId, tempDeviceId, deviceId, oscId, fileId, lastYear, lastMonth, lastDay;
    String PS, MF, fullPath, deviceName, fileName, unitName, year, month, day, oscName, oscDate, lastDate;
    Date lastDateD, newDate;
    DBClass DB;
    String source;
    boolean isPS, isMF, isDateUnitDevice, isLastFile, isNewDate, isLastDate, isDBEmpty;

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
                    case "ОМП":
                        parsNameMF(dirName);
                        ifNewMFNamePutInDB();
                        parsOMP(dirNextLvl);
                        break;
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

    void parsOMP(String s) throws SQLException, ParseException {
        File f = new File(s);
        String[] dirList = f.list();
        for (String dirList1 : dirList) {
            String dirNextLvl = s + File.separator + dirList1;
            File f1 = new File(dirNextLvl);
            String dirName = f1.getName();
            ifNewPSNamePutInDB(dirName.substring(4));
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
            parsOMPOSC(dirNextLvl, 0);
        }
    }

    void parsOMPOSC(String s, int lvl) throws SQLException, ParseException {

        isNewDate = true;
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
                ifNewUnitPutInDB();
                ifNewDevicePutInDB();

                oscDate = year + "-" + month + "-" + day;
                oscName = name;
                fileName = name;
                fullPath = nextDirLvl;
                ifNewFilePutInDB();
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
                        SimpleDateFormat format = new SimpleDateFormat();
                        format.applyPattern("yyyy-MM-dd");
                        newDate = format.parse(year + "-" + month + "-" + day);
                        if (isLastDate && (newDate.before(lastDateD))) {
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
                    lvl++;
                    parsOMPOSC(nextDirLvl, lvl); //рекурсивный вызов функции для следующего найденного файла/папки
                    if (isDateUnitDevice) {
                        errorNoFiles("NO FILES! " + nextDirLvl);
                        unitName = deviceName = fileName = "Дата есть а файла нет!";
                        ifNewUnitPutInDB();
                        ifNewDevicePutInDB();
                        oscDate = year + "-" + month + "-" + day;
                        oscName = " дата есть а файла нет ";

                        ifNewFilePutInDB();

                        isDateUnitDevice = false;
                    }
                    lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
                } else {
                    isNewDate = true;
                    isDateUnitDevice = false;
                }
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
        System.out.println(PS + " " + MF);
        switch (MF) {
            case "ABB":
                parsABB(s, 0);
                break;
            case "Экра":
                parsEkra(s, 0);
                break;
            case "Радиус":
                parsRadius(s, 0);
                break;
            case "ДГК":
                parsDGK(s, 0);
                break;
            case "Парма":
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

                    ifNewUnitPutInDB();
                    ifNewDevicePutInDB();

                    oscDate = year + "-" + month + "-" + day;
                    fileName = name;
                    fullPath = nextDirLvl;
                    ifNewFilePutInDB();
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
                        if (isLastDate && (newDate.before(lastDateD))) {
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
                    lvl++;
                    parsParma(nextDirLvl, lvl); //рекурсивный вызов функции для следующего найденного файла/папки
                    if (isDateUnitDevice) {
                        errorNoFiles("NO FILES! " + nextDirLvl);
                        String oscNameTemp = oscName;
                        fileName = "Дата есть а файла нет!";

                        ifNewUnitPutInDB();
                        ifNewDevicePutInDB();

                        oscDate = year + "-" + month + "-" + day;
                        oscName = "Дата есть а файла нет!";
                        ifNewFilePutInDB();

                        isDateUnitDevice = false;
                        oscName = oscNameTemp;
                    }
                    lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
                } else {
                    isNewDate = true;
                    isDateUnitDevice = false;
                }

            }
        }

    }

    void parsRadius(String s, int lvl) throws SQLException, ParseException {

        isNewDate = true;
        File f = new File(s);
        String[] dirList = f.list();

        for (String dirList1 : dirList) {
            String nextDirLvl = s + File.separator + dirList1;
            File f1 = new File(nextDirLvl);
            String name = f1.getName();

            if (f1.isFile()) {

                parsUnitDeviceNameAtRadius(name);

                ifNewUnitPutInDB();
                ifNewDevicePutInDB();

                oscName = name;
                oscDate = year + "-" + month + "-" + day;
                fileName = name;
                fullPath = nextDirLvl;
                ifNewFilePutInDB();
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
                        if (isLastDate && (newDate.before(lastDateD))) {
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
                    lvl++;
                    parsRadius(nextDirLvl, lvl); //рекурсивный вызов функции для следующего найденного файла/папки
                    if (isDateUnitDevice) {
                        errorNoFiles("NO FILES! " + nextDirLvl);
                        unitName = deviceName = fileName = "Дата есть а файла нет!";

                        ifNewUnitPutInDB();
                        ifNewDevicePutInDB();

                        oscDate = year + "-" + month + "-" + day;
                        oscName = "Дата есть а файла нет!";
                        ifNewFilePutInDB();
                        isDateUnitDevice = false;
                    }

                    lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
                } else {
                    isNewDate = true;
                    isDateUnitDevice = false;
                }
            }
        }

    }

    void parsDGK(String s, int lvl) throws SQLException, ParseException {

        isNewDate = true;
        File f = new File(s);
        String[] dirList = f.list();

        for (String dirList1 : dirList) {
            String nextDirLvl = s + File.separator + dirList1;
            File f1 = new File(nextDirLvl);
            String name = f1.getName();

            if (f1.isFile()) {

                unitName = deviceName = name;

                ifNewUnitPutInDB();
                ifNewDevicePutInDB();

                oscDate = year + "-" + month + "-" + "1";
                oscName = name;

                fileName = name;
                fullPath = nextDirLvl;
                ifNewFilePutInDB();
                isDateUnitDevice = false;
            } else {
                switch (lvl) {
                    case 0: {
                        year = name;
                        break;
                    }
                    case 1: {
                        month = name;
                        SimpleDateFormat format = new SimpleDateFormat();
                        format.applyPattern("yyyy-MM-dd");
                        newDate = format.parse(year + "-" + month + "-" + "1");
                        if (isLastDate && (newDate.before(lastDateD))) {
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
                    lvl++;
                    parsDGK(nextDirLvl, lvl); //рекурсивный вызов функции для следующего найденного файла/папки
                    if (isDateUnitDevice) {
                        errorNoFiles("NO FILES! " + nextDirLvl);
                        unitName = deviceName = fileName = "Дата есть а файла нет!";

                        ifNewUnitPutInDB();
                        ifNewDevicePutInDB();
                        oscDate = year + "-" + month + "-" + "1";

                        oscName = name;
                        ifNewFilePutInDB();
                        isDateUnitDevice = false;
                    }
                    lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
                } else {
                    isNewDate = true;
                    isDateUnitDevice = false;
                }
            }
        }
    }

    void parsEkra(String s, int lvl) throws SQLException, ParseException {
        isNewDate = true;
        File f = new File(s);
        String[] dirList = f.list();

        for (String dirList1 : dirList) {

            String nextDirLvl = s + File.separator + dirList1;
            File f1 = new File(nextDirLvl);
            String name = f1.getName();

            if (f1.isFile()) {
                fileName = f1.getName();
                fullPath = nextDirLvl;
                ifNewFilePutInDB();
                isDateUnitDevice = false;
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
                        SimpleDateFormat format = new SimpleDateFormat();
                        format.applyPattern("yyyy-MM-dd");
                        newDate = format.parse(year + "-" + month + "-" + day);
                        if (isLastDate && (newDate.before(lastDateD))) {
                            isNewDate = false;
                        }
                        break;
                    }
                    case 3: {
                        unitName = name;
                        break;
                    }
                    case 4: {
                        deviceName = name;
                        ifNewUnitPutInDB();
                        ifNewDevicePutInDB();
                        break;
                    }
                    case 5: {
                        oscDate = year + "-" + month + "-" + day;
                        oscName = name;
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
                    parsEkra(nextDirLvl, lvl); //рекурсивный вызов функции для следующего найденного файла/папки
                    if (isDateUnitDevice) {
                        errorNoFiles("NO FILES! " + nextDirLvl);
                        fileName = "Дата есть а файла нет!";

                        ifNewFilePutInDB();
                        isDateUnitDevice = false;
                    }
                    lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
                } else {
                    isNewDate = true;
                    isDateUnitDevice = false;
                }
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
            if (f1.isFile()) {
                oscName = fileName = f1.getName();
                fullPath = nextDirLvl;

                oscDate = year + "-" + month + "-" + day;
                ifNewFilePutInDB();
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
                        if (isLastDate && (newDate.before(lastDateD))) {
                            isNewDate = false;
                        }

                        break;

                    }
                    case 3: {
                        parsUnitAndDeviceInABB(name);
                        ifNewUnitPutInDB();
                        ifNewDevicePutInDB();
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
                        oscName = unitName = deviceName = fileName = "Дата есть а файла нет!";
                        ifNewUnitPutInDB();
                        ifNewDevicePutInDB();
                        ifNewFilePutInDB();
                        isDateUnitDevice = false;

                    }
                    lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень

                } else {
                    isNewDate = true;
                    isDateUnitDevice = false;
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
                switch (name.substring(7, 8)) {
                    case "У":
                        unitName = deviceName = name.substring(0, 20);
                        break;
                    case "2":
                        unitName = deviceName = name.substring(0, 15);
                        break;
                    case "Т":
                        unitName = deviceName = name.substring(0, 13);
                        break;
                    case "3":
                        if (name.charAt(10) == 'D') {
                            unitName = deviceName = name.substring(0, 20);
                            break;
                        } else {
                            unitName = deviceName = name.substring(0, 18);
                        }
                        break;
                    case "-":
                        unitName = deviceName = name.substring(0, 10);
                        break;
                    case "L":
                        if (name.charAt(12) == 'D') {
                            unitName = deviceName = name.substring(0, 22);
                            break;
                        } else {
                            unitName = deviceName = name.substring(0, 20);
                        }
                        break;
                    case "V":
                        unitName = deviceName = name.substring(0, 12);
                        break;
                    default:
                        unitName = deviceName = name;
                        break;
                }
                break;
            case "Заягорба":
                unitName = deviceName = name.substring(4, 15);
                break;
            case "Искра":
                unitName = deviceName = name.substring(4, 27);
                break;
            case "Кадуй":
                unitName = deviceName = name.substring(4, 20);
                break;
            case "Устюжна":
                unitName = deviceName = name.substring(0, 20);
                break;
            case "Чагода":
                unitName = deviceName = name.substring(0, 20);
                break;
            case "Шексна":
                if (name.substring(0, 5).equals("imf3r")) {
                    unitName = deviceName = name.substring(0, 10);
                    break;
                } else {
                    unitName = deviceName = name.substring(0, 20);
                }
                break;
            default:
                unitName = deviceName = name;
                break;
        }
    }

    void parsUnitAndDeviceInABB(String s) {

        if (PS.equals("Енюково")) {
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

    void ifNewUnitPutInDB() throws SQLException {

        int r = DB.newUnit(PSId, unitName);
        if (r == 0) {
            unitId = DB.putInTableUnit(PSId, unitName);
        } else {
            unitId = r;
        }
    }

    void ifNewDevicePutInDB() throws SQLException {

        int r = DB.newDevice(MFId, unitId, deviceName);
        if (r == 0) {
            deviceId = DB.putInTableDevice(MFId, unitId, deviceName);
        } else {
            deviceId = r;
        }
    }

    void ifNewFilePutInDB() throws SQLException {

        int i = DB.putInTableOsc_file(oscName, oscDate, fileName, fullPath, deviceId);
        if (i != 0) {
            oscId = i;
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
