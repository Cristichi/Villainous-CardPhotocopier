package es.cristichi.card_photocopier;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import org.jopendocument.dom.spreadsheet.Cell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import es.cristichi.MainInfoFrame;
import es.cristichi.card_photocopier.obj.Range;
import es.cristichi.card_photocopier.obj.ODS.Column;
import es.cristichi.card_photocopier.obj.ODS.OdsStructure;
import es.cristichi.exceptions.ConfigurationException;
import es.cristichi.obj.CardComparator;
import es.cristichi.obj.CardInfo;
import es.cristichi.obj.ExtraDeckInfo;
import es.cristichi.obj.Util;
import es.cristichi.obj.config.ConfigValue;
import es.cristichi.obj.config.Configuration;

/**
 * Feel free to modify the code for yourself and/or propose modifications and
 * improvements. Have an evil day!
 * 
 * @author Cristichi
 */
public class CardPhotocopier {
	private static String DESCRIPTIONS_JSON = "CardPhotocopier descriptions.json";
	private static String ERROR_DESC_LOG = "CardPhotocopier descriptions error.log";
	private static HashMap<Range, Dimension> DECK_SIZES;
	static {
		DECK_SIZES = new HashMap<>(18);
		DECK_SIZES.put(new Range(13, 15), new Dimension(5, 3));
		DECK_SIZES.put(new Range(26, 30), new Dimension(6, 5));
		DECK_SIZES.put(new Range(1, 1), new Dimension(1, 1));
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

	public CardPhotocopier() {
	}

	public ArrayList<String> generate(Configuration config, MainInfoFrame frame, File openDocumentFile, File imagesFolder, File resultsFolder, OdsStructure odsStructure, Sheet sheet) throws Exception {
		ArrayList<String> warnings = new ArrayList<>(3);
		frame.replaceText("Reading card data from " + openDocumentFile.getName() + ".");

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
				frame.replaceText("Loading " + name + "'s image data from the images folder.");
				CardInfo info = new CardInfo(Util.load(cardFile));
				if (info.imageData == null) {
					System.err.println("Image " + cardFile + " could not be loaded.");
					warnings.add("Image \"" + cardFile.getName() + "\" could not be loaded.");
				} else {
					cardsInfo.put(name, info);
				}
			}
		}

		ArrayList<CardInfo> usefulCards = new ArrayList<>(
				config.getInt(ConfigValue.CONFIG_VILLAIN_QUANTITY) + config.getInt(ConfigValue.CONFIG_FATE_QUANTITY));

		HashMap<String, ExtraDeckInfo> extraDecks = new HashMap<>(6);

		int copiesToV = 0, copiesToF = 0;
		int xV = 0, yV = 0;
		int xF = 0, yF = 0;

		int consecutiveEmptyLines = 0;
		int doneLimit = config.getInt(ConfigValue.CONFIG_EMPTY_ROWS_TO_END, 20);

