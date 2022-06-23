package com.sam.generatejcl.service;

import com.sam.generatejcl.model.JclModel;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import org.apache.commons.collections4.ListUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import static java.sql.DriverManager.getConnection;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;

public class JclFile {

    public static String INIT_JCL = "//JCLNEW1 JOB 'JCL TEST',CLASS=A,MSGCLASS=A\n"
            + "//**************************************************************\n" +
            "//*";

//    public static String FIND_ALL_SQL = "SELECT DISTINCT ACTUALFILE,LRECL\n" +
//            "FROM FEXR\n" +
//            "WHERE ACTUALFILE IS NOT NULL AND OPENMODE <> 'OUTPUT'  AND RECFM ='FB'";


    /**
     * 產生查詢的SQL
     *
     * @param fileNameArray
     * @return SQL
     * @author Sam Chen
     */
    public String getSelectSql(String[] fileNameArray) {
        StringBuilder sb = new StringBuilder("SELECT DISTINCT ACTUALFILE,LRECL FROM FEXR WHERE ( ACTUALFILE ='");
        for (int i = 0; i < fileNameArray.length; i++) {
            if (i == 0) {
                sb.append(fileNameArray[i]).append("' ");
            } else if (i < fileNameArray.length - 1) {
                sb.append("OR ACTUALFILE ='").append(fileNameArray[i]).append("'");
            } else {
                sb.append("OR ACTUALFILE ='").append(fileNameArray[i]).append("') AND OPENMODE <> 'OUTPUT'  AND RECFM ='FB'");
            }
        }
        return sb.toString();
    }

    /**
     * 產生DB連線
     *
     * @param driver
     * @param url
     * @param username
     * @param password
     * @return DB連線
     * @author Sam Chen
     */
    public Connection getConnection(String driver,
                                    String url,
                                    String username,
                                    String password) {

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return connection;
    }

    /**
     * 產生list
     *
     * @param con
     * @param fileNameArray
     * @return jclModelList
     * @author Sam Chen
     */
    public List<JclModel> getJclModelList(Connection con, String[] fileNameArray) {
        List<JclModel> jclModelList = new ArrayList<>();

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
//            con = dataSource.getConnection();
            String sql = getSelectSql(fileNameArray);
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                JclModel jclModel = new JclModel();
                String actualFile = rs.getString("ACTUALFILE");
                String lrecl = rs.getString("LRECL");
                jclModel.setActualfile(actualFile);
                jclModel.setLrecl(lrecl);
                jclModelList.add(jclModel);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return jclModelList;
    }

    /**
     * 分段產生JCL的內容
     *
     * @param index
     * @param jclModel
     * @return
     */
    public String generateParagraph(int index,
                                    JclModel jclModel) {

        String step;
        if (index < 10) {
            step = "00" + String.valueOf(index + 1);
        } else if (10 <= index && index < 100) {
            step = "0" + String.valueOf(index + 1);
        } else {
            step = String.valueOf(index + 1);
        }
        String actualFile = jclModel.getActualfile();
        String lrecl = jclModel.getLrecl();
        String recfm = jclModel.getRecfm();
        String paragraph = "//STEP" + step + "  EXEC PGM=IEFBR14\n" +
                "//SYSPRINT DD SYSOUT=*\n" +
                "//SYSOUT   DD SYSOUT=*\n" +
                "//SYSDUMP  DD SYSOUT=*\n" +
                "//DD01     DD DSN=" + actualFile + ",\n" +
                "//            DISP=(NEW,CATLG,DELETE),VOLUME=SER=DEVL,\n" +
                "//            SPACE=(TRK,(1,1),RLSE),UNIT=SYSDA,\n" +
                "//            DCB=(DSORG=PS,RECFM=" + recfm + ",LRECL=" + lrecl + ",BLKSIZE=0)\n" +
                "//***************************************************\n" +
                "//*";

        return paragraph;
    }

