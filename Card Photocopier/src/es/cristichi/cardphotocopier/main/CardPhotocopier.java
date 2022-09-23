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

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.jopendocument.dom.spreadsheet.Cell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import es.cristichi.cardphotocopier.obj.CardInfo;
import es.cristichi.cardphotocopier.obj.Configuration;
import es.cristichi.cardphotocopier.obj.Range;

/**
 * Feel free to edit the code. Have an evil day!
 * 
 * @author Cristichi#5193
 */
public class CardPhotocopier {
	private static String CONFIG_YAML = "config.yml";
	private static Dimension CARD_SIZE = new Dimension(620, 880);
	private static HashMap<Range, Dimension> DECK_SIZES;
	static {
		DECK_SIZES = new HashMap<>(18);
		DECK_SIZES.put(new Range(1, 1), new Dimension(1, 1));
		DECK_SIZES.put(new Range(2, 2), new Dimension(2, 1));
		DECK_SIZES.put(new Range(3, 3), new Dimension(3, 1));
		DECK_SIZES.put(new Range(4, 4), new Dimension(2, 2));
		DECK_SIZES.put(new Range(5, 6), new Dimension(3, 2));
		DECK_SIZES.put(new Range(7, 8), new Dimension(4, 2));
		DECK_SIZES.put(new Range(9, 9), new Dimension(3, 3));
		DECK_SIZES.put(new Range(10, 12), new Dimension(4, 3));
		DECK_SIZES.put(new Range(13, 15), new Dimension(5, 3));
		DECK_SIZES.put(new Range(16, 20), new Dimension(5, 4));
		DECK_SIZES.put(new Range(21, 24), new Dimension(6, 4));
		DECK_SIZES.put(new Range(25, 25), new Dimension(5, 5));
		DECK_SIZES.put(new Range(26, 30), new Dimension(6, 5));
		DECK_SIZES.put(new Range(31, 35), new Dimension(7, 5));
		DECK_SIZES.put(new Range(36, 36), new Dimension(6, 6));
		DECK_SIZES.put(new Range(37, 42), new Dimension(7, 6));
		DECK_SIZES.put(new Range(43, 48), new Dimension(7, 6));
	}

	public static String CONFIG_DOC = "Cards' Info Document", CONFIG_CARD_IMAGES = "Card Images Folder",
			CONFIG_RESULTS = "Results Folder", CONFIG_AUTOCLOSE = "Autoclose", CONFIG_FATE_NAME = "Fate Deck Name",
			CONFIG_VILLAIN_NAME = "Villain Deck Name", CONFIG_FATE_QUANTITY = "Fate Deck's Expected cards",
			CONFIG_VILLAIN_QUANTITY = "Villain Deck's Expected cards";

	private static ArrayList<String> problems;

	private static JFrame window;
	private static JLabel label;

	public static void main(String[] args) {
		problems = new ArrayList<>(10);
		try {
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
			e.printStackTrace();
			System.err.println("Ended with error.");
			label.setText(e.getLocalizedMessage());
			window.pack();
			window.setLocationRelativeTo(null);
		} finally {
			System.out.println("Done.");
			if (problems.size() > 0) {
				window.remove(label);
				window.setLayout(new GridLayout(problems.size() + 1, 1));
				JLabel problemTitle = new JLabel("Process completed succesfully but with some weird things:");
				problemTitle.setBorder(new EmptyBorder(2, 5, 2, 5));
				window.add(problemTitle);
				for (String problem : problems) {
					JLabel lbl = new JLabel(problem);
					lbl.setBorder(new EmptyBorder(2, 5, 2, 5));
					window.add(lbl);
				}
				window.pack();
				window.setLocationRelativeTo(null);
			}
		}
	}

	public static void generate() throws Exception {
		label.setText("Reading config file.");
		// File directory = new File("./");
		// System.out.println("\n\nAll relative paths are relative to:
		// "+directory.getAbsolutePath());

		// TODO: Completely rework the config file to be .yml. Way easier to use.
		Configuration config = new Configuration(CONFIG_YAML, "Villainous Card Photocopier");
		if (!config.exists()) {
			config.setValue(CONFIG_CARD_IMAGES,
					"../Villainous Card Generator V33.2/Villainous Card Generator V33_Data/-Exports",
					"Folder where all the generated images of your Villain's cards are. It must not contain other Villains' cards");
			config.setValue(CONFIG_DOC,
					"../Villainous Card Generator V33.2/Villainous Card Generator V33_Data/-TextFiles/Villainous Template.ods",
					"The path to the .ods file where you have your cards' info. It may contain other Villains' cards, that's fine.");
			config.setValue(CONFIG_RESULTS, "Results", "Where you want the Villain/Fate deck images to be created.");
			config.setValue(CONFIG_AUTOCLOSE, true,
					"True if you want the info window to be automatically closed if everything was ok and expected.");
			config.setValue(CONFIG_VILLAIN_NAME, "Villain deck", "The name of the main deck's file to be generated.");
			config.setValue(CONFIG_FATE_NAME, "Fate deck", "The name of the Fate deck's file to be generated.");
			config.setValue(CONFIG_VILLAIN_QUANTITY, 30,
					"The number of cards that should be expected for this Villain to have in his main deck. Useful in case you didn't count the cards well.");
			config.setValue(CONFIG_FATE_QUANTITY, 15,
					"The number of cards that should be expected for this Villain to have in his Fate deck. Useful in case you didn't count the cards well.");

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
					"Configuration file (config.yml) not found. We generated one for you, please close this window and edit it.");
		}

