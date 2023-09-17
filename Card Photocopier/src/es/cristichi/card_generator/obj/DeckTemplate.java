package es.cristichi.card_generator.obj;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import es.cristichi.obj.Util;

public class DeckTemplate {
	
	public static BufferedImage getTemplateFile(File folder, TemplateType templateType, String deck) throws IOException {
		File ret = new File(folder, deck + templateType.getFileName() + ".png");
		if (!ret.exists()) {
			throw new FileNotFoundException("A template file is missing (" + ret.getAbsolutePath() + ")");
		}
		return Util.load(ret);
	}

	public static BufferedImage getArtFile(File artFolder, String cardName) throws IOException {
		File ret = new File(artFolder, cardName + ".jpg");
		if (!ret.exists()) {
			throw new FileNotFoundException("A template file is missing (" + ret.getAbsolutePath() + ")");
		}
		return Util.load(ret);
	}
}
