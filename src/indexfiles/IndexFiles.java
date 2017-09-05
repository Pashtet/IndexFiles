/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexfiles;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
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
    public static void main(String[] args) throws SQLException, UnsupportedEncodingException {
        // TODO code application logic here

        String s = "D:\\OSC";//начальный путь/home/paveltrfimv/OSCD:\OSC
        // /disk1/OSC

        Pars p = new Pars(s);
        p.parsOSCAll(s);
        System.out.println(p.numFile);
    }

}

class Pars {//для хранения рекурсивной функции

    int numFile, numur, PSId, tempPSId, MFId, tempMFId, tempUnitId, unitId, tempDeviceId, deviceId, oscId, fileId;
    String PS, MF, fileFullPath, deviceName, fileName, unitName, year, month, day, oscName, oscDate;

    DBClass DB;
    String source;
    boolean isPS, isMF, isDateUnitDevice, isLastFile;

    public Pars(String s) {
        File f = new File(s);//объект типа файл для 
        if (!f.exists()) {
            System.out.println("\nNot found: " + s);
        }

        if (!f.isDirectory()) {
            System.out.println("\nNot directory: " + s);
        }
        DB = new DBClass();
        DB.createNewDBAll();
        source = s;
    }

    // разбор ПС до нормального имени
    void parsOSCAll(String s) throws SQLException, UnsupportedEncodingException {
        ////уровень самый верхний ПС
        File f = new File(s);
        String[] dirList = f.list();
        //System.out.println("Level - " + numur);
        for (String dirList1 : dirList) {
            String dirNextLvl = s + File.separator + dirList1;
            File f1 = new File(dirNextLvl);
            String dirName = f1.getName();
            if (!f1.isFile()) {
                switch (dirName.substring(0, 3)) {
                    case "Bat":
                        errorNoMatch(dirName);
                        break;
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
                oscId++;
                DB.putInTableOSC(oscId, oscName, oscDate, tempDeviceId);

                fileName = name;
                fileFullPath = nextDirLvl;
                fileId++;
                DB.putInTableFile(fileId, oscId, fileName, fileFullPath);
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
                    unitName = deviceName = oscName = fileName = "Дата есть а файла нет!";
                    //проверка новое ли имя?

                    ifNewPairUnitDevice(unitName, deviceName);
                    fileFullPath = nextDirLvl;
                    fileId++;
                    oscId++;
                    oscDate = year + "-" + month + "-" + "1";
                    DB.putInTableOSC(oscId, oscName, oscDate, tempDeviceId);
                    DB.putInTableFile(fileId, oscId, fileName, fileFullPath);
                    isDateUnitDevice = false;
                }
                lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
            }
        }
    }

    void parsPS(String s) throws SQLException, UnsupportedEncodingException {

        File f = new File(s);
        String[] dirList = f.list();
        for (String dirList1 : dirList) {

            String dirNextLvl = s + File.separator + dirList1;
            File f1 = new File(dirNextLvl);
            String dirName = f1.getName();

            parsNameMF(dirName);
            ifNewMFNamePutInDB();
            //if (PS.equals("Покровское")) {
            parsMF(dirNextLvl);

        }

    }

    void parsMF(String s) throws SQLException, UnsupportedEncodingException {

        switch (MF) {
            case "ABB":
                System.out.println(PS + " " + MF);
                parsABB(s, 0);
                break;
            case "Экра":
                System.out.println(PS + " " + MF);
                parsEkra(s, 0);
                break;
            case "Радиус":
                System.out.println(PS + " " + MF);
                parsRadius(s, 0);
                break;
            case "ДГК":
                System.out.println(PS + " " + MF);
                parsDGK(s, 0);
                break;
            case "Парма":
                System.out.println(PS + " " + MF);
                parsParma(s, 0);
                break;
            default:
                errorNoMatch(MF);
                break;
        }
    }

