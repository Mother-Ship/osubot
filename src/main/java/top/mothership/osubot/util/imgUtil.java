package top.mothership.osubot.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class imgUtil {
    private Logger logger = LogManager.getLogger(this.getClass());
    //TODO 绘图方法
//TODO 最后生成之后压缩图片减少发送时间
    private BufferedImage image;// 用于调整亮度的缓冲图像对象
    private BufferedImage oldImage;// 用于存放调整亮度之前的原缓冲图像对象

    public void drawTest() throws IOException {

        logger.info("开始绘图");
        //https://a.ppy.sh/2545898?.png
        //
        File file = new File("e:\\1.png");
        BufferedImage img = ImageIO.read(file);  // 创建图像对象;
        //参数1
        String s = "Imouto OuO";

        Font font = new Font("苹方", Font.BOLD, 24); // 创建字体对象


        logger.debug(font.getSize());

        Graphics2D g2 = (Graphics2D) img.getGraphics();
        //指定字体颜色
        g2.setPaint(Color.BLACK);
        //开启平滑
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        //指定字体
        g2.setFont(font);
        //参数2
        //测试循环画100下的性能
        logger.info("开始绘图2");

        g2.drawString(s, 100, 100);

        File file2 = new File("e:\\2.png");
        ImageIO.write(img, "png", file2);

        logger.info(file2.getAbsolutePath());

    }



}
