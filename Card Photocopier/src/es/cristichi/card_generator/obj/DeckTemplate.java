package es.cristichi.card_generator.obj;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import es.cristichi.obj.Util;

public class DeckTemplate {
	public static HashMap<String, BufferedImage> templates = new HashMap<>(TemplateType.values().length);
	public static HashMap<String, Semaphore> templateLoading = new HashMap<>(TemplateType.values().length);
	
	public static HashMap<String, BufferedImage> arts = new HashMap<>(TemplateType.values().length);
	public static HashMap<String, Semaphore> artLoading = new HashMap<>(TemplateType.values().length);

	public static BufferedImage getTemplateFile(File folder, TemplateType templateType, String deck)
			throws IOException {
		if (templateLoading.containsKey(templateType.name() + deck + folder.getAbsolutePath())) {
			try {
//				System.out.println("A template file is loading from memory (" + deck +" "+ templateType.name() + ")");
				templateLoading.get(templateType.name() + deck + folder.getAbsolutePath()).acquire();
				templateLoading.get(templateType.name() + deck + folder.getAbsolutePath()).release();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new IOException("Generation was waiting for ita template loading but it was interrupted.", e);
			}
		} else {
//			System.out.println("Thread for (" + deck +" "+ templateType.name() + ") locked.");
			templateLoading.put(templateType.name() + deck + folder.getAbsolutePath(), new Semaphore(0));
		}
		if (templates.containsKey(templateType.name() + deck + folder.getAbsolutePath())) {
			return templates.get(templateType.name() + deck + folder.getAbsolutePath());
		}
//		System.out.println("A template file is loading from disk (" + deck +" "+ templateType.name() + ")");
		File ret = new File(folder, deck + templateType.getFileName() + ".png");
		if (!ret.exists()) {
			throw new FileNotFoundException("A template file for deck \""+deck+"\" is missing (" + ret.getAbsolutePath() + ")");
		}
		BufferedImage retImg = Util.load(ret);
		templates.put(templateType.name() + deck + folder.getAbsolutePath(), retImg);
		templateLoading.get(templateType.name() + deck + folder.getAbsolutePath()).release();
		return retImg;
	}

	public static BufferedImage getArtFile(File artFolder, String cardName) throws IOException {
		if (artLoading.containsKey(artFolder+cardName)) {
			try {
//				System.out.println("An artwork is loading from memory (" + artFolder +" "+ cardName + ")");
				artLoading.get(artFolder+cardName).acquire();
				artLoading.get(artFolder+cardName).release();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new IOException("Generation was waiting for its artwork to load but it was interrupted.", e);
			}
		} else {
//			System.out.println("Thread for (" + artFolder +" "+ cardName + ") locked.");
			artLoading.put(artFolder+cardName, new Semaphore(0));
		}
		if (arts.containsKey(artFolder+cardName)) {
			return arts.get(artFolder+cardName);
		}
//		System.out.println("Loading artwork from disk (" + artFolder +" "+ cardName + ")");
		File ret = new File(artFolder, cardName + ".jpg");
		if (!ret.exists()) {
			throw new FileNotFoundException("An artwork is missing (" + ret.getAbsolutePath() + ")");
		}
		return Util.load(ret);
	}
}