    void parsParma(String s, int lvl) throws SQLException {
        File f = new File(s);
        String[] dirList = f.list();

        for (String dirList1 : dirList) {
            String nextDirLvl = s + File.separator + dirList1;
            File f1 = new File(nextDirLvl);
            String name = f1.getName();
            if (f1.isFile() && (lvl < 4)) {
                System.out.println("Error lvl file " + nextDirLvl);
                isDateUnitDevice = false;
            } else if (f1.isFile()) {
                oscName = fileName = name;
                oscDate = year + "-" + month + "-" + day;
                oscId++;
                DB.putInTableOSC(oscId, oscName, oscDate, tempDeviceId);

                fileFullPath = nextDirLvl;
                fileId++;
                DB.putInTableFile(fileId, oscId, fileName, fileFullPath);
                isDateUnitDevice = false;
            } else {
                switch (lvl) {
                    case 0: {
                        int length = PS.length();
                        deviceName = unitName = name.substring(length + 4);
                        ifNewPairUnitDevice(unitName, deviceName);
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
                parsParma(nextDirLvl, lvl); //рекурсивный вызов функции для следующего найденного файла/папки
                if (isDateUnitDevice) {
                    errorNoFiles("NO FILES! " + nextDirLvl);
                    String unitDeviceNameTemp = unitName;
                    unitName = deviceName = oscName = fileName = "Дата есть а файла нет!";

                    ifNewPairUnitDevice(unitName, deviceName);

                    fileId++;
                    oscId++;
                    oscDate = year + "-" + month + "-" + day;
                    fileFullPath = nextDirLvl;
                    DB.putInTableOSC(oscId, oscName, oscDate, tempDeviceId);
                    DB.putInTableFile(fileId, oscId, fileName, fileFullPath);
                    isDateUnitDevice = false;
                    deviceName = unitName = unitDeviceNameTemp;
                    ifNewPairUnitDevice(unitName, deviceName);
                }
                lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
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
                oscId++;
                DB.putInTableOSC(oscId, oscName, oscDate, tempDeviceId);

                fileName = name;
                fileFullPath = nextDirLvl;
                fileId++;
                DB.putInTableFile(fileId, oscId, fileName, fileFullPath);
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
                    unitName = deviceName = oscName = fileName = "Дата есть а файла нет!";

                    ifNewPairUnitDevice(unitName, deviceName);
                    fileFullPath = nextDirLvl;
                    fileId++;
                    oscId++;
                    oscDate = year + "-" + month + "-" + day;
                    DB.putInTableOSC(oscId, oscName, oscDate, tempDeviceId);
                    DB.putInTableFile(fileId, oscId, fileName, fileFullPath);
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
                oscId++;
                DB.putInTableOSC(oscId, oscName, oscDate, tempDeviceId);

                fileName = name;
                fileFullPath = nextDirLvl;
                fileId++;
                DB.putInTableFile(fileId, oscId, fileName, fileFullPath);
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
                    unitName = deviceName = oscName = fileName = "Дата есть а файла нет!";

                    ifNewPairUnitDevice(unitName, deviceName);
                    fileFullPath = nextDirLvl;
                    fileId++;
                    oscId++;
                    oscDate = year + "-" + month + "-" + "1";
                    DB.putInTableOSC(oscId, oscName, oscDate, tempDeviceId);
                    DB.putInTableFile(fileId, oscId, fileName, fileFullPath);
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
                fileId++;
                DB.putInTableFile(fileId, oscId, fileName, fileFullPath);
                isDateUnitDevice = false;
            } else if (!name.equals("Com_Trade_Bab") && !name.equals("ComTrade")) {
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
                        oscId++;
                        DB.putInTableOSC(oscId, oscName, oscDate, tempDeviceId);
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
                    oscName = fileName = "Дата есть а файла нет!";
                    fileFullPath = nextDirLvl;
                    fileId++;
                    oscId++;
                    oscDate = year + "-" + month + "-" + day;
                    DB.putInTableOSC(oscId, oscName, oscDate, tempDeviceId);
                    DB.putInTableFile(fileId, oscId, fileName, fileFullPath);
                    isDateUnitDevice = false;
                }
                lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
            }
        }
    }

    void parsABB(String s, int lvl) throws SQLException {

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
                fileId++;
                oscId++;
                oscDate = year + "-" + month + "-" + day;

                DB.putInTableOSC(oscId, oscName, oscDate, tempDeviceId);
                DB.putInTableFile(fileId, oscId, fileName, fileFullPath);
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

                lvl++;
                parsABB(nextDirLvl, lvl); //рекурсивный вызов функции для следующего найденного файла/папки
                if (isDateUnitDevice) {
                    errorNoFiles("NO FILES! " + nextDirLvl);
                    oscName = fileName = "Дата есть а файла нет!";
                    fileFullPath = nextDirLvl;
                    fileId++;
                    oscId++;
                    oscDate = year + "-" + month + "-" + day;
                    DB.putInTableOSC(oscId, oscName, oscDate, tempDeviceId);
                    DB.putInTableFile(fileId, oscId, fileName, fileFullPath);

                    isDateUnitDevice = false;
                }
                lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
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
                switch (name.charAt(0)) {
                    case 'A':
                        switch (name.charAt(13)) {
                            case 'D':
                                unitName = deviceName = name.substring(0, 28);
                                break;
                            case 'V':
                                unitName = deviceName = name.substring(0, 30);
                                break;
                            case 'o':
                                unitName = deviceName = name.substring(0, 38);
                                break;
                            case 'a':
                                unitName = deviceName = name.substring(0, 34);
                                break;
                            default:
                                unitName = deviceName = name;
                                break;

                        }
                        break;
                    case 'O':
                        if (name.charAt(4) == 'S') {
                            if (name.charAt(12) == '(') {
                                unitName = deviceName = name.substring(0, 26);
                            } else {
                                unitName = deviceName = name.substring(0, 28);
                            }
                        } else if (name.charAt(10) == 'D') {
                            if (name.charAt(20) == '(') {
                                unitName = deviceName = name.substring(0, 33);
                            } else {
                                unitName = deviceName = name.substring(0, 36);
                            }
                        } else if (name.charAt(16) == '(') {
                            unitName = deviceName = name.substring(0, 30);
                        } else {
                            unitName = deviceName = name.substring(0, 32);
                        }
                        break;
                    case 'С':
                        if (name.charAt(7) == 'У') {
                            unitName = deviceName = name.substring(0, 20);
                        } else if (name.charAt(7) == 'Т') {
                            unitName = deviceName = name.substring(0, 13);
                        } else {
                            unitName = deviceName = name.substring(0, 15);
                        }
                        break;
                    default:
                        unitName = deviceName = name;
                        break;
                }
                break;

            case "Заягорба":
                switch (name.substring(0, 20)) {
                    case "Osc_2SVV-10kV(301)(s":
                        unitName = deviceName = name.substring(0, 32);
                        break;
                    case "Osc_AUVT1-Z(serN211)":
                        unitName = deviceName = name.substring(0, 20);
                        break;
                    case "Osc_AUVT2(serN212)_2":
                        unitName = deviceName = name.substring(0, 18);
                        break;
                    case "Osc_Ivachevo-Z(113)(":
                        unitName = deviceName = name.substring(0, 29);
                        break;
                    case "Osc_OZT1-Z(serN360)_":
                        unitName = deviceName = name.substring(0, 19);
                        break;
                    case "Osc_OZT2-Z(serN359)_":
                        unitName = deviceName = name.substring(0, 19);
                        break;
                    case "Osc_Selstroy-1(104)(":
                        unitName = deviceName = name.substring(0, 29);
                        break;
                    case "Osc_Snabsbyt-2(412)(":
                        unitName = deviceName = name.substring(0, 29);
                        break;
                    case "Osc_SVV-1(101)(serN8":
                        unitName = deviceName = name.substring(0, 23);
                        break;
                    case "Osc_Teplovaya-3(403)":
                        unitName = deviceName = name.substring(0, 30);
                        break;
                    case "Osc_Trikotazh-2(414)":
                        unitName = deviceName = name.substring(0, 30);
                        break;
                    case "Osc_Vvod-110kV(109)(":
                        unitName = deviceName = name.substring(0, 29);
                        break;
                    case "Osc_Vvod-210kV(204)(":
                        unitName = deviceName = name.substring(0, 29);
                        break;
                    case "Osc_Vvod-310-kV(305)":
                        unitName = deviceName = name.substring(0, 30);
                        break;
                    case "Osc_Vvod-410kV(408)(":
                        unitName = deviceName = name.substring(0, 29);
                        break;
                    case "Osc_Zhilr-on-1(103)(":
                        unitName = deviceName = name.substring(0, 29);
                        break;
                    case "Osc_Zhilr-on-10(306)":
                        unitName = deviceName = name.substring(0, 30);
                        break;
                    default:
                        unitName = deviceName = name;
                        break;
                }
                break;

            case "Искра":
                switch (name.substring(0, 7)) {
                    case "Osc_DGK":
                        if (name.charAt(8) == '1') {
                            unitName = deviceName = name.substring(0, 30);
                        } else {
                            unitName = deviceName = name.substring(0, 28);
                        }
                        break;
                    case "Osc_FMK":
                        if (name.charAt(8) == '1') {
                            unitName = deviceName = name.substring(0, 29);
                        } else {
                            unitName = deviceName = name.substring(0, 28);
                        }
                        break;
                    case "Osc_Gaz":
                        unitName = deviceName = name.substring(0, 33);
                        break;
                    case "Osc_IZh":
                        unitName = deviceName = name.substring(0, 31);
                        break;
                    case "Osc_Kot":
                        unitName = deviceName = name.substring(0, 34);
                        break;
                    case "Osc_Mol":
                        unitName = deviceName = name.substring(0, 36);
                        break;
                    case "Osc_Nas":
                        unitName = deviceName = name.substring(0, 32);
                        break;
                    case "Osc_Oro":
                        unitName = deviceName = name.substring(0, 35);
                        break;
                    case "Osc_Rez":
                        unitName = deviceName = name.substring(0, 31);
                        break;
                    case "Osc_Sad":
                        unitName = deviceName = name.substring(0, 31);
                        break;
                    case "Osc_Sev":
                        if (name.charAt(13) == '5') {
                            unitName = deviceName = name.substring(0, 35);
                        } else {
                            unitName = deviceName = name.substring(0, 33);
                        }
                        break;
                    case "Osc_Skl":
                        unitName = deviceName = name.substring(0, 29);
                        break;
                    case "Osc_Spi":
                        if (name.charAt(12) == '2') {
                            unitName = deviceName = name.substring(0, 32);
                        } else {
                            unitName = deviceName = name.substring(0, 38);
                        }
                        break;
                    case "Osc_SVV":
                        if (name.charAt(9) == '_') {
                            unitName = deviceName = name.substring(0, 32);
                        } else if (name.charAt(9) == 'd') {
                            unitName = deviceName = name.substring(0, 31);
                        } else {
                            unitName = deviceName = name.substring(0, 28);
                        }
                        break;
                    case "Osc_T1_":
                        unitName = deviceName = name.substring(0, 26);
                        break;
                    case "Osc_T2_":
                        unitName = deviceName = name.substring(0, 26);
                        break;
                    case "Osc_TN-":
                        unitName = deviceName = name.substring(0, 33);
                        break;
                    case "Osc_TSN":
                        if (name.charAt(8) == '1') {
                            unitName = deviceName = name.substring(0, 29);
                        } else {
                            unitName = deviceName = name.substring(0, 28);
                        }
                        break;
                    case "Osc_Vvo":
                        if (name.charAt(8) == '_') {
                            unitName = deviceName = name.substring(0, 36);
                        } else {
                            unitName = deviceName = name.substring(0, 33);
                        }
                        break;
                    default:
                        unitName = deviceName = name;
                        break;
                }
                break;

            case "Кадуй":
                switch (name.substring(0, 5)) {
                    case "Osc_S":
                        unitName = deviceName = name.substring(0, 26);
                        break;
                    case "Osc_T":
                        if (name.charAt(7) == 'A') {
                            unitName = deviceName = name.substring(0, 21);
                        } else if (name.charAt(7) == 'D') {
                            unitName = deviceName = name.substring(0, 20);
                        } else {
                            unitName = deviceName = name.substring(0, 19);
                        }
                        break;
                    case "Osc_V": {
                        if (name.charAt(5) == 'v') {
                            if (name.substring(0, 20).equals("Osc_Vvod_35_T2_K(ser")) {
                                unitName = deviceName = name.substring(0, 26);
                            } else if (name.substring(0, 20).equals("Osc_Vvod_10_T2_K(ser")) {
                                unitName = deviceName = name.substring(0, 30);
                            } else {
                                unitName = deviceName = name.substring(0, 25);
                            }
                        } else {
                            switch (name.substring(0, 12)) {
                                case "Osc_VL_10_Do":
                                    if (name.charAt(14) == '1') {
                                        unitName = deviceName = name.substring(0, 27);
                                    } else {
                                        unitName = deviceName = name.substring(0, 31);
                                    }
                                    break;
                                case "Osc_VL_10_Go":
                                    unitName = deviceName = name.substring(0, 28);
                                    break;
                                case "Osc_VL_10_Ru":
                                    unitName = deviceName = name.substring(0, 33);
                                    break;
                                case "Osc_VL_10_Se":
                                    unitName = deviceName = name.substring(0, 34);
                                    break;
                                case "Osc_VL_10_Su":
                                    unitName = deviceName = name.substring(0, 33);
                                    break;
                                case "Osc_VL_10_Ve":
                                    unitName = deviceName = name.substring(0, 34);
                                    break;
                                case "Osc_VL_10_Vi":
                                    unitName = deviceName = name.substring(0, 30);
                                    break;
                                case "Osc_VL_35_Ni":
                                    unitName = deviceName = name.substring(0, 31);
                                    break;
                                default:
                                    unitName = deviceName = name;
                                    break;

                            }
                        }
                        break;
                    }
                    default:
                        unitName = deviceName = name;
                        break;
                }
                break;

            case "Устюжна":
                switch (name.charAt(7)) {
                    case '_'://АУВ, ДЗТ
                        unitName = deviceName = name.substring(0, 23);
                        break;
                    case 'd':// ввода
                        unitName = deviceName = name.substring(0, 34);
                        break;
                    default:
                        unitName = deviceName = name;
                        break;
                }
                break;
            case "Чагода":
                switch (name.substring(0, 8)) {
                    case "Osc_DZT_":
                        unitName = deviceName = name.substring(0, 26);
                        break;
                    case "Osc_IMF_":
                        unitName = deviceName = name.substring(0, 33);
                        break;
                    case "Osc_OMP_":
                        unitName = deviceName = name.substring(0, 32);
                        break;
                    case "Osc_SV_1":
                        unitName = deviceName = name.substring(0, 20);
                        break;
                    case "Osc_Vvod":
                        if (name.charAt(10) == '0') {
                            unitName = deviceName = name.substring(0, 32);
                        } else {
                            unitName = deviceName = name.substring(0, 33);
                        }
                        break;
                    case "Сириус-2":
                        unitName = deviceName = name.substring(0, 15);
                        break;
                    default:
                        unitName = deviceName = name;
                        break;
                }
                break;

            case "Шексна":
                if (name.charAt(0) == 'i') {
                    unitName = deviceName = name.substring(0, 10);
                } else {
                    switch (name.substring(0, 7)) {
                        case "Alarm_A":
                            unitName = deviceName = name.substring(0, 23);
                            break;
                        case "Alarm_I":
                            if (name.charAt(11) == 'a') {//Gazovaya
                                unitName = deviceName = name.substring(0, 31);
                            } else if (name.charAt(11) == 'h') {//Sheksna
                                unitName = deviceName = name.substring(0, 32);
                            } else {
                                unitName = deviceName = name.substring(0, 30);
                            }
                            break;
                        case "Alarm_V":
                            if (name.charAt(13) == 'a') {//Gazovaya
                                unitName = deviceName = name.substring(0, 37);
                            } else {
                                unitName = deviceName = name.substring(0, 36);
                            }
                            break;
                        case "Osc_IMF":
                            if (name.charAt(9) == 'a') {//Gazovaya
                                unitName = deviceName = name.substring(0, 29);
                            } else if (name.charAt(9) == 'h') {//Sheksna
                                unitName = deviceName = name.substring(0, 30);
                            } else {
                                unitName = deviceName = name.substring(0, 28);
                            }
                            break;
                        case "Osc_VL-":
                            if (name.charAt(11) == 'a') {//Gazovaya
                                unitName = deviceName = name.substring(0, 35);
                            } else {
                                unitName = deviceName = name.substring(0, 34);
                            }
                            break;
                        default:
                            unitName = deviceName = name;
                            break;
                    }
                }
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

    String parsNamePS(String s) {

        switch (s) {
            case "ПС_Бабаево":
                this.PS = "Бабаево";
                break;
            case "ПС_Енюково":
                this.PS = "Енюково";
                break;
            case "ПС_Загородная":
                this.PS = "Загородная";
                break;
            case "ПС_Климовская":
                this.PS = "Климовская";
                break;
            case "ПС_Октябрьская":
                this.PS = "Октябрьская";
                break;
            case "ПС_Петринево":
                this.PS = "Петринево";
                break;
            case "ПС_Устюжна":
                this.PS = "Устюжна";
                break;
            case "ПС_Чагода":
                this.PS = "Чагода";
                break;
            case "ПС_Покровское":
                this.PS = "Покровское";
                break;
            case "ПС_Суда":
                this.PS = "Суда";
                break;
            case "ПС_Шексна":
                this.PS = "Шексна";
                break;
            case "ПС_Домозерово":
                this.PS = "Домозерово";
                break;
            case "ПС_Кадуй":
                this.PS = "Кадуй";
                break;
            default:
                System.out.println("!Подстанция не распознана!");
                break;
        }
        System.out.println("Нормальное название ПС: " + this.PS);
        return (this.PS);

    }

    String parsNameMF(String s) {
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
        //System.out.println("Нормальное имя:" + MF);
        return this.MF;
    }

    void ifNewMFNamePutInDB() throws SQLException {
        if (DB.newMF(MF)) {
            tempMFId = ++MFId;
            DB.PutInTableMF(tempMFId, MF);
        } else {
            tempMFId = DB.getMFId(MF);
        }
    }

    void ifNewPSNamePutInDB(String s) throws SQLException {
        if (DB.newPS(s)) {
            tempPSId = ++PSId;
            DB.PutInTablePS(tempPSId, s);
        } else {
            tempPSId = DB.getPSId(s);
        }
        PS = s;
    }

    void ifNewPairUnitDevice(String u, String d) throws SQLException {
        if (DB.newPairUnitDeviceOnPS(u, d, PS)) {
            tempUnitId = ++unitId;
            tempDeviceId = ++deviceId;
            DB.putInTableUnit(tempUnitId, tempPSId, u);
            DB.putInTableDevice(tempDeviceId, tempMFId, d, tempUnitId);
        } else {
            int[] ab = new int[2];
            ab = DB.getUnitAndDeviceId(u, d);
            tempUnitId = ab[0];
            tempDeviceId = ab[1];
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
