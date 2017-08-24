package top.mothership.osubot.util;

import junit.framework.TestCase;

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
        final Path path = Paths.get("C:\\\\CoolQ Pro\\data\\image\\resource\\result");
        final List<File> files = new ArrayList<File>();
        SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                files.add(file.toFile());
                return super.visitFile(file, attrs);
            }
        };
        try {
            java.nio.file.Files.walkFileTree(path, finder);
        } catch (IOException e) {
        }
        List<String> names = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            names.add(files.get(i).getName());
        }
    }

}