package es.cristichi.cardphotocopier;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.jopendocument.dom.spreadsheet.Cell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import es.cristichi.cardphotocopier.excep.ConfigValueNotFound;
import es.cristichi.cardphotocopier.obj.CardComparator;
import es.cristichi.cardphotocopier.obj.CardInfo;
import es.cristichi.cardphotocopier.obj.Configuration;
import es.cristichi.cardphotocopier.obj.Range;
import es.cristichi.cardphotocopier.obj.ODS.Column;
import es.cristichi.cardphotocopier.obj.ODS.Structure;

/**
 * Feel free to modify the code for yourself and/or propose modifications and
 * improvements. Have an evil day!
 * 
 * @author Cristichi#5193
 */
public class CardPhotocopier {
	private static String VERSION = "v2.5.0";
	private static String NAME = "Villainous Card Photocopier " + VERSION;

	private static String CONFIG_TXT = "config.yml";
	private static String DESCRIPTIONS_JSON = "CardPhotocopier descriptions.json";
	private static String ERROR_LOG = "CardPhotocopier error.log";
	private static String ERROR_DESC_LOG = "CardPhotocopier descriptions error.log";

	// TODO: Making this configurable
	private static Dimension CARD_SIZE = new Dimension(620, 880);
	private static HashMap<Range, Dimension> DECK_SIZES;
	static {
		DECK_SIZES = new HashMap<>(18);
		DECK_SIZES.put(new Range(13, 15), new Dimension(5, 3));
		DECK_SIZES.put(new Range(26, 30), new Dimension(6, 5));
		DECK_SIZES.put(new Range(1, 1), new Dimension(2, 2));
		DECK_SIZES.put(new Range(2, 2), new Dimension(2, 2));
		DECK_SIZES.put(new Range(3, 3), new Dimension(2, 2));
		DECK_SIZES.put(new Range(4, 4), new Dimension(2, 2));
		DECK_SIZES.put(new Range(5, 6), new Dimension(3, 2));
		DECK_SIZES.put(new Range(7, 8), new Dimension(4, 2));
		DECK_SIZES.put(new Range(9, 9), new Dimension(3, 3));
		DECK_SIZES.put(new Range(10, 12), new Dimension(4, 3));
		DECK_SIZES.put(new Range(16, 20), new Dimension(5, 4));
		DECK_SIZES.put(new Range(21, 24), new Dimension(6, 4));
		DECK_SIZES.put(new Range(25, 25), new Dimension(5, 5));
		DECK_SIZES.put(new Range(31, 35), new Dimension(7, 5));
		DECK_SIZES.put(new Range(36, 36), new Dimension(6, 6));
		DECK_SIZES.put(new Range(37, 42), new Dimension(7, 6));
		DECK_SIZES.put(new Range(43, 48), new Dimension(7, 6));
		DECK_SIZES.put(new Range(0, 0), new Dimension(2, 2));
	}

	public static String CONFIG_GENERATOR_VERSION = "cardGeneratorVersion", CONFIG_DOC = "cardsInfoOds",
			CONFIG_CARD_IMAGES = "generatedCardsFolder", CONFIG_RESULTS = "resultsFolder",
			CONFIG_AUTOCLOSE = "autoclose", CONFIG_FATE_NAME = "fateDeckName", CONFIG_VILLAIN_NAME = "villainDeckName",
			CONFIG_FATE_QUANTITY = "fateDeckQuantity", CONFIG_VILLAIN_QUANTITY = "villainDeckQuantity",
			CONFIG_EMPTY_ROWS_TO_END = "maxEmptyRowsToEnd", CONFIG_TYPE_ORDER = "cardTypeOrder",
			CONFIG_IMAGE_QUALITY = "imageQuality", CONFIG_GENERATE_JSON = "generateJsonDescriptions",
			CONFIG_COPY_JSON = "copyJsonToClipboard", CONFIG_TYPE_IN_JSON = "addTypeToNameInJson",
			CONFIG_JSON_NUM_COPIES = "jsonNumberOfCopiesInDesc", CONFIG_EXTRA_DECKS = "extraDecks";
	public static String INFO_GENERATOR_VERSION = "The version of FailureFactory's Villainous Card Generator, as a number. "
			+ "For example, if you are running V33.2 you have to put here \"33.2\". "
			+ "This is improtant because the columns of the .ods file are differnt for version V35 onwards.",
			INFO_DOC = "The path to the .ods file where you have your cards' info.",
			INFO_CARD_IMAGES = "Folder where all the generated images of your Villain's cards are. It must not contain other Villains' cards",
			INFO_RESULTS = "Where you want the Villain/Fate deck images to be created. I also recommend just setting it to \".\" so that they are generated in the same folder as the .jar file.",
			INFO_AUTOCLOSE = "True if you want the info window to be automatically closed if everything was ok and expected. If anything weird happens, it won't autoclose.",
			INFO_FATE_NAME = "The name of the Fate deck's image file to be generated.",
			INFO_VILLAIN_NAME = "The name of the main deck's image file to be generated.",
			INFO_FATE_QUANTITY = "The number of cards that should be expected for this Villain to have in their Fate deck. Useful in case you didn't count the cards well.",
			INFO_VILLAIN_QUANTITY = "The number of cards that should be expected for this Villain to have in their main deck. Useful in case you didn't count the cards well.",
			INFO_EMPTY_ROWS_TO_END = "So the way this tool knows when to stop reading the .ods document is when it encounters enough empty lines. This controls the number of empty lines. "
					+ "Setting it lower will make the tool finish considerably faster. I recommend testing it with your .ods document to see the lowest you can set it. "
					+ "If set too low, it finds no cards.",
			INFO_TYPE_ORDER = "Here you can alter the order depending the types. Cards of the same type will be ordered by name respective to each other, and cards of an unlisted "
					+ "type will be last by name. To make use of the default order recommended by me, write something like "
					+ "\"" + CONFIG_TYPE_ORDER
					+ ": Hero, Condition, Effect, Ally, Item\" (without quotation marks). To make it order by name, remove this value entirely.",
			INFO_IMAGE_QUALITY = "The quality of the resulting images. Put \"1\" for the best quality but large image, "
					+ "\"0\" for the poorest quality (horrible trust me) and smallest image possible."
					+ " Recommended is \"0.9\" so keep it that way unless you need the file to be even smaller.",
			INFO_GENERATE_JSON = "If true, apart from generating the images, it will take the N column of the "
					+ ".ods document of each card and create a JSON that the Card Descriptions Loader can read "
					+ "in TTS in order to apply each name and description to each card. To use this JSON file, "
					+ "you need my Card Descriptions Loader, that you can find in this link "
					+ "https://steamcommunity.com/sharedfiles/filedetails/?id=2899195933",
			INFO_COPY_JSON = "If true, and if the JSON file is generated, it will be copied to the clipboard as well.",
			INFO_TYPE_IN_JSON = "If true, and if the JSON file is generated, the name of the cards will include the type of the card like CARD NAME [Ally]. "
					+ "Useful if during gameplay it is convenient to be able to search by type.",
			INFO_JSON_NUM_COPIES = "With this option, to every description a new line will be added that informs about the number of copies of that card in the deck. "
					+ "Values: \"true\", \"Villain\", \"Fate\", \"false\"",
			INFO_EXTRA_DECKS = "Create additional decks with this option. Just write here the list of extra decks, separated by comma if there is more than one. Use those values on the \"O\" column in the document to get them sorted into the appropriate extra deck.";