    public String generatePoParagraph(int index,
                                      JclModel jclModel) {

        String step;
        if (index < 10) {
            step = "00" + String.valueOf(index + 1);
        } else if (10 <= index && index < 100) {
            step = "0" + String.valueOf(index + 1);
        } else {
            step = String.valueOf(index + 1);
        }
        String actualFile = jclModel.getActualfile();
        String lrecl = jclModel.getLrecl();
        String recfm = jclModel.getRecfm();
        String paragraph = "//STEP" + step + "  EXEC PGM=IEFBR14\n" +
                "//SYSPRINT DD SYSOUT=*\n" +
                "//SYSOUT   DD SYSOUT=*\n" +
                "//SYSDUMP  DD SYSOUT=*\n" +
                "//DD01     DD DSN=" + actualFile + ",\n" +
                "//            DISP=(NEW,CATLG,DELETE),VOLUME=SER=DEVL,\n" +
                "//            SPACE=(TRK,(1,1),RLSE),UNIT=SYSDA,\n" +
                "//            DCB=(DSORG=PO,RECFM="+recfm+",LRECL=" + lrecl + ",BLKSIZE=0)\n" +
                "//*MFE: %PCDSN=/CCBS/CCBSES/CATALOG/DATA/" + actualFile + "/*.DAT\n" +
                "//***************************************************\n" +
                "//*";

        return paragraph;
    }


    public String generateVsamParagraph(int index,
                                        JclModel jclModel) {

        String step;
        if (index < 10) {
            step = "00" + String.valueOf(index + 1);
        } else if (10 <= index && index < 100) {
            step = "0" + String.valueOf(index + 1);
        } else {
            step = String.valueOf(index + 1);
        }
        String actualFile = jclModel.getActualfile();
        String lrecl = jclModel.getLrecl();
        String recfm = jclModel.getRecfm();
        String paragraph = "//STEP" + step + "  EXEC PGM=IEFBR14\n" +
                "//SYSPRINT DD SYSOUT=*\n" +
                "//SYSOUT   DD SYSOUT=*\n" +
                "//SYSDUMP  DD SYSOUT=*\n" +
                "//DD01     DD DSN=" + actualFile + ",\n" +
                "//            DISP=(NEW,CATLG,DELETE),VOLUME=SER=DEVL,\n" +
                "//            SPACE=(TRK,(1,1),RLSE),UNIT=SYSDA,\n" +
                "//            DCB=(DSORG=VSAM,RECFM=,LRECL=" + lrecl + ",BLKSIZE=0)\n" +
                "//***************************************************\n" +
                "//*";

        return paragraph;
    }


    public String generateDeleteParagraph(int index,
                                          JclModel jclModel) {

        String step;
        if (index < 10) {
            step = "00" + String.valueOf(index + 1);
        } else if (10 <= index && index < 100) {
            step = "0" + String.valueOf(index + 1);
        } else {
            step = String.valueOf(index + 1);
        }
        String actualFile = jclModel.getActualfile();
        String lrecl = jclModel.getLrecl();
        String recfm = jclModel.getRecfm();
        String paragraph = "//STEP" + step + "  EXEC PGM=IEFBR14\n" +
                "//SYSPRINT DD SYSOUT=*\n" +
                "//SYSOUT   DD SYSOUT=*\n" +
                "//SYSDUMP  DD SYSOUT=*\n" +
                "//DD01     DD DSN=" + actualFile + ",\n" +
                "//            DISP=(OLD,DELETE,DELETE),VOLUME=SER=DEVL,\n" +
                "//            SPACE=(TRK,(1,1),RLSE),UNIT=SYSDA,\n" +
                "//            DCB=(DSORG=PS,RECFM=VB,LRECL=00000,BLKSIZE=0)\n" +
                "//***************************************************\n" +
                "//*";

        return paragraph;
    }