		config.reloadConfigFromFile();

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
		HashMap<String, CardInfo> cardsInfo = new HashMap<>(50);

		for (File cardFile : imagesFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
			}
		})) {
			String name = cardFile.getName().substring(0,
					cardFile.getName().length() - (cardFile.getName().endsWith(".jpeg") ? 5 : 4));
			label.setText("Loading " + name + "'s image data from it's file.");
			CardInfo info = new CardInfo(load(cardFile));
			if (info.imageData == null) {
				System.err.println("Image " + cardFile + " could not be loaded.");
				problems.add("Image \"" + cardFile.getName() + "\" could not be loaded.");
			} else {
				cardsInfo.put(name, info);
			}
		}

		int copiesToV = 0, copiesToF = 0;
		int xV = 0, yV = 0;
		int xF = 0, yF = 0;

		boolean forceFate = false;
		int done = 0;
		// We are going to look into each row in the .ods and check if it's a card that
		// exists withing the images folder and draw it into it's corresponding deck.
		for (int row = 4; done <= 20; row++) {
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
					if (cardsInfo.containsKey(B.getTextValue())) {
						// We want "done" to count the CONSECUTIVE empty lines, so if we find a proper
						// card we are going to reset it why not
						done = 0;

						CardInfo ci = cardsInfo.get(B.getTextValue());

						if (A.isEmpty() || K.isEmpty() && (!C.isEmpty() || !D.isEmpty() || !E.isEmpty() || !F.isEmpty()
								|| !G.isEmpty() || !H.isEmpty() || !I.isEmpty() || !J.isEmpty() || !L.isEmpty()
								|| !M.isEmpty())) {
							problems.add("Detected error in card " + B.getTextValue() + "."
									+ (A.getTextValue().trim().isEmpty() ? " Number of copies (Column A) not filled."
											: "")
									+ (K.getTextValue().trim().isEmpty() ? " Villain/Fate deck (Column K) not filled."
											: ""));
							System.err.println("Error reading: " + row + " (Card " + B.getTextValue() + " not proper)");
						} else {
							ci.copies = Integer.parseInt(A.getTextValue());

							ci.deck = 0;
							if (K.getTextValue().equals("Fate") || K.getTextValue().equals("1") || forceFate) {
								ci.deck = 1;
								copiesToF += ci.copies;
							} else {
								copiesToV += ci.copies;
							}
						}
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

		Dimension gridV = getGrid(copiesToV);
		Dimension gridF = getGrid(copiesToF);

		if (copiesToV == 0 || copiesToF == 0) {
			throw new IllegalArgumentException("One of your decks has 0 cards! Check it please.");
		}

		// This is the data of the two images. We now create it empty (black) and we'll
		// draw each card over it.
		BufferedImage resultImageV = new BufferedImage(CARD_SIZE.width * gridV.width, CARD_SIZE.height * gridV.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics gV = resultImageV.getGraphics();
		BufferedImage resultImageF = new BufferedImage(CARD_SIZE.width * gridF.width, CARD_SIZE.height * gridF.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics gF = resultImageF.getGraphics();

		for (String cardName : cardsInfo.keySet()) {
			CardInfo ci = cardsInfo.get(cardName);

			System.out.println("Photocopying card " + cardName + ": " + ci.copies + " copies in deck " + ci.deck);
			label.setText("Photocopying card " + cardName + ": " + ci.copies + " copies in "
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

		int villainExpectedSize = config.getInt(CONFIG_VILLAIN_QUANTITY);
		int fateExpectedSize = config.getInt(CONFIG_FATE_QUANTITY);

		// If the number of copies is not the expected, we notify the user in case they
		// forgot to save their .ods after some changes.
		if (copiesToV != villainExpectedSize) {
			problems.add("Unexpected number of copies to Vilain deck. Expected was " + villainExpectedSize
					+ " but it was \"" + copiesToV + "\".");
		}
		if (copiesToF != fateExpectedSize) {
			problems.add("Unexpected error number of copies to Fate deck. Expected was " + fateExpectedSize
					+ " but it was \"" + copiesToF + "\".");
		}

		label.setText("Writing the images for TTS decks.");

		if (!resultsFolder.exists()) {
			resultsFolder.mkdir();
		}

		File villainDeck = new File(resultsFolder, config.getString(CONFIG_VILLAIN_NAME) + ".jpg");
		File fateDeck = new File(resultsFolder, config.getString(CONFIG_FATE_NAME) + ".jpg");

		if (villainDeck.exists()) {
			villainDeck.delete();
		}
		if (fateDeck.exists()) {
			fateDeck.delete();
		}

		ImageIO.write(resultImageV, "jpg", villainDeck);
		ImageIO.write(resultImageF, "jpg", fateDeck);

		if (autoclose && problems.isEmpty()) {
			System.out.println("Autoclose goes brr");
			label.setText("Done. Autoclosing.");
			Thread.sleep(500);
			System.exit(0);
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
		// Because it was quite hard to calculate it, it first sees if I already did the
		// work manually
		for (Range index : DECK_SIZES.keySet()) {
			if (index.inRange(quantity)) {
				return DECK_SIZES.get(index);
			}
		}
		
		//If the number of cards is quite wild, then
		//it tries it best to calculate a good one.
		//It probably will be wacky
		problems.add("The number of cards ("+quantity+") was a little bit too high so the result might be wicked.");
		problems.add("Please tell Cristichi#5193 to add support for "+quantity+" cards.");
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