package es.cristichi.obj;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class Util {

	/**
	 * 
	 * @param resultImage A BufferedImage containing the image data.
	 * @param deckFile    A File, existing or not, to save the data to.
	 * @param quality     0 for higher compression, 1 for higher quality, or
	 *                    any float value in between.
	 * @throws IOException
	 */
	public static void writeJpgImage(BufferedImage resultImage, File deckFile, float quality)
			throws IOException, IllegalArgumentException {
		try (ImageOutputStream ios = ImageIO.createImageOutputStream(deckFile)) {
			ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("JPEG").next();

			ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
			jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			jpgWriteParam.setCompressionQuality(quality);

			jpgWriter.setOutput(ios);

			jpgWriter.write(null, new IIOImage(resultImage, null, null), jpgWriteParam);
			jpgWriter.dispose();
		} catch (IllegalArgumentException exception) {
			throw exception;
		}
	}

	/**
	 * 
	 * @param file Image file
	 * @return a BufferedImage object with the content of an image file.
	 * @throws IOException
	 */
	public synchronized static BufferedImage load(File file) throws IOException {
		byte[] bytes = Files.readAllBytes(file.toPath());
		try (InputStream is = new ByteArrayInputStream(bytes)) {
			return ImageIO.read(is);
		}
	}
}