		// We are going to look into each row in the .ods and check if it's a card that
		// exists withing the images folder and draw it into it's corresponding deck.
		for (int row = 1; consecutiveEmptyLines <= doneLimit; row++) {
			Cell<SpreadSheet> cellCopiesCount = sheet.getCellAt(odsStructure.get(Column.COPIES_COUNT) + row);

			if (cellCopiesCount.getTextValue().trim().equalsIgnoreCase("#stop")) {
				break;
			}
			try {
				// We get all the data. The unused ones commented in case I want to do something with it one day.

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
					consecutiveEmptyLines++;
				} else {
					consecutiveEmptyLines = 0;
					String cardName = cellName.getTextValue().replaceAll("[\\\\/:*?\"<>|]", "");
					if (cardsInfo.containsKey(cardName)) {
						// So this card in the ODS document is also in the exports folder. Great!
						if (cellCopiesCount.isEmpty() || cellType.isEmpty() || cellDeck.isEmpty()) {
							// If we are missing important data and it's not because everything is empty, we
							// are going to warn the user so they can check if the .ods document is properly
							// filled.
							warnings.add("Detected error in a possible card \"" + cardName + "\"."
									+ (cellCopiesCount.getTextValue().trim().isEmpty()
											? " Number of copies (Column " + odsStructure.get(Column.COPIES_COUNT)
													+ ") is not filled."
											: "")
									+ (cellType.getTextValue().trim().isEmpty()
											? " Type (Column " + odsStructure.get(Column.TYPE) + ") is not filled."
											: "")
									+ (cellDeck.getTextValue().trim().isEmpty()
											? " Deck (Column " + odsStructure.get(Column.DECK) + ") is not filled."
											: ""));
							System.err.println("Error reading: " + row + " (Card " + cardName + " not proper)");
						} else {
							// We add the information found about this card
							CardInfo ci = cardsInfo.get(cardName);
							ci.name = cardName;
							ci.type = cellType.getTextValue();
							ci.copies = Integer.parseInt(cellCopiesCount.getTextValue());
							ci.desc = cellDescription.getTextValue();
							ci.row = row;
							frame.replaceText("Saving " + ci.name + "'s image data and card information.");

							// We first check if the Extra Deck column is filled.
							if (!cellExtraDeck.getTextValue().trim().equals("")) {
								ci.deck = cellExtraDeck.getTextValue().trim();
								if (extraDecks.containsKey(ci.deck)) {
									extraDecks.get(ci.deck).addCount(ci.copies);
								} else {
									extraDecks.put(ci.deck, new ExtraDeckInfo());
								}
								usefulCards.add(ci);
							}
							// If it's not filled, we use the regular Deck column
							if (ci.deck == null) {
								if ((cellDeck.getTextValue().equals("Villain")
										|| cellDeck.getTextValue().equals("0"))) {
									ci.deck = "0";
									copiesToV += ci.copies;
									usefulCards.add(ci);
								} else if (cellDeck.getTextValue().equals("Fate")
										|| cellDeck.getTextValue().equals("1")) {
									ci.deck = "1";
									copiesToF += ci.copies;
									usefulCards.add(ci);
								}
							}
						}

						cardsInfo.remove(cardName);
					}
				}
			} catch (IllegalArgumentException e) {
				// This means that some columns are combined, so we know it's not a card anyway.
				// System.err.println(e.getLocalizedMessage());
				// System.err.println("Line: " + A.getTextValue());
			}
		}

		cardsInfo.clear();

		frame.replaceText("Checking deck sizes.");

		int villainExpectedSize = config.getInt(ConfigValue.CONFIG_VILLAIN_QUANTITY);
		int fateExpectedSize = config.getInt(ConfigValue.CONFIG_FATE_QUANTITY);

		if (copiesToV == 0 && copiesToV != villainExpectedSize && copiesToF == 0 && copiesToF != fateExpectedSize) {
			throw new IllegalArgumentException("Both your Villain and Fate decks have 0 cards! Check it please."
					+ (doneLimit < Integer.parseInt(ConfigValue.CONFIG_EMPTY_ROWS_TO_END.getDefaultValue())
							? " You might have to increase the " + ConfigValue.CONFIG_EMPTY_ROWS_TO_END.getKey()
									+ " (its current value is " + doneLimit + ")"
							: ""));
		} else if (copiesToV == 0 && copiesToV != villainExpectedSize) {
			throw new IllegalArgumentException("Your Villain deck has 0 cards! Check it please.");
		} else if (copiesToF == 0 && copiesToF != fateExpectedSize) {
			throw new IllegalArgumentException("Your Fate deck has 0 cards! Check it please.");
		}

		frame.replaceText("Calculating final images' dimensions.");

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

		for (String extraDeckName : extraDecks.keySet()) {
			ExtraDeckInfo eDeck = extraDecks.get(extraDeckName);
			eDeck.setGrid(getGrid(eDeck.getCount()));
			eDeck.setImage(new BufferedImage(CARD_SIZE.width * eDeck.getGrid().width,
					CARD_SIZE.height * eDeck.getGrid().height, BufferedImage.TYPE_INT_RGB));
			extraDecks.put(extraDeckName, eDeck);
		}

		frame.replaceText("Reordering cards");

		// We are going to read the order the user wants and sort the cards by that order.
		String order = config.getString(ConfigValue.CONFIG_TYPE_ORDER, "ignore type");
		String[] orderSplit = order.split(",");
		for (int i = 0; i < orderSplit.length; i++) {
			orderSplit[i] = orderSplit[i].trim();
		}
		usefulCards.sort(new CardComparator(orderSplit));

		// This whole hocus pocus is for the descriptions feature to later use
		// with the TTS Description Loader https://steamcommunity.com/sharedfiles/filedetails/?id=2899195933
		Semaphore semDesc = new Semaphore(0);
		if (config.getBoolean(ConfigValue.CONFIG_GENERATE_JSON, false)) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					boolean includeType = config.getBoolean(ConfigValue.CONFIG_TYPE_IN_JSON, false);

					JSONObject jsonV = new JSONObject();
					jsonV.put("name", config.getString(ConfigValue.CONFIG_VILLAIN_NAME, "Villain Deck"));
					JSONArray cardsV = new JSONArray();

					JSONObject jsonF = new JSONObject();
					jsonF.put("name", config.getString(ConfigValue.CONFIG_FATE_NAME, "Fate Deck"));
					JSONArray cardsF = new JSONArray();

					for (String extraDeckName : extraDecks.keySet()) {
						ExtraDeckInfo eDeck = extraDecks.get(extraDeckName);
						JSONObject jObj = new JSONObject();
						jObj.put("name", extraDeckName.toLowerCase());
						eDeck.setJsonObject(jObj);
						eDeck.setJsonArrayCards(new JSONArray());
						extraDecks.put(extraDeckName, eDeck);
					}

					int countV = 0;
					int countF = 0;
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
						String jsonNumCopies = config.getString(ConfigValue.CONFIG_JSON_NUM_COPIES, "").toLowerCase();
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

						for (int i = 0; i < ci.copies; i++) {
							JSONObject jsonCard = new JSONObject();
							jsonCard.put("name", name);
							jsonCard.put("desc", desc);
							if (ci.deck.equals("0")) {
								cardsV.add(jsonCard);
								countV++;
							} else if (ci.deck.equals("1")) {
								cardsF.add(jsonCard);
								countF++;
							} else if (extraDecks.containsKey(ci.deck)) {
								extraDecks.get(ci.deck).getJsonArrayCards().add(jsonCard);
								System.out.println(extraDecks.get(ci.deck).getJsonArrayCards().toString());
							}
						}
					}

					jsonV.put("cards", cardsV);
					jsonV.put("count", countV);
					jsonF.put("cards", cardsF);
					jsonF.put("count", countF);

					JSONObject jsonT = new JSONObject();
					jsonT.put("villain", jsonV);
					jsonT.put("fate", jsonF);

					for (String extraDeckName : extraDecks.keySet()) {
						ExtraDeckInfo eDeck = extraDecks.get(extraDeckName);
						eDeck.getJsonObject().put("cards", eDeck.getJsonArrayCards());
						eDeck.getJsonObject().put("count", eDeck.getCount());
						extraDecks.put(extraDeckName, eDeck);

						jsonT.put(extraDeckName, eDeck.getJsonObject());
					}

					resultsFolder.mkdirs();
					File jsonTFile = new File(resultsFolder, DESCRIPTIONS_JSON);

					try (PrintWriter out = new PrintWriter(jsonTFile)) {
						out.println(jsonT.toString());

						if (config.getBoolean(ConfigValue.CONFIG_COPY_JSON, false)) {
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
			frame.replaceText("Photocopying " + ci.name + ".");

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
				} else if (extraDecks.containsKey(ci.deck)) {
					ExtraDeckInfo eDeck = extraDecks.get(ci.deck);
					eDeck.getGraphics().drawImage(ci.imageData, eDeck.getX(), eDeck.getY(), null);
					eDeck.addX(CARD_SIZE.width);
					if (eDeck.getX() >= eDeck.getImage().getWidth()) {
						eDeck.setX(0);
						eDeck.addY(CARD_SIZE.height);
					}
					extraDecks.put(ci.deck, eDeck);
				}
			}
		}

		frame.replaceText("Removing Herobrine.");

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
		File fileVillainDeck = new File(resultsFolder, config.getString(ConfigValue.CONFIG_VILLAIN_NAME) + ".jpg");
		File fileFateDeck = new File(resultsFolder, config.getString(ConfigValue.CONFIG_FATE_NAME) + ".jpg");

		float quality = config.getFloat(ConfigValue.CONFIG_RESULTS_QUALITY, 0.9f);

		frame.replaceText("Writing the images for TTS decks.");

		// Big chad compressed writing to file.
		Semaphore semWrite = new Semaphore(-1 - extraDecks.size());
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Util.writeJpgImage(resultImageV, fileVillainDeck, quality);
				} catch (Exception e) {
					e.printStackTrace();
					warnings.add("Error when writing file " + fileVillainDeck.getName() + ": " + e.getMessage());
				}
				semWrite.release();
			}
		}).start();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Util.writeJpgImage(resultImageF, fileFateDeck, quality);
				} catch (Exception e) {
					e.printStackTrace();
					warnings.add("Error when writing file " + fileFateDeck.getName() + ": " + e.getMessage());
				}
				semWrite.release();
			}
		}).start();

		for (String extraName : extraDecks.keySet()) {
			ExtraDeckInfo eDeck = extraDecks.get(extraName);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Util.writeJpgImage(eDeck.getImage(), new File(resultsFolder, extraName + ".jpg"), quality);
					} catch (Exception e) {
						e.printStackTrace();
						warnings.add("Error when writing file " + extraName + ".jpg: " + e.getMessage());
					}
					semWrite.release();
				}
			}).start();
		}

		semWrite.acquire();
		semDesc.acquire();

		return warnings;
	}

	// It calculates the optimal dimensions of the file, measured in number of cards
	// wide and number of cards high.
	private static Dimension getGrid(int quantity) throws ConfigurationException {
		// Because it was quite hard to calculate it, I just guessed case by case which
		// dimensions would be the most appropriate for each number of cards, returning
		// first the common 30 and 15, so it first sees if I already did the work manually.
		for (Range index : DECK_SIZES.keySet()) {
			if (index.inRange(quantity)) {
				return DECK_SIZES.get(index);
			}
		}

		// If the number of cards is quite wild, then I found an algorithm on the Internet
		// that might have been able to sort this problem out and calculate the optimal
		// dimensions, but it didn't work. So It just throws an error now. Sorry!
		throw new ConfigurationException("The number of copies (" + quantity
				+ ") is not supported. Ask Cristichi to add support to it, pronto!");
	}
}