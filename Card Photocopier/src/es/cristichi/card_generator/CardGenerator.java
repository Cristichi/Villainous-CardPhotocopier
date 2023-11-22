package es.cristichi.card_generator;

import java.awt.Color;
import java.awt.Desktop;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.util.StringUtils;

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
	private static Font FONT_CARD_NAME = new Font("Esteban", Font.PLAIN, 100);
	private static Font FONT_TEXT_MAX;
	// private static Font FONT_TYPE = new Font("Cabin", Font.BOLD, 95);
	private static Font FONT_TYPE = new Font("Cabin", Font.BOLD, 85);
	private static Font FONT_CORNER_VALUES;

	private static Color COLOR_TEXT_VILLAIN = new Color(210, 170, 110); // #D8B47F
	private static Color COLOR_TEXT_FATE = Color.BLACK; // #000000
	// private static Color EFFECT_COLOR = new Color(122, 196, 36); // #7ac424
	// private static Color ALLY_COLOR = new Color(222, 0, 34); // #de0022
	// private static Color ITEM_COLOR = new Color(69, 175, 230); // #45afe6
	// private static Color CONDIT_COLOR = new Color(211, 86, 141); // #d3568d
	// private static Color HERO_COLOR = new Color(230, 140, 10); // #e68c0a

	private static Rectangle CARD_COORDS = new Rectangle(0, 0, 1440, 2044);

	private static Rectangle NAME_COORDS = new Rectangle(173, 1090, 1094, 137);
	private static Rectangle ABILITY_COORDS = new Rectangle(140, 1292, 1160, 612);
//	private static Rectangle ABILITY_COORDS = new Rectangle(140, 0, 1160, 2000); // For testing if huge texts are shown properly if they can
//	private static Rectangle TYPE_COORDS = new Rectangle(0, 1949, 1440, 72);
//	private static Rectangle TYPE_COORDS = new Rectangle(0, 1883, 1440, 161);
	private static Rectangle TYPE_COORDS = new Rectangle(0, 1889, 1440, 161); // Callibrated manually
