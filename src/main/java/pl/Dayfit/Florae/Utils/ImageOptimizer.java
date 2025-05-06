package pl.Dayfit.Florae.Utils;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * The ImageOptimizer class provides functionality for optimizing images by compressing
 * and resizing them while maintaining a specified quality.
 * Images in JPEG, JPG, and PNG formats are supported.
 */
public class ImageOptimizer {
    private final static int DEFAULT_WIDTH = 1000;
    private final static int DEFAULT_HEIGHT = 1000;

    /**
     * Optimizes an image by compressing its size and resizing it to default dimensions
     * while maintaining the specified quality.
     *
     * @param file The image file to be optimized; must be of type JPEG, JPG, or PNG.
     * @param quality The desired quality level of the output image, ranging from 0.0 (low quality) to 1.0 (high quality).
     * @return A byte array representing the compressed and resized image.
     * @throws IOException If an I/O error occurs during the optimization process.
     * @throws IllegalArgumentException if the file is not of type JPEG, JPG, or PNG
     */
    public static byte[] optimizeImage(MultipartFile file, float quality) throws IOException, IllegalArgumentException {

        return optimizeImage(file, quality, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Optimizes an image by resizing it to the specified dimensions while maintaining its aspect ratio
     * and compressing it based on the provided quality factor. Supports JPEG, JPG, and PNG formats.
     *
     * @param file the image file to be optimized; must be of type JPEG, JPG, or PNG
     * @param quality the quality factor for compression (values between 0.0 and 1.0, where 1.0 is maximum quality and 0.0 is maximum compression)
     * @param width the target width for resizing the image
     * @param height the target height for resizing the image
     * @return a byte array of the optimized image in the original format
     * @throws IOException if an I/O error occurs during processing
     * @throws IllegalArgumentException if the file is not of type JPEG, JPG, or PNG
     */
    public static byte[] optimizeImage(MultipartFile file, float quality, int width, int height) throws IOException, IllegalArgumentException
    {
        if (!Objects.equals(file.getContentType(), "image/jpeg") && !Objects.equals(file.getContentType(), "image/jpg") && !Objects.equals(file.getContentType(), "image/png"))
        {
            throw new IllegalArgumentException("Files are not of type JPEG, PNG or JPG");
        }

        BufferedImage inputImage = ImageIO.read(file.getInputStream());

        float widthRatio = (float) width / inputImage.getWidth();
        float heightRatio = (float) height / inputImage.getHeight();

        float ratio = Math.min(widthRatio, heightRatio);
        width = (int) (inputImage.getWidth() * ratio);
        height = (int) (inputImage.getHeight() * ratio);

        Image tmpImage = inputImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(tmpImage, 0, 0, null);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (file.getContentType().equals("image/jpeg") || file.getContentType().equals("image/jpg"))
        {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);

            writer.write(null, new IIOImage(outputImage, null, null), param);

            return baos.toByteArray();
        }

        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(outputImage, null, null), null);

        return baos.toByteArray();
    }
}