	private static ArrayList<String> warnings;

	private static JFrame window;
	private static JLabel label;

	public static void main(String[] args) {
		warnings = new ArrayList<>(10);
		try {
			// We first create a new window so we can tell the user how things are going.
			window = new JFrame(NAME);
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			label = new JLabel("Starting");
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setBorder(new EmptyBorder(1, 5, 1, 5));
			window.add(label);
			window.setMinimumSize(new Dimension(500, 200));
			window.setMaximumSize(new Dimension(800, 500));
			window.setPreferredSize(new Dimension(500, 200));
			window.setLocationRelativeTo(null);
			window.setAlwaysOnTop(true);
			window.setVisible(true);
			generate();
			label.setText("Done. Close this window to finish.");
		} catch (Exception e) {
			// If anything happens that makes the proccess unable to continue (things are
			// missing, wrong values in the .ods file, etc) then we just stop and tell
			// the user what happened. We also print in the console and a file just in case.
			System.err.println("Ended badly with error. Sadge.");
			e.printStackTrace();
			label.setText("<html>" + e.getLocalizedMessage() + "</html>");
			// window.pack();
			window.setLocationRelativeTo(null);
			try {
				PrintStream ps = new PrintStream(ERROR_LOG);
				ps.println("Error in version " + VERSION);
				e.printStackTrace(ps);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				label.setText("<html><div>" + e.getLocalizedMessage()
						+ "</div><div style=\"margin-top: 5px;\">An error occurred that prevented the error log to be created to a log file. "
						+ "Please screenshot the error now and save it if you need further assistance from Cristichi#5193.</div></html>");
			}
		}
		// After we are done, no matter if there was an error or not, we are going to
		// show any item in "warnings" so the user can fix whatever weird thing we found
		// (cards missing information, too many copies or not enough, etc)
		if (warnings.size() > 0) {
			window.remove(label);
			window.setLayout(new GridLayout(warnings.size() + 1, 1));
			JLabel warningTitle = new JLabel("Process completed without errors but with some weird notes:");
			warningTitle.setBorder(new EmptyBorder(2, 5, 2, 5));
			window.add(warningTitle);
			for (String warning : warnings) {
				JLabel lbl = new JLabel("<html>" + warning + "</html>");
				lbl.setBorder(new EmptyBorder(1, 5, 1, 5));
				window.add(lbl);
			}
			// window.pack();
			window.setLocationRelativeTo(null);
		}
	}