    /**
     * 產出JCL
     *
     * @author Sam Chen
     */
    public void generateJcl() {

        Properties properties = new Properties();
        String url = null;
        String driver = null;
        String username = null;
        String password = null;
        String outputPath = null;
        String[] actualNames = null;
        try (FileInputStream fileInputStream = new FileInputStream("config.properties");) {
            properties.load(fileInputStream);
            url = properties.getProperty("url");
            driver = properties.getProperty("driver");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            outputPath = properties.getProperty("outputPath");
            actualNames = properties.getProperty("confirmFile").split(",");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Connection connection = getConnection(driver, url, username, password);
        List<JclModel> jclModelList = getJclModelList(connection, actualNames);
        try (FileWriter fileWriter = new FileWriter(outputPath);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(INIT_JCL);
            bufferedWriter.newLine();

            for (int i = 0; i < jclModelList.size(); i++) {
                bufferedWriter.write(generateParagraph(i, jclModelList.get(i)));
                bufferedWriter.newLine();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void generateJclFromXlsx() {

        List<JclModel> jclModelList = new ArrayList<>();


        String config = "config.properties";
        Properties props = new Properties();
        try {
            props.load(new InputStreamReader(new FileInputStream(config), "UTF-8"));

            String mappingExcel = props.getProperty("excelFile");
            String outputPath = props.getProperty("outputPath");
            FileInputStream inp = new FileInputStream(mappingExcel);
            XSSFWorkbook wb = new XSSFWorkbook(inp);
            inp.close();

            XSSFSheet sheet = wb.getSheetAt(0);
            int rowLength = sheet.getLastRowNum();

            XSSFRow rowHeader = sheet.getRow(0);


            String header0 = rowHeader.getCell(0).getStringCellValue();
            String header1 = rowHeader.getCell(1).getStringCellValue();
            String header2 = rowHeader.getCell(2).getStringCellValue();
            String header3 = rowHeader.getCell(3).getStringCellValue();

            String fileName = null;
            String lrecl = null;
            String recfm = null;
            String dsorg = null;

            for (int i = 1; i <= rowLength; i++) {


                JclModel jclModel = new JclModel();

                XSSFRow row = sheet.getRow(i);

                XSSFCell cell0 = row.getCell(0);
                XSSFCell cell1 = row.getCell(1);
                XSSFCell cell2 = row.getCell(2);
                XSSFCell cell3 = row.getCell(3);

                switch (header0.toLowerCase()) {
                    case "dsname":
                        fileName = cell0.getStringCellValue();
                        break;
                    case "lrecl":
                        if (cell0.getCellType() == NUMERIC) {
                            double lreclNumericD = cell0.getNumericCellValue();
                            int lreclNumeric = (int) lreclNumericD;
                            lrecl = String.valueOf(lreclNumeric);
                        } else {

                            lrecl = cell0.getStringCellValue();
                        }
                        break;
                    case "recfm":
                        recfm = cell0.getStringCellValue();
                        break;
                    case "dsorg":
                        dsorg = cell0.getStringCellValue();
                        break;
                }


                switch (header1.toLowerCase()) {
                    case "dsname":
                        fileName = cell1.getStringCellValue();
                        break;
                    case "lrecl":
                        if (cell1.getCellType() == NUMERIC) {
                            double lreclNumericD = cell1.getNumericCellValue();
                            int lreclNumeric = (int) lreclNumericD;
                            lrecl = String.valueOf(lreclNumeric);
                        } else {

                            lrecl = cell1.getStringCellValue();
                        }
                        break;
                    case "recfm":
                        recfm = cell1.getStringCellValue();
                        break;
                    case "dsorg":
                        dsorg = cell1.getStringCellValue();
                        break;
                }

                switch (header2.toLowerCase()) {
                    case "dsname":
                        fileName = cell2.getStringCellValue();
                        break;
                    case "lrecl":
                        if (cell2.getCellType() == NUMERIC) {
                            double lreclNumericD = cell2.getNumericCellValue();
                            int lreclNumeric = (int) lreclNumericD;
                            lrecl = String.valueOf(lreclNumeric);
                        } else {

                            lrecl = cell2.getStringCellValue();
                        }
                        break;
                    case "recfm":
                        recfm = cell2.getStringCellValue();
                        break;
                    case "dsorg":
                        dsorg = cell2.getStringCellValue();
                        break;
                }


                switch (header3.toLowerCase()) {
                    case "dsname":
                        fileName = cell3.getStringCellValue();
                        break;
                    case "lrecl":
                        if (cell3.getCellType() == NUMERIC) {
                            double lreclNumericD = cell3.getNumericCellValue();
                            int lreclNumeric = (int) lreclNumericD;
                            lrecl = String.valueOf(lreclNumeric);
                        } else {

                            lrecl = cell3.getStringCellValue();
                        }
                        break;
                    case "recfm":
                        recfm = cell3.getStringCellValue();
                        break;
                    case "dsorg":
                        dsorg = cell3.getStringCellValue();
                        break;
                }


//                fileName = cellFileName.getStringCellValue();
//
//                if (cellLrecl.getCellType() == NUMERIC) {
//                    double lreclNumericD = cellLrecl.getNumericCellValue();
//                    int lreclNumeric = (int) lreclNumericD;
//                    lrecl = String.valueOf(lreclNumeric);
//                } else {
//
//                    lrecl = cellLrecl.getStringCellValue();
//                }
//                recfm = cellRecfm.getStringCellValue();
//                dsorg = cellDsorg.getStringCellValue();
                jclModel.setActualfile(fileName);
                jclModel.setLrecl(lrecl);
                jclModel.setRecfm(recfm);
                jclModel.setDsorg(dsorg);
                jclModelList.add(jclModel);


            }


//             int times =  jclModelList.size()/250 +1;

            List<List<JclModel>> subLists = ListUtils.partition(new ArrayList<JclModel>(jclModelList), 250);

            LocalDate todaysDate = LocalDate.now();
            String exportFileNameile = "/" + todaysDate + "CatalogJcl";

            for (int j = 0; j < subLists.size(); j++) {
                int serialno = j + 1;
                try (FileWriter fileWriter = new FileWriter(outputPath + exportFileNameile + serialno + ".jcl");

                     BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                    bufferedWriter.write(INIT_JCL);
                    bufferedWriter.newLine();

                    for (int i = 0; i < subLists.get(j).size(); i++) {
                        if (subLists.get(j).get(i).getDsorg().toLowerCase().equals("vsam")) {
                            bufferedWriter.write(generateVsamParagraph(i, subLists.get(j).get(i)));
                            bufferedWriter.newLine();
                        } else if (subLists.get(j).get(i).getDsorg().toLowerCase().equals("po")) {
                            bufferedWriter.write(generatePoParagraph(i, subLists.get(j).get(i)));
                            bufferedWriter.newLine();
                        } else {
                            bufferedWriter.write(generateParagraph(i, subLists.get(j).get(i)));
                            bufferedWriter.newLine();
                        }
                    }

                    System.out.println(serialno + " JCL file generated successfully !!");


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void generateDeleteJclFromXlsx() {

        List<JclModel> jclModelList = new ArrayList<>();


        String config = "config.properties";
        Properties props = new Properties();
        try {
            props.load(new InputStreamReader(new FileInputStream(config), "UTF-8"));

            String mappingExcel = props.getProperty("excelFile");
            String outputPath = props.getProperty("outputPath");
            FileInputStream inp = new FileInputStream(mappingExcel);
            XSSFWorkbook wb = new XSSFWorkbook(inp);
            inp.close();

            XSSFSheet sheet = wb.getSheetAt(0);
            int rowLength = sheet.getLastRowNum();

            String fileName = null;
            String lrecl = null;
            String recfm = null;

            for (int i = 1; i <= rowLength; i++) {

                JclModel jclModel = new JclModel();

                XSSFRow row = sheet.getRow(i);

                XSSFCell cellFileName = row.getCell(0);
//                XSSFCell cellLrecl = row.getCell(1);
//                XSSFCell cellRecfm = row.getCell(2);

                fileName = cellFileName.getStringCellValue();

//                if (cellLrecl.getCellType() == NUMERIC) {
//                    double lreclNumericD = cellLrecl.getNumericCellValue();
//                    int lreclNumeric = (int) lreclNumericD;
//                    lrecl = String.valueOf(lreclNumeric);
//                } else {
//
//                    lrecl = cellLrecl.getStringCellValue();
//                }
//                recfm = cellRecfm.getStringCellValue();

                jclModel.setActualfile(fileName);
//                jclModel.setLrecl(lrecl);
//                jclModel.setRecfm(recfm);
                jclModelList.add(jclModel);


            }


//             int times =  jclModelList.size()/250 +1;

            List<List<JclModel>> subLists = ListUtils.partition(new ArrayList<JclModel>(jclModelList), 250);

            LocalDate todaysDate = LocalDate.now();
            String exportFileNameile = "/" + todaysDate + "CatalogJcl";

            for (int j = 0; j < subLists.size(); j++) {
                int serialno = j + 1;
                try (FileWriter fileWriter = new FileWriter(outputPath + exportFileNameile + serialno + ".jcl");

                     BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                    bufferedWriter.write(INIT_JCL);
                    bufferedWriter.newLine();

                    for (int i = 0; i < subLists.get(j).size(); i++) {
                        bufferedWriter.write(generateDeleteParagraph(i, subLists.get(j).get(i)));
                        bufferedWriter.newLine();
                    }

                    System.out.println(serialno + " JCL file generated successfully !!");


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
