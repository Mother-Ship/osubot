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

/**
 * Created by QHS on 2017/8/24.
 */
public class imgUtilTest extends TestCase {
    public void testDrawOneBP() throws Exception {
        imgUtil imgUtil = new imgUtil();

        BP bp = new BP();
        bp.setBeatmap_id(53554);
        bp.setRank("XH");
        bp.setPerfect(1);

    }

}