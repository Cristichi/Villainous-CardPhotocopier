package es.cristichi.cardphotocopier.main;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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

import es.cristichi.cardphotocopier.obj.CardComparator;
import es.cristichi.cardphotocopier.obj.CardInfo;
import es.cristichi.cardphotocopier.obj.Configuration;
import es.cristichi.cardphotocopier.obj.Range;

/**
 * Feel free to modify the code for yourself and/or propose modifications and
 * improvements. Have an evil day!
 * 
 * @author Cristichi#5193
 */
public class CardPhotocopier {
	private static String CONFIG_YAML = "config.yml";
	private static Dimension CARD_SIZE = new Dimension(620, 880);
	private static HashMap<Range, Dimension> DECK_SIZES;
	static {
		DECK_SIZES = new HashMap<>(18);
		DECK_SIZES.put(new Range(13, 15), new Dimension(5, 3));
		DECK_SIZES.put(new Range(26, 30), new Dimension(6, 5));
		DECK_SIZES.put(new Range(1, 1), new Dimension(1, 1));
		DECK_SIZES.put(new Range(2, 2), new Dimension(2, 1));
		DECK_SIZES.put(new Range(3, 3), new Dimension(3, 1));
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
	}

	public static String CONFIG_DOC = "Cards' Info Document", CONFIG_CARD_IMAGES = "Card Images Folder",
			CONFIG_RESULTS = "Results Folder", CONFIG_AUTOCLOSE = "Autoclose", CONFIG_FATE_NAME = "Fate Deck Name",
			CONFIG_VILLAIN_NAME = "Villain Deck Name", CONFIG_FATE_QUANTITY = "Fate Deck's Expected cards",
			CONFIG_VILLAIN_QUANTITY = "Villain Deck's Expected cards", CONFIG_EMPTY_ROWS_TO_END = "Empty Rows to End",
			CONFIG_TYPE_ORDER = "Card Order", CONFIG_IMAGE_QUALITY = "Image Quality";
	public static String INFO_DOC = "The path to the .ods file where you have your cards' info. It may contain other Villains' cards, that's fine.",
			INFO_CARD_IMAGES = "Folder where all the generated images of your Villain's cards are. It must not contain other Villains' cards",
			INFO_RESULTS = "Where you want the Villain/Fate deck images to be created.",
			INFO_AUTOCLOSE = "True if you want the info window to be automatically closed if everything was ok and expected.",
			INFO_FATE_NAME = "The name of the Fate deck's file to be generated.",
			INFO_VILLAIN_NAME = "The name of the main deck's file to be generated.",
			INFO_FATE_QUANTITY = "The number of cards that should be expected for this Villain to have in his Fate deck. Useful in case you didn't count the cards well.",
			INFO_VILLAIN_QUANTITY = "The number of cards that should be expected for this Villain to have in his main deck. Useful in case you didn't count the cards well.",
			INFO_EMPTY_ROWS_TO_END = "This is a little bit technical. It's not easy to detect when we can stop reading new lines so what we do is that if we detect an X "
					+ "number of empty lines in a row we suppose we reached the end of the document. We recommend a value of 20 and if it's lower it will finish way "
					+ "faster but it might not reach your villain's cards if they are at the end of your .ods document.",
			INFO_TYPE_ORDER = "Here you can alter the order of the types. To make it use the default order, write something like "
					+ "\"" + CONFIG_TYPE_ORDER
					+ ": Hero, Condition, Effect, Ally, Item\" (without quotation marks). To make it order by name, remove this value entirely.",
			INFO_IMAGE_QUALITY = "The quality of the resulting images. Put \"1\" for the best quality but large image, "
					+ "\"0\" for the poorest quality (horrible trust me) and smallest image possible. Recommended is \"0.7\" so keep it that way unless you need the file to be even smaller.";

	private static ArrayList<String> warnings;

	private static JFrame window;
	private static JLabel label;

