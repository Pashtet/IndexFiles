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
        //int num = 0;
        //DBClass DB = new DBClass();
        //DB.createDB();
        String s = "D:\\OSC";//начальный путь/home/paveltrfimv/OSCD:\\OSC

        UtilPars up = new UtilPars(s);
        up.parsOSC(s);
        System.out.println(up.numFile);
    }

}

class UtilPars {//для хранения рекурсивной функции

    int numFile, numur, PSId, MFId, tempMFId, eventId, tempUnitId, unitId, tempDeviceId, deviceId, oscId, fileId;
    String PS, MF, fileFullPath, deviceName, fileName, unitName, year, month, day, oscName, oscDate;
    String[] listMF, listPS = null;
    ArrayList<String> namePSAL, nameMFAL, nameEventAL, nameDeviceAL, nameUnitAL, nameDeviceUnitAL;
    DBClass DB;
    String source;
    boolean isPS, isMF;

    public UtilPars(String s) {
        File f = new File(s);//объект типа файл для 
        if (!f.exists()) {
            System.out.println("\nNot found: " + s);
        }

        if (!f.isDirectory()) {
            System.out.println("\nNot directory: " + s);
        }
        DB = new DBClass();
        DB.createDB();
        source = s;
        nameMFAL = new ArrayList<String>();
        nameUnitAL = new ArrayList<String>();
        nameDeviceAL = new ArrayList<String>();
        nameDeviceUnitAL = new ArrayList<String>();
        nameMFAL.add("");
        nameUnitAL.add("");
        nameDeviceAL.add("");
        nameDeviceUnitAL.add("");
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
            case "Радиус_Устюжна":
                this.PS = "Устюжна";
                break;
            case "Радиус_Чагода":
                this.PS = "Чагода";
                break;
            case "Радиус_Шексна":
                this.PS = "Шексна";
                break;
            case "ДГК_Заягорба":
                this.PS = "Заягорба";
                break;
            case "ДГК_Искра":
                this.PS = "Искра";
                break;
            case "ДГК_Чагода":
                this.PS = "Чагода";
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
            default:
                MF = "default";
                System.out.println("Нет совпадений для производителя оборудования:" + s);
                break;
        }
        //System.out.println("Нормальное имя:" + MF);
        return this.MF;
    }

    void parsOSC(String s) throws SQLException, UnsupportedEncodingException {

        File f = new File(s);
        String[] dirList = f.list();
        //System.out.println("Level - " + numur);
        for (String dirList1 : dirList) {
            String dirNextLvl = s + File.separator + dirList1;
            File f1 = new File(dirNextLvl);
            String dirName = f1.getName();

            if (dirName.equals("BattDatabase") || dirName.equals("ОМП")) {
                errorNoMatch(dirName);
                isPS = false;
            } else {
                PS = dirName.substring(3);
                PSId++;
                DB.PutInTablePS(PSId, PS);
                isPS = true;
            }

            parsPS(dirNextLvl);

        }

    }

    void parsPS(String s) throws SQLException, UnsupportedEncodingException {

        File f = new File(s);
        String[] dirList = f.list();
        for (String dirList1 : dirList) {

            String dirNextLvl = s + File.separator + dirList1;
            File f1 = new File(dirNextLvl);
            String dirName = f1.getName();

            if (isPS) {
                parsNameMF(dirName);
                ifNewMFNamePutInDB();
                parsMF(dirNextLvl);
            }

        }

    }

    void parsMF(String s) throws SQLException, UnsupportedEncodingException {

        switch (MF) {
            case "ABB":
                parsABB(s, 0);
        }
    }

    void parsABB(String s, int lvl) throws SQLException, UnsupportedEncodingException {

        File f = new File(s);
        String[] dirList = f.list();

        for (String dirList1 : dirList) {

            String nextDirLvl = s + File.separator + dirList1;
            File f1 = new File(nextDirLvl);
            String name = f1.getName();

            if (f1.isFile()) {
                oscName = fileName = f1.getName();
                //fileFullPath = new String(nextDirLvl.getBytes("WINDOWS-1251"),"UTF-8");
                fileFullPath = nextDirLvl;
                fileId++;
                oscId++;
                oscDate = year + "-" + month + "-" + day;
                
                DB.putInTableFile(fileId, fileName, fileFullPath);
                DB.putInTableOSC(oscId, oscName, oscDate, fileId, tempDeviceId);

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
                        ifNewUnitNamePutInDB();
                        ifNewDeviceNamePutInDB();
                        //ifNewDeviceAndUnitPair();
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
                lvl--;//уменьшаем счетчик уровней когда обработали очередной подуровень
            }

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

    void ifNewMFNamePutInDB() throws SQLException {
        if (!nameMFAL.contains(MF)) {
            MFId++;
            nameMFAL.add(MF);

            tempMFId = MFId;
            DB.PutInTableMF(tempMFId, MF);
        } else {
            tempMFId = nameMFAL.indexOf(MF);
        }
    }

    void ifNewUnitNamePutInDB() throws SQLException {
//        unitId++;
//        DB.putInTableUnit(unitId, PSId, unitName);
        if (!nameUnitAL.contains(unitName)) {
            unitId++;
            nameUnitAL.add(unitName);

            tempUnitId = unitId;
            DB.putInTableUnit(tempUnitId, PSId, unitName);
        } else {
            tempUnitId = nameUnitAL.indexOf(unitName);
        }

    }

    void ifNewDeviceNamePutInDB() throws SQLException {
        
//        deviceId++;
//        DB.putInTableDevice(deviceId, tempMFId, deviceName, unitId);

        if (!nameDeviceAL.contains(deviceName)) {
            deviceId++;
            nameDeviceAL.add(deviceName);
            tempDeviceId = deviceId;
            DB.putInTableDevice(tempDeviceId, tempMFId, deviceName,tempUnitId);
        } else {
            tempDeviceId = nameDeviceAL.indexOf(deviceName);
        }
    }

    void ifNewDeviceAndUnitPair() throws SQLException {
        if (!nameDeviceUnitAL.contains(deviceName + unitName)) {
            nameDeviceUnitAL.add(deviceName + unitName);
            DB.putInTableDeviceUnit(tempUnitId, tempDeviceId);
        }
    }

    void errorNoMatch(String message) {
        System.out.println("Нет разбора для " + message);
    }

    int listAbb(String szDir) throws SQLException, UnsupportedEncodingException {
        //Рекурсивный обход файлов

        numur++;//прибавляем счетчик вложенности при каждом вызове list - след. уровень
        File f = new File(szDir);//файл - объект текущей директории
        String[] sDirList = f.list();//список текущих папок в директории
        for (String sDirList1 : sDirList) {
            //обходим подпапки
            File f1 = new File(szDir + File.separator + sDirList1); //следующая папка в директории
            if (f1.isFile()) {
                //если файл
                //System.out.println(szDir + File.separator + sDirList1); //выводим имя(записываем полный путь)
                fileName = f1.getName();
                //System.out.println(GV.nameFileg);//имя файла
                String ts = szDir + File.separator + sDirList1;
                //String[] temps = {szDir + File.separator + sDirList1, MF, PS, year, month, day, unitName, devName, fileName};
                String[] temps = {year, month, day, unitName, deviceName, fileName, (szDir + File.separator + sDirList1)};
                numFile++;
                DB.PutInTableEvent(temps, numFile, MFId);//ложим в таблицу ABB
                //увеличиваем счетчик файлов

            } else {
                //если не файл то продолжаем обход
                //для начала надо проверить имя папки-> добавить его в переменную - поле таблицы

                //System.out.println("Level - "+GV.numur);//текущий уровень
                switch (numur) {
                    case 3: {
                        year = f1.getName().toLowerCase();
                        month = null;
                        day = null;
                        deviceName = null;
                        fileName = null;
                        //System.out.println("3");
                        break;
                    }
                    case 4: {
                        month = f1.getName().toLowerCase();
                        day = null;
                        deviceName = null;
                        fileName = null;
                        //System.out.println("4");
                        break;
                    }
                    case 5: {
                        day = f1.getName().toLowerCase();
                        deviceName = null;
                        fileName = null;
                        //System.out.println("5");
                        break;
                    }
                    case 6: {
                        deviceName = f1.getName().toLowerCase();
                        fileName = null;
                        //System.out.println("6");
                        break;
                    }
                    default: {
                        System.out.println("Error Level: " + szDir + File.separator + sDirList1);
                        System.out.println(szDir);
                        System.out.println(f1.getName().toLowerCase());
                        break;
                    }
                }
                listAbb(szDir + File.separator + sDirList1); //рекурсивный вызов функции для следующего найденного файла/папки
                numur--;//уменьшаем счетчик уровней когда обработали очередной подуровень
            }
        }
        unitName = deviceName = null;
        return numFile;
    }

    int listParma(String szDir) throws SQLException, UnsupportedEncodingException {
        //Рекурсивный обход файлов
        //при вызове попадаем в каталог ParmaOSC
        numur++;//прибавляем счетчик вложенности при каждом вызове list - след. уровень
        File f = new File(szDir);//файл - объект текущей директории
        String[] sDirList = f.list();//список текущих папок в директории
        for (String sDirList1 : sDirList) {
            //обходим подпапки
            //уровень 1 - Парма_суда, Устюжна, Шексна
            //уровень 2 - ПС_Суда_Самописец, ПС_Суда_Аварии
            File f1 = new File(szDir + File.separator + sDirList1); //следующая папка в директории
            if (f1.isFile()) {
                //если файл
                //System.out.println(szDir + File.separator + sDirList1); //выводим имя(записываем полный путь)
                fileName = f1.getName().toLowerCase();
                //System.out.println(GV.nameFileg);//имя файла
                String ts = szDir + File.separator + sDirList1;
                String[] temps = {szDir + File.separator + sDirList1, "Parma", PS, year, month, day, unitName, deviceName, fileName};
                //DBParma.PutInTableParma(temps, numFile);//ложим в таблицу ABB
                numFile++;//увеличиваем счетчик файлов
            } else {
                //если не файл то продолжаем обход
                //для начала надо проверить имя папки-> добавить его в переменную - поле таблицы
                //System.out.println("Level - "+GV.numur);//текущий уровень
                switch (numur) {
                    case 3: {
                        switch (PS) {
                            case "ПС_Покровское":
                                unitName = deviceName = f1.getName().substring(14);
                                break;
                            case "ПС_Суда":
                                unitName = deviceName = f1.getName().substring(8);
                                break;
                            case "ПС_Устюжна":
                                unitName = deviceName = f1.getName().substring(11);
                                break;
                            case "ПС_Шексна":
                                unitName = deviceName = f1.getName().substring(10);
                                break;
                            default:
                                System.out.println("Не прошла проверку Парма - авария/самописец");
                                break;
                        }
                        year = null;
                        month = null;
                        day = null;
                        //nameProt = null;
                        //nameDev = null;
                        fileName = null;
                        //  System.out.println("3");
                        break;
                    }
                    case 4: {
                        year = f1.getName().toLowerCase();
                        month = null;
                        day = null;
                        //nameProt = null;
                        //nameDev = null;
                        fileName = null;
                        //   System.out.println("4");
                        break;
                    }
                    case 5: {
                        month = f1.getName().toLowerCase();
                        day = null;
                        //nameProt = null;
                        //nameDev = null;
                        fileName = null;
                        // System.out.println("5");
                        break;
                    }
                    case 6: {
                        day = f1.getName().toLowerCase();
                        //nameProt = null;
                        //nameDev = null;
                        fileName = null;
                        // System.out.println("5");
                        break;
                    }
                    default: {
                        System.out.println("Error Level");
                        System.out.println(f1.getName().toLowerCase());
                        break;
                    }
                }
                listParma(szDir + File.separator + sDirList1); //рекурсивный вызов функции для следующего найденного файла/папки
                numur--;//уменьшаем счетчик уровней когда обработали очередной подуровень
            }
        }
        year = null;
        unitName = deviceName = null;
        return numFile;
    }

}


