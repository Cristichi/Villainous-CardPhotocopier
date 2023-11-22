package es.cristichi.card_generator;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.jopendocument.dom.spreadsheet.Sheet;

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
//	private static Rectangle TYPE_COORDS = new Rectangle(0, 1949, 1440, 72);
//	private static Rectangle TYPE_COORDS = new Rectangle(0, 1883, 1440, 161);
	private static Rectangle TYPE_COORDS = new Rectangle(0, 1889, 1440, 161); // Callibrated manually
//	private static Rectangle COST_COORDS = new Rectangle(109, 117, 156, 156);
	private static Rectangle COST_COORDS = new Rectangle(78, 86, 218, 218);
	private static Rectangle STRENGTH_COORDS = new Rectangle(61, 1827, 156, 156);
	private static Rectangle TR_COORDS = new Rectangle(1175, 118, 156, 156);
	private static Rectangle BR_COORDS = new Rectangle(1223, 1826, 156, 156);

	private static Rectangle ART_COORDS = new Rectangle(0, 0, 1440, 970);

	static {
		Map<TextAttribute, Object> fontTextAtts = new HashMap<>();
		fontTextAtts.put(TextAttribute.FAMILY, "Cabin");
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

	/**
	private BufferedImage generateImage(File templatesFolder, File artFolder, CardInfo card) throws IOException {
		JPanel panelCard = new JPanel();
		panelCard.setBackground(Color.GREEN);
		panelCard.setLayout(null);
		panelCard.setSize(CristichiVillainousMain.CARD_SIZE);
		panelCard.setPreferredSize(CristichiVillainousMain.CARD_SIZE);
		panelCard.setMaximumSize(CristichiVillainousMain.CARD_SIZE);
		panelCard.setMinimumSize(CristichiVillainousMain.CARD_SIZE);
		panelCard.setVisible(true);
		JLabel lblArt = new JLabel();
		lblArt.setBounds(ART_COORDS);
		panelCard.add(lblArt);
		try {
			BufferedImage art = DeckTemplate.getArtFile(artFolder, card.name);
			lblArt.setIcon(new ImageIcon(art));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			warnings.add("Error finding the art for " + card.name);
		} catch (Exception e) {
			e.printStackTrace();
			warnings.add("Error reading the art for " + card.name);
		}
		BufferedImage deckTempl = DeckTemplate.getTemplateFile(templatesFolder, TemplateType.DECK, card.deck);
		JLabel lblDeckTempl = new JLabel(new ImageIcon(deckTempl));
		lblDeckTempl.setBounds(CARD_COORDS);
		panelCard.add(lblDeckTempl);
		// Name
		JLabel lblName = new JLabel("<html><style>" + "html { height: " + NAME_COORDS.height + "; width: "
				+ NAME_COORDS.width + ";}" + "body { height: 100%; width: 100%; text-align: center; }"
				+ "</style><body>" + card.name.trim().toUpperCase().replace("   ", " ") + "</body></html>");
		lblName.setBounds(NAME_COORDS);
		lblName.setFont(FONT_CARD_NAME);
		lblName.setForeground(card.deck == "Fate" ? COLOR_TEXT_FATE : COLOR_TEXT_VILLAIN);
		lblName.setHorizontalAlignment(SwingConstants.CENTER);
		lblName.setVerticalAlignment(SwingConstants.CENTER);
		lblName.setBorder(new LineBorder(Color.RED, 1));
		panelCard.add(lblName);
		// Type
		JLabel lblType = new JLabel(
				"<html><style>" + "html { height: " + TYPE_COORDS.height + "; width: " + TYPE_COORDS.width + ";}"
						+ "body { height: 100%; width: 100%; text-align: center; }" + "</style><body>"
						+ card.type.trim().replace("Effect", "<span style=\"color: #7ac424\">Effect</span>")
								.replace("Ally", "<span style=\"color: #de0022\">Ally</span>")
								.replace("Item", "<span style=\"color: #45afe6\">Item</span>")
								.replace("Condition", "<span style=\"color: #d3568d\">Condition</span>")
								.replace("Hero", "<span style=\"color: #e68c0a\">Hero</span>")
						+ "</body></html>");
		lblType.setBounds(TYPE_COORDS);
		lblType.setFont(FONT_TYPE);
		lblType.setForeground(card.deck == "Fate" ? COLOR_TEXT_FATE : COLOR_TEXT_VILLAIN);
		lblType.setHorizontalAlignment(SwingConstants.CENTER);
		lblType.setVerticalAlignment(SwingConstants.CENTER);
		lblType.setBorder(new LineBorder(Color.RED, 1));
		panelCard.add(lblType);
		// Cost
		JLabel lblCostIcon = new JLabel();
		JLabel lblCostText = new JLabel("<html><style>" + "html { height: " + COST_COORDS.height + "; width: "
				+ COST_COORDS.width + ";}" + "body { height: 100%; width: 100%; text-align: center; }"
				+ "</style><body'>" + card.cost + "</body></html>");
		if (!card.cost.equals("")) {
			BufferedImage costTempl = DeckTemplate.getTemplateFile(templatesFolder, TemplateType.COST, card.deck);
			lblCostIcon.setIcon(new ImageIcon(costTempl));
			lblCostIcon.setBounds(CARD_COORDS);
			panelCard.add(lblCostIcon);
			lblCostText.setBounds(COST_COORDS);
			lblCostText.setFont(FONT_CORNER_VALUES);
			lblCostText.setForeground(card.deck == "Fate" ? COLOR_TEXT_FATE : COLOR_TEXT_VILLAIN);
			lblCostText.setHorizontalAlignment(SwingConstants.CENTER);
			lblCostText.setVerticalAlignment(SwingConstants.CENTER);
			panelCard.add(lblCostText);
		}
		// Strength
		JLabel lblStrengthIcon = new JLabel();
		JLabel lblStrengthText = new JLabel("<html><style>" + "html { height: " + STRENGTH_COORDS.height + "; width: "
				+ STRENGTH_COORDS.width + ";}" + "body { height: 100%; width: 100%; text-align: center; }"
				+ "</style><body>" + card.strength + "</body></html>");
		if (!card.strength.equals("")) {
			BufferedImage strengthTempl = DeckTemplate.getTemplateFile(templatesFolder, TemplateType.STRENGTH,
					card.deck);
			lblStrengthIcon.setIcon(new ImageIcon(strengthTempl));
			lblStrengthIcon.setBounds(CARD_COORDS);
			panelCard.add(lblStrengthIcon);
			lblStrengthText.setBounds(STRENGTH_COORDS);
			lblStrengthText.setFont(FONT_CORNER_VALUES);
			lblStrengthText.setForeground(card.deck == "Fate" ? COLOR_TEXT_FATE : COLOR_TEXT_VILLAIN);
			lblStrengthText.setHorizontalAlignment(SwingConstants.CENTER);
			lblStrengthText.setVerticalAlignment(SwingConstants.CENTER);
			panelCard.add(lblStrengthText);
		}
		// Top Right
		JLabel lblTRIcon = new JLabel();
		JLabel lblTRText = new JLabel("<html><style>" + "html { height: " + TR_COORDS.height + "; width: "
				+ TR_COORDS.width + ";}" + "body { height: 100%; width: 100%; text-align: center; }" + "</style><body>"
				+ card.topRight + "</body></html>");
		if (!card.topRight.equals("")) {
			BufferedImage topRightTempl = DeckTemplate.getTemplateFile(templatesFolder, TemplateType.TOP_RIGHT,
					card.deck);
			lblTRIcon.setIcon(new ImageIcon(topRightTempl));
			lblTRIcon.setBounds(CARD_COORDS);
			panelCard.add(lblTRIcon);
			lblTRText.setBounds(TR_COORDS);
			lblTRText.setFont(FONT_CORNER_VALUES);
			lblTRText.setForeground(card.deck == "Fate" ? COLOR_TEXT_FATE : COLOR_TEXT_VILLAIN);
			lblTRText.setHorizontalAlignment(SwingConstants.CENTER);
			lblTRText.setVerticalAlignment(SwingConstants.CENTER);
			panelCard.add(lblTRText);
		}
		// Bottom Right
		JLabel lblBRIcon = new JLabel();
		JLabel lblBRText = new JLabel("<html><style>" + "html { height: " + BR_COORDS.height + "; width: "
				+ BR_COORDS.width + ";}" + "body { height: 100%; width: 100%; text-align: center; }" + "</style><body>"
				+ card.bottomLeft + "</body></html>");
		if (!card.bottomLeft.equals("")) {
			BufferedImage topRightTempl = DeckTemplate.getTemplateFile(templatesFolder, TemplateType.BOTTOM_RIGHT,
					card.deck);
			lblBRIcon.setIcon(new ImageIcon(topRightTempl));
			lblBRIcon.setBounds(CARD_COORDS);
			panelCard.add(lblBRIcon);
			lblBRText.setBounds(BR_COORDS);
			lblBRText.setFont(FONT_CORNER_VALUES);
			lblBRText.setForeground(card.deck == "Fate" ? COLOR_TEXT_FATE : COLOR_TEXT_VILLAIN);
			lblBRText.setHorizontalAlignment(SwingConstants.CENTER);
			lblBRText.setVerticalAlignment(SwingConstants.CENTER);
			panelCard.add(lblBRText);
		}
		// Higher value = printed first (behind)
		// Lower value = printed last (in front)
		int compCount = panelCard.getComponentCount() - 1;
		panelCard.setComponentZOrder(lblArt, compCount--);
		panelCard.setComponentZOrder(lblDeckTempl, compCount--);
		panelCard.setComponentZOrder(lblName, compCount--);
		// panelCard.setComponentZOrder(lblAbility, compCount--);
		panelCard.setComponentZOrder(lblType, compCount--);
		if (!card.cost.equals("")) {
			panelCard.setComponentZOrder(lblCostIcon, compCount--);
			panelCard.setComponentZOrder(lblCostText, compCount--);
		}
		if (!card.strength.equals("")) {
			panelCard.setComponentZOrder(lblStrengthIcon, compCount--);
			panelCard.setComponentZOrder(lblStrengthText, compCount--);
		}
		if (!card.topRight.equals("")) {
			panelCard.setComponentZOrder(lblTRIcon, compCount--);
			panelCard.setComponentZOrder(lblTRText, compCount--);
		}
		if (!card.bottomLeft.equals("")) {
			panelCard.setComponentZOrder(lblBRIcon, compCount--);
			panelCard.setComponentZOrder(lblBRText, compCount--);
		}
		BufferedImage image = new BufferedImage(CARD_COORDS.width, CARD_COORDS.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D imageResult = image.createGraphics();
		panelCard.printAll(imageResult);
		// Ability 
		String htmlAbility = "<html>" + "<head><style>" + "html { width: " + ABILITY_COORDS.width + "; height: "
				+ ABILITY_COORDS.height + "; }" + "body { background-color: green; font: " + FONT_TEXT_MAX.getSize()
				+ " " + FONT_TEXT_MAX.getFamily() + ", sans-serif; color: "
				+ (card.deck == "Fate" ? COLOR_TEXT_FATE : COLOR_TEXT_VILLAIN) + "}" + "p {}"
				+ "</style></head><body><p style='text-align: center;'>"
				+ card.ability.trim().replace("   ", "\n").replace("\n", "<br>")
						.replace("Effect", "<span style=\"color: #7ac424\">Effect</span>")
						.replace("Ally", "<span style=\"color: #de0022\">Ally</span>")
						.replace("Item", "<span style=\"color: #45afe6\">Item</span>")
						.replace("Condition", "<span style=\"color: #d3568d\">Condition</span>")
						.replace("Hero", "<span style=\"color: #e68c0a\">Hero</span>")
				+ "</p></body></html>";
		// JLabel lblAbility = new JLabel(htmlAbility);
		// lblAbility.setDebugGraphicsOptions(DebugGraphics.LOG_OPTION);
		// lblAbility.setFont(FONT_TEXT_MAX);
		// lblAbility.setForeground(card.deck == "Fate" ? COLOR_TEXT_FATE : COLOR_TEXT_VILLAIN);
		// lblAbility.setBorder(new LineBorder(Color.RED, 2));
		// lblAbility.setBackground(Color.BLACK);
		// lblAbility.setPreferredSize(ABILITY_COORDS.getSize());
		// lblAbility.setSize(ABILITY_COORDS.getSize());
		// BufferedImage imageLblAbility = new BufferedImage(ABILITY_COORDS.width, ABILITY_COORDS.height,
		// BufferedImage.TYPE_INT_ARGB);
		// Graphics2D imageAbG = image.createGraphics();
		// imageAbG.translate(145, 1292);
		// lblAbility.printAll(imageAbG);
		//
		// imageG.drawImage(imageLblAbility, 0, 0, null);
		return image;
	}
	*/

	private BufferedImage generateImage(File templatesFolder, File artFolder, CardInfo card) throws IOException {
		Color textColor = (card.deck == "Fate" ? COLOR_TEXT_FATE : COLOR_TEXT_VILLAIN);
		String htmlAbility = card.ability.trim()
						.replace("Effect", "<span style=\"color: #7ac424\">Effect</span>")
						.replace("Ally", "<span style=\"color: #de0022\">Ally</span>")
						.replace("Item", "<span style=\"color: #45afe6\">Item</span>")
						.replace("Condition", "<span style=\"color: #d3568d\">Condition</span>")
						.replace("Hero", "<span style=\"color: #e68c0a\">Hero</span>");
		String htmlType = card.type.trim().replace("Effect", "<span style=\"color: #7ac424\">Effect</span>")
								.replace("Ally", "<span style=\"color: #de0022\">Ally</span>")
								.replace("Item", "<span style=\"color: #45afe6\">Item</span>")
								.replace("Condition", "<span style=\"color: #d3568d\">Condition</span>")
								.replace("Hero", "<span style=\"color: #e68c0a\">Hero</span>");

		BufferedImage resImage = new BufferedImage(CARD_COORDS.width, CARD_COORDS.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D resImageG = resImage.createGraphics();
		
		try {
			BufferedImage art = DeckTemplate.getArtFile(artFolder, card.name);
			resImageG.drawImage(art, null, 0, 0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			warnings.add("Error finding the art for " + card.name);
		} catch (Exception e) {
			e.printStackTrace();
			warnings.add("Error reading the art for " + card.name);
		}

		BufferedImage deck = DeckTemplate.getTemplateFile(templatesFolder, TemplateType.DECK, card.deck);
		resImageG.drawImage(deck, null, 0, 0);
		
		drawCenteredHTMLString(resImageG, htmlType, TYPE_COORDS, FONT_TYPE, textColor);
		
		if (!card.cost.equals("")) {
			BufferedImage costTempl = DeckTemplate.getTemplateFile(templatesFolder, TemplateType.COST, card.deck);
			resImageG.drawImage(costTempl, null, 0, 0);
			drawCenteredString(resImageG, card.cost, COST_COORDS, FONT_CORNER_VALUES, textColor);
		}
		
		drawCenteredHTMLParagraph(resImageG, htmlAbility, ABILITY_COORDS, FONT_TEXT_MAX, textColor, 1);
		
		return resImage;
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
	public void drawCenteredString(Graphics2D g, String text, Rectangle rect, Font font, Color textColor) {
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
	
	public void drawCenteredHTMLString(Graphics2D g, String htmlText, Rectangle rect, Font font, Color textColor) {
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
	public void drawCenteredHTMLParagraph(Graphics2D graphics, String htmlText, Rectangle rect, Font font, Color textColor, int resizeStep) {
		htmlText = htmlText.replace("   ", "\n").replace("\n", "<br>").replace("<br>", "</p><p>");
		JLabel label = new JLabel("<html><style>html{width:" + rect.width + ";}p{ border: 1px solid white;}</style><body style='text-align: center'><div><p>"+htmlText);
		
		
//		int numLines = 1;
//		char[] textArray = htmlText.toCharArray();
//		for (int i = 0; i < textArray.length; i++) {
//			System.out.println(textArray[i]);
//			if ((i>3 && textArray[i] == textArray[i-1] && textArray[i] == textArray[i-2] && textArray[i] == ' ')
//					|| (i > 4 && textArray[i] == '>' && textArray[i-1] == 'r' && textArray[i-2] == 'b' && textArray[i-3] == '<')
//					|| (textArray[i] == Character.LINE_SEPARATOR)) {
//				numLines++;
//			}
//		}
		
		//TODO: Calculate lines
		Font fontFinal = font;
		FontMetrics fontMetrics = graphics.getFontMetrics(fontFinal);
		
		int numLines = 1;
		char[] textArray = htmlText.toCharArray();
		int lastLineJump = 0;
		boolean insideTag = false;
		int tagCharacters = 0;
		for (int i = 0; i < textArray.length; i++) {
			//TODO: right now characters in <span> are counted, I'd want them absolutely discarded from all of this. Perhaps just doing another loop...
			if (textArray[i] == '<'){
				insideTag = true;
				tagCharacters++;
			}
			String subString = htmlText.substring(lastLineJump, i);
			System.out.println("Checking line: \""+subString+"\"");
			if (subString.endsWith("</p><p>")) {
				System.out.println("Line jump in "+i+": \""+ textArray[i]+"\"");
		
				int stringWidth = fontMetrics.stringWidth(subString.substring(0, subString.length()-7));
				int addNumLines = stringWidth / ABILITY_COORDS.width;
				System.out.println("addLine in between "+addNumLines);
				if (stringWidth % ABILITY_COORDS.width>0){
					System.out.println("Added +1 because "+stringWidth +"%"+ABILITY_COORDS.width+" = "+(stringWidth % ABILITY_COORDS.width));
					addNumLines++;
				}
				numLines+=addNumLines;
				lastLineJump = i;
				System.out.println("Line: \""+subString.substring(0, subString.length()-7)+"\"\n   with width: "+stringWidth+"\n   and num lines:"+ addNumLines);
			} else if (i == textArray.length-1){
				System.out.println("Last line in "+i+": \""+ textArray[i]+"\"");
		
				int stringWidth = fontMetrics.stringWidth(subString);
				int addNumLines = stringWidth / ABILITY_COORDS.width;
				if (stringWidth % ABILITY_COORDS.width>0){
					addNumLines++;
				}
				numLines+=addNumLines;
				lastLineJump = i;
				System.out.println("Line: \""+subString+"\"\n   with width: "+stringWidth+"\n   and num lines:"+ addNumLines);
			}
		}
//		int numLines = stringWidth / ABILITY_COORDS.width;
//		if (stringWidth % ABILITY_COORDS.width>0){
//			numLines++;
//		}
		
		System.out.println("Rectangle width: "+ABILITY_COORDS.width);
		System.out.println("Bounds height: "+ABILITY_COORDS.height +" for each of the "+numLines+" lines");
		System.out.println("Calculated height: "+(fontMetrics.getHeight()*numLines));
		
		label.setBounds(rect);
		label.setLocation(rect.getLocation());
		label.setFont(font);
		label.setForeground(textColor);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		
		BufferedImage type = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
		label.paint(type.getGraphics());
		graphics.drawImage(type, null, rect.x, rect.y);
	}

	/**
	 * Changes all pixels of a specific color in a BufferedImage to alpha.
	 * 
	 * @param sourceImage
	 * @param chromaColor
	 * @return The resulting image
	 */
	public static BufferedImage intArgbColorToIntArgbAlpha(BufferedImage sourceImage, Color chromaColor) {
		BufferedImage targetImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		WritableRaster targetRaster = targetImage.getRaster();
		WritableRaster sourceRaster = sourceImage.getRaster();

		for (int row = 0; row < sourceImage.getHeight(); row++) {

			int[] rgbaTarget = new int[4 * sourceImage.getWidth()];
			int[] rgbaSource = new int[4 * sourceImage.getWidth()];

			// Get the next row of pixels
			sourceRaster.getPixels(0, row, sourceImage.getWidth(), 1, rgbaSource);

			for (int i = 0, j = 0; i < rgbaSource.length; i += 4, j += 4) {
				// if (origColor.equals(new Color(rgb[i], rgb[i + 1], rgb[i + 2]))) {
				if (row < 3 || row > sourceImage.getWidth() - 3 // We make transparent the border (top/bottom)
						|| i < 12 || i > sourceImage.getWidth() - 12 // We make transparent the border (left/right)
						|| rgbaSource[i] == 0 && rgbaSource[i + 1] > 0 && rgbaSource[i + 2] == 0) {
					// System.out.println("!!!!!!!!!!!!!!!! Confirmed transparency");
					// System.out.println("Orig color: " + origColor);
					// System.out.println("pìxel color: " + new Color(rgbaSource[i], rgbaSource[i + 1], rgbaSource[i + 2]));
					// System.out.println("pìxel color2: " + rgbaSource[i] + ", " + rgbaSource[i + 1] + ", " + rgbaSource[i + 2]);
					// If it's the same make it transparent
					rgbaTarget[j] = 0;
					rgbaTarget[j + 1] = 0;
					rgbaTarget[j + 2] = 0;
					rgbaTarget[j + 3] = 0;
				} else {
					// System.out.println(" Confirmed NO transparency: ");
					// System.out.println(" Orig color: " + origColor);
					// System.out.println(" pìxel color: " + new Color(rgbaSource[i], rgbaSource[i + 1], rgbaSource[i + 2]));
					rgbaTarget[j] = rgbaSource[i];
					rgbaTarget[j + 1] = rgbaSource[i + 1];
					rgbaTarget[j + 2] = rgbaSource[i + 2];
					// Make it opaque
					// rgba[j + 3] = 255;
					rgbaTarget[j + 3] = rgbaSource[i + 3];
				}
			}
			// Write the line
			targetRaster.setPixels(0, row, sourceImage.getWidth(), 1, rgbaTarget);
		}
		return targetImage;
	}

	public static void main(String[] args) throws Exception {
		CardGenerator cGen = new CardGenerator();
		cGen.warnings = new ArrayList<>(3);

		CardInfo ci = new CardInfo(null);
		ci.name = "Riptide Rex";
		ci.deck = "Villain";
		ci.extraDeck = "";

		ci.cost = "32";
		ci.strength = "3";
//		ci.ability = "When Riptide Rex is played, please take one +1 Strength Token and put it on yourself for being so good at this game. Then, take a -1 Strength Token and put it on an opponent because nobody is left alive after trying to conquer your domain.";
		ci.ability = "Riptide Rex may be used to defeat a Hero at an adjacent location if he is at The Dreadway's location.";
//		ci.ability = "When Riptide Rex do the good.";
//		ci.ability = "You won   gg";

		ci.activateAbility = "";
		ci.activateCost = "";

		ci.topRight = "?";
		ci.bottomLeft = "?";
		ci.action = "";
		ci.credits = "Riot Games";

		ci.type = "Ally";
		ci.copies = 1;
		ci.desc = "desc";
		ci.row = 22;

		BufferedImage bi = cGen.generateImage(new File("-Layout"), new File("-Images"), ci);

		File testFile = new File("test " + ci.name + ".jpg");
		Util.writeJpgImage(bi, testFile, 1);
		Desktop.getDesktop().open(testFile);
		
		System.out.println("Warnings:");
		System.out.println(cGen.warnings.toString());
	}
}
