package es.cristichi.card_generator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.jopendocument.dom.spreadsheet.Sheet;

import es.cristichi.CristichiVillainousMain;
import es.cristichi.MainInfoFrame;
import es.cristichi.card_generator.obj.DeckTemplate;
import es.cristichi.card_generator.obj.GeneratorReturn;
import es.cristichi.card_generator.obj.TemplateType;
import es.cristichi.card_photocopier.obj.ODS.OdsStructure;
import es.cristichi.obj.CardInfo;
import es.cristichi.obj.ExtraDeckInfo;
import es.cristichi.obj.Util;
import es.cristichi.obj.config.ConfigValue;
import es.cristichi.obj.config.Configuration;

public class CardGenerator {
	private static Font CARD_NAME_FONT = new Font("Esteban", Font.PLAIN, 100);
	private static Font CARD_TEXT_MAX;
	private static Font CARD_TYPE = new Font("Cabin", Font.BOLD, 95);
	private static Font CARD_CORNER_VALUES;

	private static Rectangle NAME_COORDS = new Rectangle(173, 1090, 1094, 137);
	private static Rectangle TEXT_COORDS = new Rectangle(140, 1292, 1160, 612);

	private static Dimension ART_SIZE = new Dimension(1440, 970);

	static {
		Map<TextAttribute, Object> fontTextAtts = new HashMap<>();

		fontTextAtts.put(TextAttribute.FAMILY, "Cabin");
		fontTextAtts.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_MEDIUM);
		fontTextAtts.put(TextAttribute.SIZE, 85);

		CARD_TEXT_MAX = new Font(fontTextAtts);

		Map<TextAttribute, Object> fontCornerAtts = new HashMap<>();

		fontCornerAtts.put(TextAttribute.FAMILY, "Cabin");
		fontCornerAtts.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_SEMIBOLD);
		fontCornerAtts.put(TextAttribute.SIZE, 120);

		CARD_CORNER_VALUES = new Font(fontCornerAtts);
	}
	
	private ArrayList<String> warnings;

	public GeneratorReturn generate(Configuration config, MainInfoFrame frame, File openDocumentFile, File imagesFolder,
			File resultsFolder, OdsStructure odsStructure, Sheet sheet, ArrayList<CardInfo> usefulCards,
			HashMap<String, ExtraDeckInfo> extraDecks) throws Exception {
		warnings= new ArrayList<>(3);

		if (!config.contains(ConfigValue.CONFIG_TEMPLATES)) {
			config.setValue(ConfigValue.CONFIG_TEMPLATES, ConfigValue.CONFIG_TEMPLATES.getDefaultValue());
			config.saveToFile();
		}

		File artFolder = new File(config.getString(ConfigValue.CONFIG_ART_FOLDER));
		if (!artFolder.exists()) {
			frame.replaceText("The folder where the templates should be (" + artFolder.getAbsolutePath()
					+ ") was not found. Please edit the config file or create it and place the templates there.");
			throw new FileNotFoundException("The folder where the templates should be (" + artFolder.getAbsolutePath()
					+ ") was not found. Please edit the config file or create it and place the templates there.");
		}

		File templatesFolder = new File(config.getString(ConfigValue.CONFIG_TEMPLATES));
		if (!templatesFolder.exists()) {
			frame.replaceText("The folder where the templates should be (" + templatesFolder.getAbsolutePath()
					+ ") was not found. Please edit the config file or create it and place the templates there.");
			throw new FileNotFoundException("The folder where the templates should be ("
					+ templatesFolder.getAbsolutePath()
					+ ") was not found. Please edit the config file or create it and place the templates there.");
		}

		frame.replaceText("Reading arts and layouts (this takes a while)...");
		Semaphore sem = new Semaphore(1 - usefulCards.size());

		for (CardInfo card : usefulCards) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						BufferedImage bi = CardGenerator.this.generateImage(templatesFolder, artFolder, card);
						Util.writeJpgImage(bi, new File(imagesFolder, card.name + ".jpg"), 1);
						frame.replaceText("Generated "
								+ (usefulCards.size() + sem.availablePermits() + "/" + usefulCards.size()));
					} catch (IOException e) {
						e.printStackTrace();
						warnings.add("Error generating " + card.name + ". " + e.getClass().getName() + ": "
								+ e.getMessage());
					}

					sem.release();
				}
			}).start();
		}

		sem.acquire();

		return new GeneratorReturn(warnings, usefulCards, extraDecks);
	}

	private BufferedImage generateImage(File templatesFolder, File artFolder, CardInfo card) throws IOException {
		BufferedImage base = new BufferedImage(CristichiVillainousMain.CARD_SIZE.width,
				CristichiVillainousMain.CARD_SIZE.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D baseG = base.createGraphics();

		try {
			BufferedImage art = DeckTemplate.getArtFile(artFolder, card.name);
			baseG.drawImage(art, 0, 0, ART_SIZE.width, ART_SIZE.height, null);
		} catch (Exception e) {
			e.printStackTrace();
			warnings.add("Error finding the art for "+card.name);
		}

		BufferedImage deck = DeckTemplate.getTemplateFile(templatesFolder, TemplateType.DECK, card.deck);
		baseG.drawImage(deck, 0, 0, base.getWidth(), base.getHeight(), null);
		
		drawCenteredString(baseG, card.name, NAME_COORDS, CARD_NAME_FONT);

		baseG.dispose();

		BufferedImage rgbCopy = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = rgbCopy.createGraphics();
		graphics.drawImage(base, 0, 0, Color.WHITE, null);
		graphics.dispose();
		return rgbCopy;
	}
	
	/**
	 * Draw a String centered in the middle of a Rectangle.
	 *
	 * @param g The Graphics instance.
	 * @param text The String to draw.
	 * @param rect The Rectangle to center the text in.
	 */
	public static void drawCenteredString(Graphics2D g, String text, Rectangle rect, Font font) {
	    // Get the FontMetrics
	    FontMetrics metrics = g.getFontMetrics(font);
	    // Determine the X coordinate for the text
	    int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
	    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
	    int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
	    // Set the font
	    g.setFont(font);
	    // Draw the String
	    g.drawString(text, x, y);
	}
}
