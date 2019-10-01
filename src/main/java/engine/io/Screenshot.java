package engine.io;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import sim.Sim;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Screenshot implements Runnable {

    private String path;

    private ByteBuffer buffer;
    private int width;
    private int height;
    private int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.

    /**
     * Capture the current renderWindow screen and save it to the specified path on the disk.
     */
    public Screenshot(String path) {
        this.path = path;

        // Read buffer in main thread since we have no openGL capabilities in other threads
        buffer = readBuffer();
        // Switch to a new thread to create and save image
        new Thread(this).start();
    }


    /**
     * Capture the current renderWindow screen and save it to "src/main/resources/output/".
     */
    public Screenshot() {
        this("src/main/resources/output/");
    }

    private ByteBuffer readBuffer() {
        GL11.glReadBuffer(GL11.GL_FRONT);
        width = Sim.renderWindow.getWidth();
        height = Sim.renderWindow.getHeight();

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        return buffer;
    }

    @Override
    public void run() {

        // Convert ByteBuffer to BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int i = (x + (width * y)) * bpp;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        // Filename based on current date to miliseconds
        DateFormat df = new SimpleDateFormat("MM-dd_HH-mm-ss.SSS");
        Date now = Calendar.getInstance().getTime();
        String fileName = df.format(now);

        // Save the file to the disk
        try {
            ImageIO.write(image, "PNG", new File( path + fileName + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
