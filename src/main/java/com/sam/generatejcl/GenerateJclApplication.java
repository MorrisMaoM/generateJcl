package com.sam.generatejcl;

import com.sam.generatejcl.service.FileCompare;
import com.sam.generatejcl.service.JclFile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class GenerateJclApplication {

    public static void main(String[] args) {

        SpringApplication.run(GenerateJclApplication.class, args);

        JclFile jclFile = new JclFile();

        String funtionString = null;
        Scanner scanner = new Scanner(System.in);


        while (true) {

            System.out.println("要生成JCL的類型");
            System.out.println("1) 新增CATALOG");
            System.out.println("2) 刪除CATALOG");
            System.out.println("輸入數字");
            funtionString = scanner.next();

            if (funtionString.equals("1")) {
                //用DB產JCL
//        jclFile.generateJcl();
                //用excel產JCL
                jclFile.generateJclFromXlsx();
                System.out.println("執行產CATALOG");
                break;
            }

            if (funtionString.equals("2")) {

                jclFile.generateDeleteJclFromXlsx();
                System.out.println("執行刪除CATALOG");
                break;
            }
        }


//        FileCompare fileCompare = new FileCompare();
//        fileCompare.printDiff();
    }

}
