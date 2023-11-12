package flocean.module.peeper.fjnuoj.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.Random;

public class ImgConvert {

    public static final int MAX_WIDTH = 1024;

    /**
     * 计算文本的长度
     *
     * @param font    字体
     * @param content 文本内容
     * @return 文本的长度
     */
    public static int calculateStringWidth(Font font, String content) {
        JLabel label = new JLabel(content);
        label.setFont(font);
        return label.getFontMetrics(label.getFont()).stringWidth(label.getText());
    }

    /**
     * 计算一段文本的高度
     *
     * @param font           字体
     * @param content        文本内容
     * @param maxWidth       文本最大长度
     * @param lineMultiplier 行距
     * @return 所给文本的高度
     */
    public static int calculateStringHeight(Font font, String content, int maxWidth, double lineMultiplier) {
        int height = 0;
        for (String text : content.split("\n")) {
            JLabel label = new JLabel(text);
            label.setFont(font);
            FontMetrics metrics = label.getFontMetrics(label.getFont());
            int textH = (int) (metrics.getHeight() * lineMultiplier);
            int textW = metrics.stringWidth(label.getText()); //字符串的宽
            String tempText = text;
            while (textW > maxWidth) {
                int n = textW / maxWidth;
                int subPos = tempText.length() / n;
                String drawText = tempText.substring(0, subPos);
                int subTxtW = metrics.stringWidth(drawText);
                while (subTxtW > maxWidth) {
                    subPos--;
                    drawText = tempText.substring(0, subPos);
                    subTxtW = metrics.stringWidth(drawText);
                }
                height += textH;
                textW = textW - subTxtW;
                tempText = tempText.substring(subPos);
            }
            height += textH;
        }
        return height;
    }

    /**
     * 绘制文本
     *
     * @param graphics       目标图片
     * @param content        包装后的文本内容
     * @param x              文本左上角的横坐标
     * @param y              文本左上角的纵坐标
     * @param maxWidth       文本最大长度
     * @param lineMultiplier 行距
     */
    public static void drawString(Graphics graphics, StyledString content, int x, int y, int maxWidth, double lineMultiplier) {
        int offset = 0;
        for (String text : content.content.split("\n")) {
            JLabel label = new JLabel(text);
            label.setFont(content.font);
            FontMetrics metrics = label.getFontMetrics(label.getFont());
            int textH = (int) (metrics.getHeight() * lineMultiplier);
            int textW = metrics.stringWidth(label.getText()); //字符串的宽
            String tempText = text;
            if (offset == 0) offset += textH;
            while (textW > maxWidth) {
                int n = textW / maxWidth;
                int subPos = tempText.length() / n;
                String drawText = tempText.substring(0, subPos);
                int subTxtW = metrics.stringWidth(drawText);
                while (subTxtW > maxWidth) {
                    subPos--;
                    drawText = tempText.substring(0, subPos);
                    subTxtW = metrics.stringWidth(drawText);
                }
                graphics.setFont(content.font);
                graphics.drawString(drawText, x, y + offset);
                offset += textH;
                textW = textW - subTxtW;
                tempText = tempText.substring(subPos);
            }
            graphics.setFont(content.font);
            graphics.drawString(tempText, x, y + offset);
            offset += textH;
        }
    }


    /**
     * 给图片应用覆盖色
     *
     * @param image 图片
     * @param tint  覆盖色
     * @return 处理完后的图片
     */
    public static Image applyTint(Image image, Color tint) {
        ImageFilter filter = new RGBImageFilter() {
            @Override
            public int filterRGB(int x, int y, int rgb) {
                return (rgb & 0xff000000) | (tint.getRed() << 16) | (tint.getGreen() << 8) | tint.getBlue();
            }
        };

        ImageProducer producer = new FilteredImageSource(image.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(producer);
    }


    public static class GradientColors {
        private static final String[][] colors = {
                {"#C6FFDD", "#FBD786", "#f7797d"},
                {"#009FFF", "#ec2F4B"},
                {"#22c1c3", "#fdbb2d"},
                {"#3A1C71", "#D76D77", "#FFAF7B"},
                {"#00c3ff", "#ffff1c"},
                {"#FEAC5E", "#C779D0", "#4BC0C8"},
                {"#C9FFBF", "#FFAFBD"},
                {"#FC354C", "#0ABFBC"},
                {"#355C7D", "#6C5B7B", "#C06C84"},
                {"#00F260", "#0575E6"},
                {"#FC354C", "#0ABFBC"},
                {"#833ab4", "#fd1d1d", "#fcb045"},
                {"#FC466B", "#3F5EFB"}
        };


        /**
         * 随机抽取一个渐变色
         *
         * @return 渐变色 <每个颜色所处的位置, 颜色>
         */
        public static Pair<float[], Color[]> generateGradient() {
            Random random = new Random();
            String[] nowColor = colors[random.nextInt(colors.length)];
            //50%的概率翻转渐变色
            if (random.nextInt(1000) % 2 == 1) {
                String beginColor = nowColor[nowColor.length - 1];
                nowColor[nowColor.length - 1] = nowColor[0];
                nowColor[0] = beginColor;
            }
            if (nowColor.length == 2) {
                return Pair.of(new float[]{0.0f, 1.0f}, new Color[]{Color.decode(nowColor[0]), Color.decode(nowColor[1])});
            } else {
                return Pair.of(new float[]{0.0f, 0.5f, 1.0f}, new Color[]{Color.decode(nowColor[0]), Color.decode(nowColor[1]), Color.decode(nowColor[2])});
            }
        }
    }

    /**
     * 包含绘制需要的参数的字符串
     */
    public static class StyledString {
        public String content;
        public int height;
        public Font font;
        public double lineMultiplier;

        public StyledString(String content, Font font, double lineMultiplier) {
            this.content = content;
            this.font = font;
            this.height = calculateStringHeight(font, content, MAX_WIDTH, lineMultiplier);
            this.lineMultiplier = lineMultiplier;
        }

        public StyledString(String content, Font font) {
            this(content, font, 1.0);
        }
    }
}
