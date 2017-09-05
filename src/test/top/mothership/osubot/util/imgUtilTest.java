package top.mothership.osubot.util;

import com.google.gson.Gson;
import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
   private dbUtil dbUtil = new dbUtil();
   private apiUtil apiUtil = new apiUtil();
    private Logger logger = LogManager.getLogger(this.getClass());
    public void testDrawOneBP() throws Exception {

    }
}

