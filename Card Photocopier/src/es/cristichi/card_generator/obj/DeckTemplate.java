package es.cristichi.card_generator.obj;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import es.cristichi.obj.Util;

public class DeckTemplate {
	public static HashMap<String, BufferedImage> templates = new HashMap<>(TemplateType.values().length);
	
	public static BufferedImage getTemplateFile(File folder, TemplateType templateType, String deck) throws IOException {
		if (templates.containsKey(templateType.name()+deck+folder.getAbsolutePath())) {
			return templates.get(templateType.name()+deck+folder.getAbsolutePath());
		}
		File ret = new File(folder, deck + templateType.getFileName() + ".png");
		if (!ret.exists()) {
			throw new FileNotFoundException("A template file is missing (" + ret.getAbsolutePath() + ")");
		}
		BufferedImage retImg = Util.load(ret);
		templates.put(templateType.name()+deck+folder.getAbsolutePath(), retImg);
		return retImg;
	}

	public static BufferedImage getArtFile(File artFolder, String cardName) throws IOException {
		File ret = new File(artFolder, cardName + ".jpg");
		if (!ret.exists()) {
			throw new FileNotFoundException("A template file is missing (" + ret.getAbsolutePath() + ")");
		}
		return Util.load(ret);
	}
}