	public static void main(String[] args) {
		warnings = new ArrayList<>(10);
		try {
			// We first create a new window so we can tell the user how things are going.
			window = new JFrame("Villainous Card Photocopier");
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			label = new JLabel("Starting");
			label.setHorizontalAlignment(JLabel.CENTER);
			window.add(label);
			window.setMinimumSize(new Dimension(500, 200));
			window.setLocationRelativeTo(null);
			window.setAlwaysOnTop(true);
			window.setVisible(true);
			generate();
			label.setText("Done. Close this window to finish.");

		} catch (Exception e) {
			// If anything happens that makes the proccess unable to continue (things are
			// missing, wrong values in the .ods file, etc) then we just stop and tell
			// the user what happened. We also print in the console just in case.
			System.err.println("Ended badly with error. Sadge.");
			e.printStackTrace();
			label.setText(e.getLocalizedMessage());
			window.pack();
			window.setLocationRelativeTo(null);

		} finally {
			// After we are done, no matter if there was an error or not, we are going to
			// show any item in "warnings" so the user can fix whatever weird thing we found
			// (cards missing information, too many copies or not enough, etc)
			if (warnings.size() > 0) {
				window.remove(label);
				window.setLayout(new GridLayout(warnings.size() + 1, 1));
				JLabel warningTitle = new JLabel("Process completed succesfully but with some weird things:");
				warningTitle.setBorder(new EmptyBorder(2, 5, 2, 5));
				window.add(warningTitle);
				for (String w : warnings) {
					JLabel lbl = new JLabel(w);
					lbl.setBorder(new EmptyBorder(1, 5, 1, 5));
					window.add(lbl);
				}
				window.pack();
				window.setLocationRelativeTo(null);
			}
		}
	}