	public static void generate() throws Exception {
		label.setText("Reading config file.");

		Configuration config = new Configuration(CONFIG_TXT,
				NAME + " configuration.\n For help, contact Cristichi#5193 on Discord.");
		if (!config.exists()) {
			config.setValue(CONFIG_GENERATOR_VERSION, "33.2", INFO_GENERATOR_VERSION);
			config.setValue(CONFIG_CARD_IMAGES,
					"./Villainous Card Generator V35/Villainous Card Generator_Data/-Exports", INFO_CARD_IMAGES);
			config.setValue(CONFIG_DOC,
					"./Villainous Card Generator V35/Villainous Card Generator_Data/-TextFiles/Villainous Template.ods",
					INFO_DOC);
			config.setValue(CONFIG_RESULTS, "Results", INFO_RESULTS);
			config.setValue(CONFIG_AUTOCLOSE, true, INFO_AUTOCLOSE);
			config.setValue(CONFIG_VILLAIN_NAME, "Villain deck", INFO_VILLAIN_NAME);
			config.setValue(CONFIG_FATE_NAME, "Fate deck", INFO_FATE_NAME);
			config.setValue(CONFIG_VILLAIN_QUANTITY, 30, INFO_VILLAIN_QUANTITY);
			config.setValue(CONFIG_FATE_QUANTITY, 15, INFO_FATE_QUANTITY);
			config.setValue(CONFIG_EMPTY_ROWS_TO_END, 20, INFO_EMPTY_ROWS_TO_END);
			config.setValue(CONFIG_TYPE_ORDER, "Hero, Condition, Effect, Ally, Item", INFO_TYPE_ORDER);
			config.setValue(CONFIG_IMAGE_QUALITY, "0.9", INFO_IMAGE_QUALITY);
			config.setValue(CONFIG_GENERATE_JSON, "false", INFO_GENERATE_JSON);
			config.setValue(CONFIG_COPY_JSON, "true", INFO_COPY_JSON);
			config.setValue(CONFIG_TYPE_IN_JSON, "false", INFO_TYPE_IN_JSON);
			config.setValue(CONFIG_JSON_NUM_COPIES, "false", INFO_JSON_NUM_COPIES);
			config.setValue(CONFIG_EXTRA_DECKS, "", INFO_EXTRA_DECKS);

			config.saveConfig();

			// System.out.println(config.getAbsolutePath());

			window.setMinimumSize(new Dimension(800, 200));
			// window.pack();
			window.setLocationRelativeTo(null);
			window.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					super.windowClosing(e);
					if (Desktop.isDesktopSupported()) {
						Desktop desktop = Desktop.getDesktop();
						try {
							desktop.open(config.getParentFile());
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			});
			label.setText("Configuration file (" + CONFIG_TXT
					+ ") not found. We generated one for you, please close this window and edit it accordingly.");
			throw new FileNotFoundException("Configuration file (" + CONFIG_TXT
					+ ") not found. We generated one for you, please close this window and edit it accordingly.");
		}

		config.reloadConfigFromFile();
		config.setInfo(CONFIG_GENERATOR_VERSION, INFO_GENERATOR_VERSION);
		config.setInfo(CONFIG_CARD_IMAGES, INFO_CARD_IMAGES);
		config.setInfo(CONFIG_DOC, INFO_DOC);
		config.setInfo(CONFIG_RESULTS, INFO_RESULTS);
		config.setInfo(CONFIG_AUTOCLOSE, INFO_AUTOCLOSE);
		config.setInfo(CONFIG_VILLAIN_NAME, INFO_VILLAIN_NAME);
		config.setInfo(CONFIG_FATE_NAME, INFO_FATE_NAME);
		config.setInfo(CONFIG_VILLAIN_QUANTITY, INFO_VILLAIN_QUANTITY);
		config.setInfo(CONFIG_FATE_QUANTITY, INFO_FATE_QUANTITY);
		config.setInfo(CONFIG_EMPTY_ROWS_TO_END, INFO_EMPTY_ROWS_TO_END);
		config.setInfo(CONFIG_TYPE_ORDER, INFO_TYPE_ORDER);
		config.setInfo(CONFIG_IMAGE_QUALITY, INFO_IMAGE_QUALITY);
		config.setInfo(CONFIG_GENERATE_JSON, INFO_GENERATE_JSON);
		config.setInfo(CONFIG_COPY_JSON, INFO_COPY_JSON);
		config.setInfo(CONFIG_TYPE_IN_JSON, INFO_TYPE_IN_JSON);
		config.setInfo(CONFIG_JSON_NUM_COPIES, INFO_JSON_NUM_COPIES);
		config.saveConfig();

		boolean autoclose = config.getBoolean(CONFIG_AUTOCLOSE);

		File imagesFolder = new File(config.getString(CONFIG_CARD_IMAGES));
		File resultsFolder = new File(config.getString(CONFIG_RESULTS));
		File documentFile = new File(config.getString(CONFIG_DOC));

		if (!imagesFolder.exists()) {
			window.setMinimumSize(new Dimension(800, 200));
			// window.pack();
			window.setLocationRelativeTo(null);
			label.setText("The folder where the already existing images for the cards are supposed to be ("
					+ imagesFolder.getAbsolutePath() + ") was not found. We need that one.");
			throw new FileNotFoundException(
					"The folder where the already existing images for the cards are supposed to be ("
							+ imagesFolder.getAbsolutePath() + ") was not found. Please edit the config file.");
		}
		if (!documentFile.exists()) {
			window.setMinimumSize(new Dimension(800, 200));
			// window.pack();
			window.setLocationRelativeTo(null);
			label.setText("The .ods document with the information for each card (" + documentFile.getAbsolutePath()
					+ ") was not found. We need that one.");
			throw new FileNotFoundException("The .ods document with the information for each card ("
					+ documentFile.getAbsolutePath() + ") was not found. Please edit the config file.");
		}
		
		if (!config.contains(CONFIG_GENERATOR_VERSION)) {
			config.setValue(CONFIG_GENERATOR_VERSION, "", INFO_GENERATOR_VERSION);
			config.saveConfig();
			throw new ConfigValueNotFound(
					"You need to specify the version of the Card Generator that you are using in order to determine the layout of the .ods file.");
		}

		Structure odsStructure = new Structure(config.getDouble(CONFIG_GENERATOR_VERSION));

		label.setText("Reading card data from " + documentFile.getName() + ".");

		SpreadSheet sheetDoc = SpreadSheet.createFromFile(documentFile);
		Sheet sheet = sheetDoc.getFirstSheet();

		// First we read every .png, .jpg and .jpeg file.
		HashMap<String, CardInfo> cardsInfo = new HashMap<>(60);

		for (File cardFile : imagesFolder.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				name = name.toLowerCase();
				return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
			}
		})) {

			String name = cardFile.getName().substring(0,
					cardFile.getName().length() - (cardFile.getName().endsWith(".jpeg") ? 5 : 4));
			if (!name.isEmpty()) {
				label.setText("Loading " + name + "'s image data from it's file.");
				// System.out.println("Loading " + name + "'s image data from it's file.");
				CardInfo info = new CardInfo(load(cardFile));
				if (info.imageData == null) {
					System.err.println("Image " + cardFile + " could not be loaded.");
					warnings.add("Image \"" + cardFile.getName() + "\" could not be loaded.");
				} else {
					cardsInfo.put(name, info);
				}
			}
		}

		String extraDecksStr = config.getString(CONFIG_EXTRA_DECKS, "");
		final String[] extraDecks = extraDecksStr.length() == 0 ? new String[0] : extraDecksStr.split(",");
		for (int i = 0; i < extraDecks.length; i++) {
			extraDecks[i] = extraDecks[i].trim();
		}
		int[] extraDecksCount = new int[extraDecks.length];

		ArrayList<CardInfo> usefulCards = new ArrayList<>(
				config.getInt(CONFIG_VILLAIN_QUANTITY) + config.getInt(CONFIG_FATE_QUANTITY));

		int copiesToV = 0, copiesToF = 0;
		int xV = 0, yV = 0;
		int xF = 0, yF = 0;
		int[] xExtras = new int[extraDecks.length];
		int[] yExtras = new int[extraDecks.length];

		boolean forceFate = false;
		int done = 0;
		if (!config.contains(CONFIG_EMPTY_ROWS_TO_END)) {
			config.setValue(CONFIG_EMPTY_ROWS_TO_END, 20, INFO_EMPTY_ROWS_TO_END);
		}
		if (!config.contains(CONFIG_EXTRA_DECKS)) {
			config.setValue(CONFIG_EXTRA_DECKS, "", INFO_EXTRA_DECKS);
		}
		Arrays.fill(extraDecksCount, 0);
		int doneLimit = config.getInt(CONFIG_EMPTY_ROWS_TO_END, 20);

		// We are going to look into each row in the .ods and check if it's a card that
		// exists withing the images folder and draw it into it's corresponding deck.
		for (int row = 1; done <= doneLimit; row++) {
			// Cell<SpreadSheet> cellName = sheet.getCellAt("A" + row);
			Cell<SpreadSheet> cellCopiesCount = sheet.getCellAt(odsStructure.get(Column.COPIES_COUNT) + row);

			if (cellCopiesCount.getTextValue().trim().equalsIgnoreCase("#stop")) {
				break;
			}
			try {
				// We get all the data. The unused one commented in case I want to do something with it one day.

				Cell<SpreadSheet> cellName = sheet.getCellAt(odsStructure.get(Column.NAME) + row);
				// Cell<SpreadSheet> cellCost = sheet.getCellAt(odsStructure.get(Column.COST) + row);
				// Cell<SpreadSheet> cellStrengh = sheet.getCellAt(odsStructure.get(Column.STRENGTH) + row);
				// Cell<SpreadSheet> cellEffect = sheet.getCellAt(odsStructure.get(Column.EFFECT) + row);
				Cell<SpreadSheet> cellType = sheet.getCellAt(odsStructure.get(Column.TYPE) + row);
				// Cell<SpreadSheet> cellActEffect = sheet.getCellAt(odsStructure.get(Column.ACTIVATE_EFFECT) + row);
				// Cell<SpreadSheet> cellActCost = sheet.getCellAt(odsStructure.get(Column.ACTIVATE_COST) + row);
				// Cell<SpreadSheet> cellTopRight = sheet.getCellAt(odsStructure.get(Column.TOP_RIGHT) + row);
				// Cell<SpreadSheet> cellBotRight = sheet.getCellAt(odsStructure.get(Column.BOTTOM_RIGHT) + row);
				Cell<SpreadSheet> cellDeck = sheet.getCellAt(odsStructure.get(Column.DECK) + row);
				// Cell<SpreadSheet> cellAction = sheet.getCellAt(odsStructure.get(Column.ACTION_SYMBOL) + row);
				// Cell<SpreadSheet> cellAutoLayout = sheet.getCellAt(odsStructure.get(Column.AUTO_LAYOUT) + row);
				Cell<SpreadSheet> cellDescription = sheet.getCellAt(odsStructure.get(Column.DESCRIPTION) + row);
				Cell<SpreadSheet> cellExtraDeck = sheet.getCellAt(odsStructure.get(Column.EXTRA_DECK) + row);
				// Cell<SpreadSheet> cellCredits = sheet.getCellAt(odsStructure.get(Column.CREDITS) + row);

				// If we find too many empty lines we are going to call it a day, because it
				// might mean we are at the end but there are tons of empty lines.
				// Well, not neccesarily empty lines, but if there is nothing in column B then
				// those are not cards anyway.
				if (cellName.isEmpty()) {
					done++;
				} else {
					// We want "done" to count the CONSECUTIVE empty lines, so if we find a proper
					// card we are going to reset it why not.
					done = 0;
					String cardName = cellName.getTextValue().replaceAll("[\\\\/:*?\"<>|]", "");
					if (cardsInfo.containsKey(cardName)) {
						// So this card in the ODS document is also in the exports folder. Great!
						if (cellCopiesCount.isEmpty() || cellType.isEmpty() || cellDeck.isEmpty()) {
							// If we are missing important data and it's not because everything is empty, we
							// are going to warn the user so they can check if the .ods document is properly
							// filled.
							warnings.add("Detected error in a possible card \"" + cardName + "\"."
									+ (cellCopiesCount.getTextValue().trim().isEmpty()
											? " Number of copies (Column "+odsStructure.get(Column.COPIES_COUNT)+") is not filled."
											: "")
									+ (cellType.getTextValue().trim().isEmpty()
											? " Type (Column "+odsStructure.get(Column.TYPE)+") is not filled."
											: "")
									+ (cellDeck.getTextValue().trim().isEmpty()
											? " Deck (Column "+odsStructure.get(Column.DECK)+") is not filled."
											: "")
									);
							System.err.println("Error reading: " + row + " (Card " + cardName + " not proper)");
						} else {
							// We add the information found about this card
							CardInfo ci = cardsInfo.get(cardName);
							ci.name = cardName;
							ci.type = cellType.getTextValue();
							ci.copies = Integer.parseInt(cellCopiesCount.getTextValue());
							ci.desc = cellDescription.getTextValue();
							// System.out.println("Loading .ods data for " + ci.name + ": x" + ci.copies + ".");

							if (!cellExtraDeck.getTextValue().trim().equals("")) {
								for (int i = 0; i < extraDecks.length; i++) {
									if (cellExtraDeck.getTextValue().equalsIgnoreCase(extraDecks[i])) {
										ci.deck = extraDecks[i];
										extraDecksCount[i] += ci.copies;
										usefulCards.add(ci);
										break;
									}
								}
							}
							if (ci.deck == null) {
								if ((cellDeck.getTextValue().equals("Villain") || cellDeck.getTextValue().equals("0"))
										&& !forceFate) {
									ci.deck = "0";
									copiesToV += ci.copies;
									usefulCards.add(ci);
								} else if (cellDeck.getTextValue().equals("Fate") || cellDeck.getTextValue().equals("1")
										|| forceFate) {
									ci.deck = "1";
									copiesToF += ci.copies;
									usefulCards.add(ci);
								} else {
									// System.out.println("Card " + cardName + " is from another deck.");
								}
							}
						}

						cardsInfo.remove(cardName);
					}
				}
			} catch (IllegalArgumentException e) {
				// This probably means that some columns are combined, so we know it's not a
				// card anyway.
				// System.err.println(e.getLocalizedMessage());
				// System.err.println("Line: " + A.getTextValue());

				// If the column A contains "- Fate -" and there are combined cells somewhere
				// here, it's Fate forcing time. This allows villains that need to generate Fate
				// cards as Villain cards with a different layout to still tell my tool which
				// cards are Fate. Read the Usage guide to know how.
				if (cellCopiesCount.getTextValue().contains("- Fate -")) {
					forceFate = true;
					// System.out.println("Detected \"- Fate -\". Forcing fate from now on");
				} else if (forceFate) {
					forceFate = false;
					// System.out.println("Interpreted as end of force Fate. No longer forcing fate");
				}
			}
		}

		int villainExpectedSize = config.getInt(CONFIG_VILLAIN_QUANTITY);
		int fateExpectedSize = config.getInt(CONFIG_FATE_QUANTITY);

		if (copiesToV == 0 && copiesToV != villainExpectedSize && copiesToF == 0 && copiesToF != fateExpectedSize) {
			throw new IllegalArgumentException(
					"Both your Villain and Fate decks have 0 cards! Check it please." + (doneLimit < 20
							? " You might have to increase the " + CONFIG_EMPTY_ROWS_TO_END + " (its current value is "
									+ doneLimit + ")"
							: ""));
		} else if (copiesToV == 0 && copiesToV != villainExpectedSize) {
			throw new IllegalArgumentException("Your Villain deck has 0 cards! Check it please.");
		} else if (copiesToF == 0 && copiesToF != fateExpectedSize) {
			throw new IllegalArgumentException("Your Fate deck has 0 cards! Check it please.");
		}

		// We get the proper dimensions for the final image, depending on the number of cards.
		Dimension gridV = getGrid(copiesToV);
		Dimension gridF = getGrid(copiesToF);

		// This is the data of the two images. We now create it empty (black) and we'll draw each card over it.
		BufferedImage resultImageV = new BufferedImage(CARD_SIZE.width * gridV.width, CARD_SIZE.height * gridV.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics gV = resultImageV.getGraphics();
		BufferedImage resultImageF = new BufferedImage(CARD_SIZE.width * gridF.width, CARD_SIZE.height * gridF.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics gF = resultImageF.getGraphics();

		Dimension[] gridExtras = new Dimension[extraDecks.length];
		BufferedImage[] resultImageExtras = new BufferedImage[extraDecks.length];
		Graphics[] gExtras = new Graphics[extraDecks.length];
		for (int i = 0; i < extraDecks.length; i++) {
			gridExtras[i] = getGrid(extraDecksCount[i]);
			resultImageExtras[i] = new BufferedImage(CARD_SIZE.width * gridExtras[i].width,
					CARD_SIZE.height * gridExtras[i].height, BufferedImage.TYPE_INT_RGB);
			gExtras[i] = resultImageExtras[i].getGraphics();
		}

		cardsInfo.clear();

		// We are going to read the order the user wants and sort the cards by that order.
		if (!config.contains(CONFIG_TYPE_ORDER)) {
			config.setInfo(CONFIG_TYPE_ORDER, INFO_TYPE_ORDER);
			config.setValue(CONFIG_TYPE_ORDER, "ignore_type", INFO_TYPE_ORDER);
		}
		String order = config.getString(CONFIG_TYPE_ORDER, "ignore type");
		String[] orderSplit = order.split(",");
		for (int i = 0; i < orderSplit.length; i++) {
			orderSplit[i] = orderSplit[i].trim();
		}
		usefulCards.sort(new CardComparator(orderSplit));

		// This whole hocus pocus is for the descriptions feature to later use
		// with the TTS Descrpton Loader (tool by me, not public right now).
		Semaphore semDesc = new Semaphore(0);
		if (config.getBoolean(CONFIG_GENERATE_JSON, false)) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					// System.out.println(" (Thread) Generating JSON file.");

					if (!config.contains(CONFIG_TYPE_IN_JSON)) {
						config.setInfo(CONFIG_TYPE_IN_JSON, INFO_TYPE_IN_JSON);
						config.setValue(CONFIG_TYPE_IN_JSON, false, INFO_TYPE_IN_JSON);
					}
					boolean includeType = config.getBoolean(CONFIG_TYPE_IN_JSON, false);

					JSONObject jsonV = new JSONObject();
					jsonV.put("name", config.getString(CONFIG_VILLAIN_NAME, "Villain Deck"));
					JSONArray cardsV = new JSONArray();

					JSONObject jsonF = new JSONObject();
					jsonF.put("name", config.getString(CONFIG_FATE_NAME, "Fate Deck"));
					JSONArray cardsF = new JSONArray();

					JSONObject[] jsonExtras = new JSONObject[extraDecks.length];
					JSONArray[] cardsExtras = new JSONArray[extraDecks.length];
					for (int i = 0; i < jsonExtras.length; i++) {
						jsonExtras[i] = new JSONObject();
						jsonExtras[i].put("name", extraDecks[i].toLowerCase());
						cardsExtras[i] = new JSONArray();
					}

					int contV = 0;
					int contF = 0;
					int[] countExtras = new int[extraDecks.length];
					Arrays.fill(countExtras, 0);

					for (CardInfo ci : usefulCards) {
						String name = ci.name.replace("   ", " ").replace("\n", " ").trim();

						boolean stopAdding = false;
						char startStop = '[';
						char endStop = ']';
						char[] nameArray = name.toCharArray();
						name = "";
						for (int i = 0; i < nameArray.length; i++) {
							if (nameArray[i] == startStop) {
								stopAdding = true;
							}
							if (!stopAdding)
								name += Character.toString(nameArray[i]);
							if (nameArray[i] == endStop) {
								stopAdding = false;
							}
						}
						name = name.trim();
						String desc = ci.desc.replace("   ", "\n").replace("$THIS_NAME", name)
								.replace("$this_name", name).replace("$THIS_CARD", name).replace("$this_card", name)
								.trim();
						String jsonNumCopies = config.getString(CONFIG_JSON_NUM_COPIES, "").toLowerCase();
						if (jsonNumCopies.equals("")) {
							config.setInfo(CONFIG_JSON_NUM_COPIES, INFO_JSON_NUM_COPIES);
							config.setValue(CONFIG_JSON_NUM_COPIES, "false", INFO_JSON_NUM_COPIES);
						}
						if (jsonNumCopies.equals("true") || jsonNumCopies.equals("villain") && ci.deck.equals("0")
								|| jsonNumCopies.equals("fate") && ci.deck.equals("1")) {
							boolean sing = ci.copies == 1;
							desc = desc
									.concat("\n* There " + (sing ? "is" : "are") + " " + ci.copies + " "
											+ (sing ? "copy" : "copies") + " of " + name + " in your "
											+ (ci.deck.equals("0") ? "deck"
													: (ci.deck.equals("1") ? "Fate deck" : ci.deck + " deck"))
											+ ".")
									.trim();
						}
						name = name.trim().toUpperCase().concat((includeType ? " [" + ci.type + "]" : ""));
						// System.out.println(" (Thread) Writing " + name + ": x" + ci.copies + " times");
						int extraDeckIndex = -1;
						for (int i = 0; extraDeckIndex == -1 && i < extraDecks.length; i++) {
							if (extraDecks[i].equalsIgnoreCase(ci.deck)) {
								extraDeckIndex = i;
							}
						}
						for (int i = 0; i < ci.copies; i++) {
							JSONObject c = new JSONObject();
							c.put("name", name);
							c.put("desc", desc);
							if (ci.deck.equals("0")) {
								cardsV.add(contV++, c);
							} else if (ci.deck.equals("1")) {
								cardsF.add(contF++, c);
							} else if (extraDeckIndex >= 0) {
								cardsExtras[extraDeckIndex].add(countExtras[extraDeckIndex]++, c);
							}
						}
					}

					jsonV.put("cards", cardsV);
					jsonV.put("count", contV);
					jsonF.put("cards", cardsF);
					jsonF.put("count", contF);

					JSONObject jsonT = new JSONObject();
					jsonT.put("villain", jsonV);
					jsonT.put("fate", jsonF);

					for (int i = 0; i < jsonExtras.length; i++) {
						jsonExtras[i].put("cards", cardsExtras[i]);
						jsonExtras[i].put("count", countExtras[i]);
						jsonT.put(extraDecks[i].toLowerCase(), jsonExtras[i]);
					}

					resultsFolder.mkdirs();
					File jsonTFile = new File(resultsFolder, DESCRIPTIONS_JSON);

					try (PrintWriter out = new PrintWriter(jsonTFile)) {
						out.println(jsonT.toString());

						if (!config.contains(CONFIG_COPY_JSON)) {
							config.setInfo(CONFIG_COPY_JSON, INFO_COPY_JSON);
							config.setValue(CONFIG_COPY_JSON, "false", INFO_COPY_JSON);
							config.saveConfig();
						}
						if (config.getBoolean(CONFIG_COPY_JSON, false)) {
							Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
							clipboard.setContents(new StringSelection(jsonT.toString()), null);
						}
					} catch (IOException e) {
						e.printStackTrace();
						warnings.add("JSON Files could not be generated.");

						try {
							e.printStackTrace(new PrintStream(ERROR_DESC_LOG));
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
						semDesc.release();
					}
					semDesc.release();
				}
			}).start();
		} else
			semDesc.release();

		// It's time to print the images of every card!
		// But not to a file. First we draw only in the RAM.
		for (CardInfo ci : usefulCards) {
			// System.out.println("Photocopying card " + ci.name + ": " + ci.copies + " copies in deck " + ci.deck);
			label.setText("Photocopying card " + ci.name + ": " + ci.copies + " copies in "
					+ (ci.deck.equals("0") ? "Villain" : (ci.deck.equals("1") ? "Fate" : ci.deck)) + " deck.");

			for (int i = 0; i < ci.copies; i++) {
				if (ci.deck.equals("0")) {
					gV.drawImage(ci.imageData, xV, yV, null);
					xV += CARD_SIZE.width;
					if (xV >= resultImageV.getWidth()) {
						xV = 0;
						yV += CARD_SIZE.height;
					}
				} else if (ci.deck.equals("1")) {
					gF.drawImage(ci.imageData, xF, yF, null);
					xF += CARD_SIZE.width;
					if (xF >= resultImageF.getWidth()) {
						xF = 0;
						yF += CARD_SIZE.height;
					}
				} else {
					for (int j = 0; j < extraDecks.length; j++) {
						if (extraDecks[j].equalsIgnoreCase(ci.deck)) {
							gExtras[j].drawImage(ci.imageData, xExtras[j], yExtras[j], null);
							xExtras[j] += CARD_SIZE.width;
							if (xExtras[j] >= resultImageExtras[j].getWidth()) {
								xExtras[j] = 0;
								yExtras[j] += CARD_SIZE.height;
							}
							break;
						}
					}
				}
			}
		}

		label.setText("Removing Herobrine.");

		// If the number of copies is not the expected, we notify the user in case they forgot to save their .ods after some changes.
		if (copiesToV != villainExpectedSize) {
			warnings.add("Unexpected number of copies to Vilain deck. Expected was " + villainExpectedSize
					+ " but it was \"" + copiesToV + "\".");
		}
		if (copiesToF != fateExpectedSize) {
			warnings.add("Unexpected error number of copies to Fate deck. Expected was " + fateExpectedSize
					+ " but it was \"" + copiesToF + "\".");
		}

		// We create the results folder if it doesn't exist.
		if (!resultsFolder.exists()) {
			resultsFolder.mkdir();
		}

		// We save the image data into .jpgs files.
		File fileVillainDeck = new File(resultsFolder, config.getString(CONFIG_VILLAIN_NAME) + ".jpg");
		File fileFateDeck = new File(resultsFolder, config.getString(CONFIG_FATE_NAME) + ".jpg");
		File[] filesExtraDecks = new File[extraDecks.length];
		for (int i = 0; i < filesExtraDecks.length; i++) {
			filesExtraDecks[i] = new File(resultsFolder, extraDecks[i] + " deck.jpg");
		}

		if (!config.contains(CONFIG_IMAGE_QUALITY)) {
			config.setInfo(CONFIG_IMAGE_QUALITY, INFO_IMAGE_QUALITY);
			config.setValue(CONFIG_IMAGE_QUALITY, "0.9", INFO_IMAGE_QUALITY);
			config.saveConfig();
		}
		float quality = config.getFloat(CONFIG_IMAGE_QUALITY);

		label.setText("Writing the images for TTS decks.");

		// Big chad compressed writing to file.
		Semaphore semWrite = new Semaphore(-1 - extraDecks.length);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					writeJpgImage(resultImageV, fileVillainDeck, quality);
				} catch (IOException e) {
					e.printStackTrace();
				}
				semWrite.release();
			}
		}).start();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					writeJpgImage(resultImageF, fileFateDeck, quality);
				} catch (IOException e) {
					e.printStackTrace();
				}
				semWrite.release();
			}
		}).start();

		for (int i = 0; i < filesExtraDecks.length; i++) {
			final int index = i;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						writeJpgImage(resultImageExtras[index], filesExtraDecks[index], quality);
					} catch (IOException e) {
						e.printStackTrace();
					}
					semWrite.release();
				}
			}).start();
		}

		try {
			config.saveConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}

		semWrite.acquire();
		semDesc.acquire();

		// We check if the user wants to autoclose and we do it after 500ms if there are
		// no warnings whatsoever. Forget the 500ms I want it to close asap.
		if (autoclose && warnings.isEmpty()) {
			// System.out.println("Autoclose goes brr");
			label.setText("Done. Autoclosing.");
			// Thread.sleep(500);
			window.dispose();
		}
	}

	/**
	 * 
	 * @param resultImage A BufferedImage containing the image data.
	 * @param deckFile    A File, existing or not, to save the data to.
	 * @param quality     0 for priorizing compression, 1 for priorizing quality, or
	 *                    any value in between.
	 * @throws IOException
	 */
	private static void writeJpgImage(BufferedImage resultImage, File deckFile, float quality) throws IOException {
		try (ImageOutputStream ios = ImageIO.createImageOutputStream(deckFile)) {
			ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("JPEG").next();

			ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
			jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			jpgWriteParam.setCompressionQuality(quality);

			jpgWriter.setOutput(ios);

			jpgWriter.write(null, new IIOImage(resultImage, null, null), jpgWriteParam);
			jpgWriter.dispose();
		}
	}

	private static BufferedImage load(File f) throws IOException {
		byte[] bytes = Files.readAllBytes(f.toPath());
		try (InputStream is = new ByteArrayInputStream(bytes)) {
			return ImageIO.read(is);
		}
	}

	// It calculates the optimal dimensions of the file, measured in number of cards
	// wide and number of cards high.
	private static Dimension getGrid(int quantity) {
		// Because it was quite hard to calculate it I just guessed case by case which
		// dimensions would be the most appropriate for each number of cards, including
		// the common 30 and 15, so it first sees if I already did the work manually.
		for (Range index : DECK_SIZES.keySet()) {
			if (index.inRange(quantity)) {
				return DECK_SIZES.get(index);
			}
		}

		// If the number of cards is quite wild, then
		// this algorithm I found on the Internet
		// tries its best to calculate a good one.
		// It is a little bit wacky though.
		// If the dimensions of your deck is weird,
		// either it's like so long or tall, tell me!
		warnings.add("The number of cards \"" + quantity
				+ "\" was a little bit too high so the result might be unexpected. The result might be too tall or too wide.");
		warnings.add("Please tell Cristichi#5193 to add support for " + quantity + " cards! He'll be happy to add it.");
		List<Integer> divisors = getDivisors(quantity);
		divisors.add(1);
		divisors.add(quantity);
		Collections.sort(divisors);
		double sqrt = Math.sqrt(quantity);
		List<Integer> sol;
		if (divisors.size() == 2) {
			sol = divisors;
		} else {
			sol = findKClosestElements(divisors, 2, ((int) Math.round(sqrt)));
		}
		// System.out.println("Grid for " + quantity + ": " + sol);
		// System.out.println("Divisors: " + divisors + " sqrt: " + sqrt);
		return new Dimension(sol.get(1), sol.get(0));
	}

	private static ArrayList<Integer> getDivisors(int n) {
		ArrayList<Integer> divisors = new ArrayList<>();
		for (int i = 2; i * i <= n; ++i)
			if (n % i == 0) {
				divisors.add(i);
				if (i != n / i)
					divisors.add(n / i);
			}
		return divisors;
	}

	private static List<Integer> findKClosestElements(List<Integer> input, int k, int target) {
		int i = Collections.binarySearch(input, target);
		if (i < 0) {
			i = -(i + 1);
		}

		int left = i - 1;
		int right = i;

		while (k-- > 0) {
			if (left < 0 || (right < input.size()
					&& Math.abs(input.get(left) - target) > Math.abs(input.get(right) - target))) {
				right++;
			} else {
				left--;
			}
		}

		return input.subList(left + 1, right);
	}
}