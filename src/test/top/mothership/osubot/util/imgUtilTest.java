package top.mothership.osubot.util;

import com.google.gson.Gson;
import junit.framework.TestCase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import top.mothership.osubot.pojo.BP;
import top.mothership.osubot.pojo.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by QHS on 2017/8/24.
 */
public class imgUtilTest extends TestCase {
    private final String getUserURL = "https://osu.ppy.sh/u/";
    public void testDrawOneBP() throws Exception {

        Document doc = Jsoup.connect(getUserURL + "2").get();

        Elements link = doc.select("time[class*=timeago]");
        String a = link.text();
        a = a.substring(0,19);

        Date date2 = new Date(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(a).getTime()+8*3600*1000);
    }
}