	public static void generate() throws Exception {
		label.setText("Reading config file.");

		Configuration config = new Configuration(CONFIG_YAML,
				"Villainous Card Photocopier configuration.\n For help, contact Cristichi#5193 on discord.");
		if (!config.exists()) {
			config.setValue(CONFIG_CARD_IMAGES,
					"../Villainous Card Generator V33.2/Villainous Card Generator V33_Data/-Exports", INFO_CARD_IMAGES);
			config.setValue(CONFIG_DOC,
					"../Villainous Card Generator V33.2/Villainous Card Generator V33_Data/-TextFiles/Villainous Template.ods",
					INFO_DOC);
			config.setValue(CONFIG_RESULTS, "Results", INFO_RESULTS);
			config.setValue(CONFIG_AUTOCLOSE, true, INFO_AUTOCLOSE);
			config.setValue(CONFIG_VILLAIN_NAME, "Villain deck", INFO_VILLAIN_NAME);
			config.setValue(CONFIG_FATE_NAME, "Fate deck", INFO_FATE_NAME);
			config.setValue(CONFIG_VILLAIN_QUANTITY, 30, INFO_VILLAIN_QUANTITY);
			config.setValue(CONFIG_FATE_QUANTITY, 15, INFO_FATE_QUANTITY);
			config.setValue(CONFIG_EMPTY_ROWS_TO_END, 20, INFO_EMPTY_ROWS_TO_END);
			config.setValue(CONFIG_TYPE_ORDER, "Hero, Condition, Effect, Ally, Item", INFO_TYPE_ORDER);
			config.setValue(CONFIG_IMAGE_QUALITY, "0.7", INFO_IMAGE_QUALITY);

			config.saveConfig();

			System.out.println(config.getAbsolutePath());

			window.setMinimumSize(new Dimension(800, 200));
			window.pack();
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
			label.setText(
					"Configuration file (config.yml) not found. We generated one for you, please close this window and edit it accordingly.");
			throw new FileNotFoundException(
					"Configuration file (config.yml) not found. We generated one for you, please close this window and edit it accordingly.");
		}

		config.reloadConfigFromFile();
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
		config.saveConfig();

		boolean autoclose = config.getBoolean(CONFIG_AUTOCLOSE);

		File imagesFolder = new File(config.getString(CONFIG_CARD_IMAGES));
		File resultsFolder = new File(config.getString(CONFIG_RESULTS));
		File documentFile = new File(config.getString(CONFIG_DOC));

		if (!imagesFolder.exists()) {
			window.setMinimumSize(new Dimension(800, 200));
			window.pack();
			window.setLocationRelativeTo(null);
			label.setText("The folder where the already existing images for the cards are supposed to be ("
					+ imagesFolder.getAbsolutePath() + ") was not found. We need that one.");
			throw new FileNotFoundException(
					"The folder where the already existing images for the cards are supposed to be ("
							+ imagesFolder.getAbsolutePath() + ") was not found. We need that one.");
		}
		if (!documentFile.exists()) {
			window.setMinimumSize(new Dimension(800, 200));
			window.pack();
			window.setLocationRelativeTo(null);
			label.setText("The .ods document with the information for each card (" + documentFile.getAbsolutePath()
					+ ") was not found. We need that one.");
			throw new FileNotFoundException("The .ods document with the information for each card ("
					+ documentFile.getAbsolutePath() + ") was not found. We need that one.");
		}

		label.setText("Reading card data from " + documentFile.getName() + ".");

		SpreadSheet sheetDoc = SpreadSheet.createFromFile(documentFile);
		Sheet sheet = sheetDoc.getFirstSheet();

		// First we read every .png, .jpg and .jpeg file.
		HashMap<String, CardInfo> cardsInfo = new HashMap<>(60);

		for (File cardFile : imagesFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
			}
		})) {
			String name = cardFile.getName().substring(0,
					cardFile.getName().length() - (cardFile.getName().endsWith(".jpeg") ? 5 : 4));
			if (!name.isEmpty()) {
				label.setText("Loading " + name + "'s image data from it's file.");
				System.out.println("Loading " + name + "'s image data from it's file.");
				CardInfo info = new CardInfo(load(cardFile));
				if (info.imageData == null) {
					System.err.println("Image " + cardFile + " could not be loaded.");
					warnings.add("Image \"" + cardFile.getName() + "\" could not be loaded.");
				} else {
					cardsInfo.put(name, info);
				}
			}
		}

		ArrayList<CardInfo> usefulCards = new ArrayList<>(
				config.getInt(CONFIG_VILLAIN_QUANTITY) + config.getInt(CONFIG_FATE_QUANTITY));

		int copiesToV = 0, copiesToF = 0;
		int xV = 0, yV = 0;
		int xF = 0, yF = 0;

		boolean forceFate = false;
		int done = 0;
		if (!config.contains(CONFIG_EMPTY_ROWS_TO_END)) {
			config.setInfo(CONFIG_EMPTY_ROWS_TO_END, INFO_EMPTY_ROWS_TO_END);
			config.setValue(CONFIG_EMPTY_ROWS_TO_END, 20, INFO_EMPTY_ROWS_TO_END);
			config.saveConfig();
		}
		int doneLimit = config.getInt(CONFIG_EMPTY_ROWS_TO_END, 20);
		// We are going to look into each row in the .ods and check if it's a card that
		// exists withing the images folder and draw it into it's corresponding deck.
		for (int row = 4; done <= doneLimit; row++) {
			Cell<SpreadSheet> A = sheet.getCellAt("A" + row);
			try {
				Cell<SpreadSheet> B = sheet.getCellAt("B" + row);
				Cell<SpreadSheet> C = sheet.getCellAt("C" + row);
				Cell<SpreadSheet> D = sheet.getCellAt("D" + row);
				Cell<SpreadSheet> E = sheet.getCellAt("E" + row);
				Cell<SpreadSheet> F = sheet.getCellAt("F" + row);
				Cell<SpreadSheet> G = sheet.getCellAt("G" + row);
				Cell<SpreadSheet> H = sheet.getCellAt("H" + row);
				Cell<SpreadSheet> I = sheet.getCellAt("I" + row);
				Cell<SpreadSheet> J = sheet.getCellAt("J" + row);
				Cell<SpreadSheet> K = sheet.getCellAt("K" + row);
				Cell<SpreadSheet> L = sheet.getCellAt("L" + row);
				Cell<SpreadSheet> M = sheet.getCellAt("M" + row);

				// If we find too many empty lines we are going to call it a day, because it
				// might mean we are at the end but there are tons of empty lines.
				// Well, not neccesarily empty lines, but if there is nothing in column B then
				// those are not cards anyway.
				if (B.isEmpty()) {
					done++;
				} else {
					done = 0;
					if (cardsInfo.containsKey(B.getTextValue())) {
						// We want "done" to count the CONSECUTIVE empty lines, so if we find a proper
						// card we are going to reset it why not.

						if (A.isEmpty() || K.isEmpty() && (!C.isEmpty() || !D.isEmpty() || !E.isEmpty() || !F.isEmpty()
								|| !G.isEmpty() || !H.isEmpty() || !I.isEmpty() || !J.isEmpty() || !L.isEmpty()
								|| !M.isEmpty())) {
							// If we are missing data and it's not because everything is empty, we are going
							// to warn the user so they can check if the .ods document is not properly
							// filled.
							warnings.add("Detected error in card " + B.getTextValue() + "."
									+ (A.getTextValue().trim().isEmpty() ? " Number of copies (Column A) not filled."
											: "")
									+ (K.getTextValue().trim().isEmpty() ? " Villain/Fate deck (Column K) not filled."
											: ""));
							System.err.println("Error reading: " + row + " (Card " + B.getTextValue() + " not proper)");
						} else {
							// We add the information found about this card
							CardInfo ci = cardsInfo.get(B.getTextValue());
							ci.name = B.getTextValue();
							ci.type = F.getTextValue();
							ci.copies = Integer.parseInt(A.getTextValue());
							System.out.println("Loading data for " + ci.name + ": x" + ci.copies + ".");

							if ((K.getTextValue().equals("Villain") || K.getTextValue().equals("0")) && !forceFate) {
								ci.deck = 0;
								copiesToV += ci.copies;
								usefulCards.add(ci);
							} else if (K.getTextValue().equals("Fate") || K.getTextValue().equals("1") || forceFate) {
								ci.deck = 1;
								copiesToF += ci.copies;
								usefulCards.add(ci);
							} else {
								System.out.println("Card " + B.getTextValue() + " is from another deck.");
							}
						}

						cardsInfo.remove(B.getTextValue());
					}
				}
			} catch (IllegalArgumentException e) {
				// This probably means that some columns are combined, so we know it's not a
				// card.
				System.err.println(e.getLocalizedMessage());
				System.err.println("Line: " + A.getTextValue());

				// If the column A contains "- Fate -", it's Fate forcing time. This allows
				// villains that need to generate Fate cards as Villain cards with
				// a different layout to still tell my tool which cards are Fate. Read the Usage
				// guide to know how.
				if (A.getTextValue().contains("- Fate -")) {
					forceFate = true;
					System.out.println("Detected \"- Fate -\". Forcing fate from now on");
				} else {
					forceFate = false;
					System.out.println("Interpreted as end of force Fate. No longer forcing fate");
				}
			}
		}

		if (copiesToV == 0 && copiesToF == 0) {
			throw new IllegalArgumentException("Both your Villain and Fate decks has 0 cards! Check it please.");
		} else if (copiesToV == 0) {
			throw new IllegalArgumentException("Your Villain deck has 0 cards! Check it please.");
		} else if (copiesToF == 0) {
			throw new IllegalArgumentException("Your Fate deck has 0 cards! Check it please.");
		}

		// We get the proper dimensions for the final image, depending on the number of
		// cards.
		Dimension gridV = getGrid(copiesToV);
		Dimension gridF = getGrid(copiesToF);

		// This is the data of the two images. We now create it empty (black) and we'll
		// draw each card over it.
		BufferedImage resultImageV = new BufferedImage(CARD_SIZE.width * gridV.width, CARD_SIZE.height * gridV.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics gV = resultImageV.getGraphics();
		BufferedImage resultImageF = new BufferedImage(CARD_SIZE.width * gridF.width, CARD_SIZE.height * gridF.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics gF = resultImageF.getGraphics();

		cardsInfo.clear();

		//We are going to read the order the user wants and sort the cards by that order
		if (!config.contains(CONFIG_TYPE_ORDER)) {
			config.setInfo(CONFIG_TYPE_ORDER, INFO_TYPE_ORDER);
			config.setValue(CONFIG_TYPE_ORDER, "ignore_type", INFO_TYPE_ORDER);
			config.saveConfig();
		}
		String order = config.getString(CONFIG_TYPE_ORDER, "ignore type");
		String[] orderSplit = order.split(",");
		for (int i = 0; i < orderSplit.length; i++) {
			orderSplit[i] = orderSplit[i].trim();
		}
		usefulCards.sort(new CardComparator(orderSplit));
		
		//It's time to write the images of every card!
		for (CardInfo ci : usefulCards) {
			System.out.println("Photocopying card " + ci.name + ": " + ci.copies + " copies in deck " + ci.deck);
			label.setText("Photocopying card " + ci.name + ": " + ci.copies + " copies in "
					+ (ci.deck == 0 ? "Villain" : "Fate") + " deck.");

			for (int i = 0; i < ci.copies; i++) {
				if (ci.deck == 0) {
					gV.drawImage(ci.imageData, xV, yV, null);
					xV += CARD_SIZE.width;
					if (xV >= resultImageV.getWidth()) {
						xV = 0;
						yV += CARD_SIZE.height;
					}
				} else {
					gF.drawImage(ci.imageData, xF, yF, null);
					xF += CARD_SIZE.width;
					if (xF >= resultImageF.getWidth()) {
						xF = 0;
						yF += CARD_SIZE.height;
					}
				}
			}
		}

		label.setText("Removing Herobrine.");

		// If the number of copies is not the expected, we notify the user in case they
		// forgot to save their .ods after some changes.
		int villainExpectedSize = config.getInt(CONFIG_VILLAIN_QUANTITY);
		int fateExpectedSize = config.getInt(CONFIG_FATE_QUANTITY);

		if (copiesToV != villainExpectedSize) {
			warnings.add("Unexpected number of copies to Vilain deck. Expected was " + villainExpectedSize
					+ " but it was \"" + copiesToV + "\".");
		}
		if (copiesToF != fateExpectedSize) {
			warnings.add("Unexpected error number of copies to Fate deck. Expected was " + fateExpectedSize
					+ " but it was \"" + copiesToF + "\".");
		}

		label.setText("Writing the images for TTS decks.");

		// We create the results folder if it doesn't exist.
		if (!resultsFolder.exists()) {
			resultsFolder.mkdir();
		}

		// We save the image data into .jpgs files.
		File villainDeck = new File(resultsFolder, config.getString(CONFIG_VILLAIN_NAME) + ".jpg");
		File fateDeck = new File(resultsFolder, config.getString(CONFIG_FATE_NAME) + ".jpg");

		if (!config.contains(CONFIG_IMAGE_QUALITY)) {
			config.setInfo(CONFIG_IMAGE_QUALITY, INFO_IMAGE_QUALITY);
			config.setValue(CONFIG_IMAGE_QUALITY, "0.7", INFO_IMAGE_QUALITY);
			config.saveConfig();
		}
		float quality = config.getFloat(CONFIG_IMAGE_QUALITY);
		// Big chad compressed writing to file.
		writeImage(resultImageV, villainDeck, quality);
		writeImage(resultImageF, fateDeck, quality);

		// We check if the user wants to autoclose and we do it after 500ms if there are
		// no warnings whatsoever.
		if (autoclose && warnings.isEmpty()) {
			System.out.println("Autoclose goes brr");
			label.setText("Done. Autoclosing.");
			Thread.sleep(500);
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
	private static void writeImage(BufferedImage resultImage, File deckFile, float quality) throws IOException {
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

	// It calculates the optimal dimensions of the file
	// In number of cards wide and number of cards high
	private static Dimension getGrid(int quantity) {
		// Because it was quite hard to calculate it I just guessed case by case which
		// dimensions would be the most appropriate for each number of cards, including
		// the common 30 and 15, so it first sees if I already did the work manually
		for (Range index : DECK_SIZES.keySet()) {
			if (index.inRange(quantity)) {
				return DECK_SIZES.get(index);
			}
		}

		// If the number of cards is quite wild, then
		// it tries it best to calculate a good one.
		// It probably will be wacky
		warnings.add("The number of cards (" + quantity + ") was a little bit too high so the result might be wicked.");
		warnings.add("Please tell Cristichi#5193 to add support for " + quantity + " cards.");
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
		System.out.println("Grid for " + quantity + ": " + sol);
		System.out.println("Divisors: " + divisors + " sqrt: " + sqrt);
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