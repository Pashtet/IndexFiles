/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexfiles;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Locale;

/**
 *
 * @author pashtet
 */
class UtilPars {//для хранения рекурсивной функции

    int numFile, numur;
    String PS, MF, fullpath, devName, fileName, unitName, year, month, day;
    int PSId, MFId, eventId;
    DBClass DB;

    UtilPars() {
        DB = new DBClass();
        DB.createDB();
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
        switch (s) {
            case "db":
                this.MF = "db";
                break;
            case "Br":
                this.MF = "Бреслер";
                break;
            case "ЭК":
                this.MF = "Экра";
                break;
            case "Па":
                this.MF = "Парма";
                break;
            case "ПА":
                this.MF = "Парма";
                break;
            case "Ра":
                this.MF = "Радиус";
                break;
            case "ДГ":
                this.MF = "ДГК";
                break;
            case "AB":
                this.MF = "ABB";
                break;
            case "SK":
                this.MF = "SKI";
                break;
            case "Ст":
                this.MF = "Старт";
                break;
            case "За":
                this.MF = "Загородная";
                break;
            default:
                System.out.println("Нет совпадений для производителя оборудования:" + s);
                break;
        }
        //System.out.println("Нормальное имя:" + MF);
        return this.MF;
    }

    String parsPS(String ps1) {
        switch (ps1) {
            case "ПС_Енюково":
                return "Енюково";
            case "ПС_Климовская":
                return "Климовская";
            case "ПАРМА_Суда":
                return "Суда";
            case "ПАРМА_Устюжна":
                return "Устюжна";
            case "ПАРМА_Шексна":
                return "Шексна";
            default:
                return "error";
        }
    }

    int list(String szDir) throws SQLException, UnsupportedEncodingException {

        numFile = numur = PSId = MFId = eventId = 0;

        File f = new File(szDir);//файл - объект текущей директории
        String[] sDirList = f.list();//список текущих папок в директории
        numur++;//уровень ПС
        //пошли по ПС
        for (String sDirList1 : sDirList) {
            String szDir1 = (szDir + File.separator + sDirList1);//переменная 1 уровня szDir1
            File f1 = new File(szDir1);// идем по названиям ПС
            System.out.println("Level - " + numur);
            PS = (f1.getName());//ПС_Название
            PSId++;
            DB.PutInTablePS(PSId, PS);
            String[] sDirList2 = f1.list();//список текущих производителей в подстанции
            numur++;

            System.out.println("Level - " + numur);
            for (String sDirList3 : sDirList2) {//идем по названиям производителей
                String szDir2 = szDir1 + File.separator + sDirList3;//переменная второго уровня
                File f2 = new File(szDir2);//идем по производителям
                MF = (this.parsNameMF(f2.getName().substring(0, 2)));// ложим производителя
                MFId++;
                DB.PutInTableMF(MFId, PSId, MF);
                String szDir3 = szDir2 + File.separator + f2.getName();
                System.out.println(PS + "," + MF);
                switch (MF) {
                    case "ABB": {
                        System.out.println(listAbb(szDir2));
                        numur--;
                        break;
                    }
                    case "Парма": {
                        System.out.println(listParma(szDir2));
                        numur--;
                        break;
                    }
                    default:
                        System.out.println("Нет разбора!");
                        break;
                }
            }
            numur--;
        }

        return numFile;
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
                String[] temps = {year, month, day, unitName, devName, fileName, (szDir + File.separator + sDirList1)};
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
                        devName = null;
                        fileName = null;
                        //System.out.println("3");
                        break;
                    }
                    case 4: {
                        month = f1.getName().toLowerCase();
                        day = null;
                        devName = null;
                        fileName = null;
                        //System.out.println("4");
                        break;
                    }
                    case 5: {
                        day = f1.getName().toLowerCase();
                        devName = null;
                        fileName = null;
                        //System.out.println("5");
                        break;
                    }
                    case 6: {
                        devName = f1.getName().toLowerCase();
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
        unitName = devName = null;
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
                String[] temps = {szDir + File.separator + sDirList1, "Parma", PS, year, month, day, unitName, devName, fileName};
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
                                unitName = devName = f1.getName().substring(14);
                                break;
                            case "ПС_Суда":
                                unitName = devName = f1.getName().substring(8);
                                break;
                            case "ПС_Устюжна":
                                unitName = devName = f1.getName().substring(11);
                                break;
                            case "ПС_Шексна":
                                unitName = devName = f1.getName().substring(10);
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
        unitName = devName = null;
        return numFile;
    }

}

class MF {//класс для работы с производителями устройств

    String mfName, psName;
    //int mfId, psId;
}

class PS {

    String psName, mfName;

    PS(String mf) {
        this.mfName = mf;
    }
    //проверка названия подстанции

}

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
        String s = "D:\\NetbeansProjects\\OSC";//начальный путь
        File f = new File(s);//объект типа файл для 
        if (!f.exists()) {
            System.out.println("\nNot found: " + s);
        }

        if (!f.isDirectory()) {
            System.out.println("\nNot directory: " + s);
        }
        UtilPars up = new UtilPars();

        System.out.println(up.list(s));
    }

}
