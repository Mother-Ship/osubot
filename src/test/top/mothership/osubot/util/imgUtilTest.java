package top.mothership.osubot.util;

import com.google.gson.Gson;
import junit.framework.TestCase;
import top.mothership.osubot.pojo.BP;
import top.mothership.osubot.pojo.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by QHS on 2017/8/24.
 */
public class imgUtilTest extends TestCase {
    private static String mainRegex = "[!！]([^ ]+) (.*+)";
    private static String mainRegexWithNum = "[!！]([^ ]+) ([^#]+) #([0-9]+)";
    public void testDrawOneBP() throws Exception {
        String txt="!statme #2";

        Matcher m= Pattern.compile(mainRegex).matcher(txt);
        m.find();

            String c1=m.group(1);
            String word1=m.group(2);
//            String ws1=m.group(3);
//            String var1=m.group(4);
//            String ws2=m.group(5);
//            String c2=m.group(6);
//            String int1=m.group(7);
//            System.out.print("("+c1+")"+"("+word1+")"+"("+ws1+")"+"("+var1+")"+"("+ws2+")"+"("+c2+")"+"("+int1+")"+"\n");
            System.out.print(m.matches());


    }
}