//	private static Rectangle COST_COORDS = new Rectangle(109, 117, 156, 156);
	private static Rectangle COST_COORDS = new Rectangle(78, 86, 218, 218);
	private static Rectangle STRENGTH_COORDS = new Rectangle(61, 1827, 156, 156);
	private static Rectangle TOP_RIGHT_COORDS = new Rectangle(1175, 118, 156, 156);
	private static Rectangle BOTTOM_RIGHT_COORDS = new Rectangle(1223, 1826, 156, 156);

	private static Rectangle ART_COORDS = new Rectangle(0, 0, 1440, 970);

	static {
		Map<TextAttribute, Object> fontTextAtts = new HashMap<>();
		fontTextAtts.put(TextAttribute.FAMILY, "Cabin");
		//TODO: Is the weight happening? Who knows! Check it, and compare with FailureFactory's and Original's
		fontTextAtts.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_MEDIUM);
		fontTextAtts.put(TextAttribute.SIZE, 85);
		FONT_TEXT_MAX = new Font(fontTextAtts);

		Map<TextAttribute, Object> fontCornerAtts = new HashMap<>();
		fontCornerAtts.put(TextAttribute.FAMILY, "Cabin");
		fontCornerAtts.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_SEMIBOLD);
		fontCornerAtts.put(TextAttribute.SIZE, 120);
		FONT_CORNER_VALUES = new Font(fontCornerAtts);
	}

	private ArrayList<String> warnings;

	public GeneratorReturn generate(Configuration config, MainInfoFrame frame, File openDocumentFile, File imagesFolder,
			File resultsFolder, OdsStructure odsStructure, Sheet sheet, ArrayList<CardInfo> usefulCards,
			HashMap<String, ExtraDeckInfo> extraDecks) throws Exception {
		warnings = new ArrayList<>(3);

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
		Color textColor = (card.deck == "Fate" ? COLOR_TEXT_FATE : COLOR_TEXT_VILLAIN);
		String htmlAbility = applyTags(card.ability.trim());
		String htmlType = applyTags(card.type.trim());

		// Base
		BufferedImage resImage = new BufferedImage(CARD_COORDS.width, CARD_COORDS.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D resImageG = resImage.createGraphics();
		
		// Art
		try {
			BufferedImage art = DeckTemplate.getArtFile(artFolder, card.name);
			resImageG.drawImage(art, null, ART_COORDS.x, ART_COORDS.y);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			warnings.add("Error finding the art for " + card.name);
		} catch (Exception e) {
			e.printStackTrace();
			warnings.add("Error reading the art for " + card.name);
		}

		// Deck
		BufferedImage deck = DeckTemplate.getTemplateFile(templatesFolder, TemplateType.DECK, card.deck);
		resImageG.drawImage(deck, null, 0, 0);
		
		// Name
		drawCenteredHTMLString(resImageG, card.name.toUpperCase(), NAME_COORDS, FONT_CARD_NAME, textColor);
		
		// Type
		drawCenteredHTMLString(resImageG, htmlType, TYPE_COORDS, FONT_TYPE, textColor);
		
		// Cost
		if (!card.cost.equals("")) {
			BufferedImage costTempl = DeckTemplate.getTemplateFile(templatesFolder, TemplateType.COST, card.deck);
			resImageG.drawImage(costTempl, null, 0, 0);
			drawCenteredString(resImageG, card.cost, COST_COORDS, FONT_CORNER_VALUES, COLOR_TEXT_VILLAIN);
		}
		
		// Strength
		if (!card.strength.equals("")) {
			BufferedImage strengthTempl = DeckTemplate.getTemplateFile(templatesFolder, TemplateType.STRENGTH,
					card.deck);
			resImageG.drawImage(strengthTempl, null, 0, 0);
			drawCenteredString(resImageG, card.strength, STRENGTH_COORDS, FONT_CORNER_VALUES, COLOR_TEXT_VILLAIN);
		}
		
		// Top Right
		if (!card.topRight.equals("")) {
			BufferedImage topRightTempl = DeckTemplate.getTemplateFile(templatesFolder, TemplateType.TOP_RIGHT,
					card.deck);
			resImageG.drawImage(topRightTempl, null, 0, 0);
			drawCenteredString(resImageG, card.topRight, TOP_RIGHT_COORDS, FONT_CORNER_VALUES, COLOR_TEXT_VILLAIN);
		}
		
		// Bottom Right
		if (!card.bottomRight.equals("")) {
			BufferedImage botRightTempl = DeckTemplate.getTemplateFile(templatesFolder, TemplateType.BOTTOM_RIGHT,
					card.deck);
			resImageG.drawImage(botRightTempl, null, 0, 0);
			drawCenteredString(resImageG, card.topRight, BOTTOM_RIGHT_COORDS, FONT_CORNER_VALUES, COLOR_TEXT_VILLAIN);
		}
		
		// Ability
		drawCenteredHTMLParagraph(resImageG, htmlAbility, ABILITY_COORDS, FONT_TEXT_MAX, textColor, 1);
		
		return resImage;
	}
	
	/**
	 * It applies color to keywords.
	 * @param text
	 * @return
	 */
	//TODO: This configurable, also perhaps add the things for PirVil?
	private String applyTags(String text) {
		return text
				.replace("Effects", "<span style=\"color: #7ac424\">Effects</span>")
				.replace("Effect", "<span style=\"color: #7ac424\">Effect</span>")
				.replace("Allies", "<span style=\"color: #de0022\">Allies</span>")
				.replace("Ally", "<span style=\"color: #de0022\">Ally</span>")
				.replace("Items", "<span style=\"color: #45afe6\">Items</span>")
				.replace("Item", "<span style=\"color: #45afe6\">Item</span>")
				.replace("Conditions", "<span style=\"color: #d3568d\">Conditions</span>")
				.replace("Condition", "<span style=\"color: #d3568d\">Condition</span>")
				.replace("Heroes", "<span style=\"color: #e68c0a\">Heroes</span>")
				.replace("Hero", "<span style=\"color: #e68c0a\">Hero</span>")
				;
	}

	/**
	 * Draw a String centered in the middle of a Rectangle.
	 *
	 * @param g The Graphics instance.
	 * @param text The String to draw.
	 * @param rect The Rectangle to center the text in.
	 * 
	 * @author https://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java
	 */
	private void drawCenteredString(Graphics2D g, String text, Rectangle rect, Font font, Color textColor) {
	    // Get the FontMetrics
	    FontMetrics metrics = g.getFontMetrics(font);
	    // Set the font & color
	    g.setFont(font);
	    g.setColor(textColor);
	    // Determine the X coordinate for the text
	    int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
	    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
	    // Added small correction with -2
	    int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent() -2;
	    // Draw the String
	    g.drawString(text, x, y);
	}
	
	private void drawCenteredHTMLString(Graphics2D g, String htmlText, Rectangle rect, Font font, Color textColor) {
		JLabel label = new JLabel("<html>"+htmlText);
		label.setBounds(rect);
		label.setLocation(rect.getLocation());
		label.setFont(font);
		label.setForeground(textColor);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		
		BufferedImage type = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
		label.paint(type.getGraphics());
		g.drawImage(type, null, rect.x, rect.y);
	}
	
	/**
	 * Used to print a paragraph of text as several "p" in an html. It supports coloring and other styles.
	 * @param graphics The graphics in which to draw this paragraph.
	 * @param htmlText The HTML text. "html", "style" and "body" tags will be included, as well as an initial "p", so please include just regular text. "/n", "   " and "br" will be interpreted as new line markers.
	 * @param rect
	 * @param font
	 * @param textColor
	 * @param resizeStep
	 */
	private void drawCenteredHTMLParagraph(Graphics2D graphics, String htmlText, Rectangle rect, Font font, Color textColor, int resizeStep) {
		htmlText = htmlText.replace("   ", "\n").replace("\n", "<br>").replace("<br>", "</p><p>")
				.replace("</p><p>", " "); //Adding this for testing on already existing parts
		
		char[] charsTextNoTags = htmlText.toCharArray();
		StringBuilder textNoTags = new StringBuilder(htmlText.length());
		boolean insideTag = false;
		for (int i = 0; i < charsTextNoTags.length; i++) {
			if (charsTextNoTags[i] == '<'){
				insideTag = true;
			} else if (charsTextNoTags[i] == '>'){
				insideTag = false;
			} else if (!insideTag) {
				textNoTags.append(charsTextNoTags[i]);
			}
		}
		String htmlTextNoTags = textNoTags.toString();
		charsTextNoTags = htmlTextNoTags.toCharArray();
		
		Font fontFinal = font;
		do {
//			System.out.println("Font size: "+fontFinal.getSize());
			FontMetrics currentFontMetrics = graphics.getFontMetrics(fontFinal);
			int oneLineHeight = currentFontMetrics.getHeight();
			// Thank you: https://stackoverflow.com/questions/12129633/how-do-i-render-wrapped-text-on-an-image-in-java/12129735?r=Saves_AllUserSaves#12129735
			List<String> listLines = StringUtils.wrap(htmlTextNoTags, currentFontMetrics, ABILITY_COORDS.width);
//			System.out.println(listLines.size());
//			for (String string : listLines) {
//				System.out.println(string);
//			}
//			System.out.println(oneLineHeight * listLines.size() +" ?? "+ABILITY_COORDS.height);
			if (oneLineHeight * listLines.size() < ABILITY_COORDS.height){
				break;
			} else {
				fontFinal = fontFinal.deriveFont((float) fontFinal.getSize()-resizeStep);
			}
		} while (true);
//		System.out.println("Font initial size: "+font.getSize());
//		System.out.println("Font final size: "+fontFinal.getSize());
		
		// Left padding to compensate for how <p> works sometimes Madge calculated with GIMP xdd
		JLabel label = new JLabel("<html><style>html{width:" + rect.width + "}body{text-align:center;padding-left:16px;}</style><body><div><p>"+htmlText);
		label.setBounds(rect);
		label.setLocation(rect.getLocation());
		label.setForeground(textColor);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setFont(fontFinal);
		BufferedImage type = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
		label.paint(type.getGraphics());
		graphics.drawImage(type, null, rect.x, rect.y);
	}

	/**
	 * Testing purposes
	 */
	public static void main(String[] args) throws Exception {
		CardGenerator cGen = new CardGenerator();
		cGen.warnings = new ArrayList<>(3);

		CardInfo ci = new CardInfo(null);
		ci.name = "Riptide Rex";
		ci.deck = "Villain";
		ci.extraDeck = "";

		ci.cost = "?";
		ci.strength = "32";
		ci.ability = "When Riptide Rex is played, please take one +1 Strength Token and put it on yourself for being so good at this game. Then, take a -1 Strength Token and put it on an opponent because nobody is left alive after trying to conquer your domain.";
//		ci.ability = "Riptide Rex may be used to defeat a Hero at an adjacent location if he is at The Dreadway's location.";
		ci.ability = "All Heroes and Allies and Items and Heroes and Conditions and Effects and Heroes again are banned to horny jail. Then all Heroes and Allies and Allies again and Heroes again, except that Hero, he knows what he did, and all Items get out of Jail for free. Then jail them again. Then cut out their tongues, except the Items they don't have tongues. And don't forget about THAT Hero you understand? Are you taking notes?";
//		ci.ability = "When Riptide Rex do the good.";
//		ci.ability = "You won   gg";

		ci.activateAbility = "";
		ci.activateCost = "";

		ci.topRight = "2";
		ci.bottomRight = "7";
		ci.action = "";
		ci.credits = "Riot Games";

		ci.type = "Ally";
		ci.copies = 1;
		ci.desc = "desc";
		ci.row = 22;

		BufferedImage bi = cGen.generateImage(new File("-Layout"), new File("-Images"), ci);

		File testFile = new File("-Exports/test " + ci.name + " long.jpg");
		Util.writeJpgImage(bi, testFile, 1);
		Desktop.getDesktop().open(testFile);

		ci.ability = "When Riptide Rex do the good.";
		
		bi = cGen.generateImage(new File("-Layout"), new File("-Images"), ci);

		testFile = new File("-Exports/test " + ci.name + " short.jpg");
		Util.writeJpgImage(bi, testFile, 1);
		Desktop.getDesktop().open(testFile);
		
		System.out.println("Warnings:");
		System.out.println(cGen.warnings.toString());
	}
}
