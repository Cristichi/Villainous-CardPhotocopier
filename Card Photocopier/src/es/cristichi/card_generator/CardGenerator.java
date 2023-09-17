package es.cristichi.card_generator;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jopendocument.dom.spreadsheet.Cell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import es.cristichi.MainInfoFrame;
import es.cristichi.card_generator.obj.DeckTemplate;
import es.cristichi.card_photocopier.obj.ODS.Column;
import es.cristichi.card_photocopier.obj.ODS.OdsStructure;
import es.cristichi.obj.CardInfo;
import es.cristichi.obj.ExtraDeckInfo;
import es.cristichi.obj.config.ConfigValue;
import es.cristichi.obj.config.Configuration;

public class CardGenerator {
	private static Font CARD_TITLE = new Font("ESTEBAN", Font.PLAIN, 100);
	private static Font CARD_TEXT_MAX;
	private static Font CARD_TYPE = new Font("Cabin", Font.BOLD, 95);
	private static Font CARD_CORNER_VALUES;

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

	public ArrayList<String> generate(Configuration config, MainInfoFrame frame, File openDocumentFile,
			File imagesFolder, OdsStructure odsStructure, Sheet sheet) throws Exception {
		ArrayList<String> warnings = new ArrayList<>(3);

		if (!config.contains(ConfigValue.CONFIG_TEMPLATES)) {
			config.setValue(ConfigValue.CONFIG_TEMPLATES, ConfigValue.CONFIG_TEMPLATES.getDefaultValue());
			config.saveToFile();
		}

		File templatesFolder = new File(config.getString(ConfigValue.CONFIG_TEMPLATES));

		if (!templatesFolder.exists()) {
			frame.replaceText("The folder where the templates should be (" + templatesFolder.getAbsolutePath()
					+ ") was not found. Please edit the config file or create it and place the templates there.");
			throw new FileNotFoundException("The folder where the templates should be ("
					+ templatesFolder.getAbsolutePath()
					+ ") was not found. Please edit the config file or create it and place the templates there.");
		}

		DeckTemplate deckTemplVillain = new DeckTemplate("Villain");
		DeckTemplate deckTemplFate = new DeckTemplate("Fate");

		ArrayList<CardInfo> usefulCards = new ArrayList<>(
				config.getInt(ConfigValue.CONFIG_VILLAIN_QUANTITY) + config.getInt(ConfigValue.CONFIG_FATE_QUANTITY));

		HashMap<String, ExtraDeckInfo> extraDecks = new HashMap<>(6);

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
				Cell<SpreadSheet> cellCost = sheet.getCellAt(odsStructure.get(Column.COST) + row);
				Cell<SpreadSheet> cellStrengh = sheet.getCellAt(odsStructure.get(Column.STRENGTH) + row);
				Cell<SpreadSheet> cellAbility = sheet.getCellAt(odsStructure.get(Column.ABILITY) + row);
				Cell<SpreadSheet> cellType = sheet.getCellAt(odsStructure.get(Column.TYPE) + row);
				Cell<SpreadSheet> cellActAbility = sheet.getCellAt(odsStructure.get(Column.ACTIVATE_ABILITY) + row);
				Cell<SpreadSheet> cellActCost = sheet.getCellAt(odsStructure.get(Column.ACTIVATE_COST) + row);
				Cell<SpreadSheet> cellTopRight = sheet.getCellAt(odsStructure.get(Column.TOP_RIGHT) + row);
				Cell<SpreadSheet> cellBotRight = sheet.getCellAt(odsStructure.get(Column.BOTTOM_RIGHT) + row);
				Cell<SpreadSheet> cellDeck = sheet.getCellAt(odsStructure.get(Column.DECK) + row);
				Cell<SpreadSheet> cellAction = sheet.getCellAt(odsStructure.get(Column.ACTION_SYMBOL) + row);
//				Cell<SpreadSheet> cellAutoLayout = sheet.getCellAt(odsStructure.get(Column.AUTO_LAYOUT) + row);
				Cell<SpreadSheet> cellDescription = sheet.getCellAt(odsStructure.get(Column.DESCRIPTION) + row);
				Cell<SpreadSheet> cellExtraDeck = sheet.getCellAt(odsStructure.get(Column.EXTRA_DECK) + row);
				Cell<SpreadSheet> cellCredits = sheet.getCellAt(odsStructure.get(Column.CREDITS) + row);

				// If we find too many empty lines we are going to call it a day, because it
				// might mean we are at the end but there are tons of empty lines.
				// Well, not neccesarily empty lines, but if there is nothing in column B then
				// those are not cards anyway.
				if (cellName.isEmpty()) {
					consecutiveEmptyLines++;
				} else {
					consecutiveEmptyLines = 0;
					String cardName = cellName.getTextValue().replaceAll("[\\\\/:*?\"<>|]", "");
					// So this card in the ODS document
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
						CardInfo ci = new CardInfo(null);
						ci.name = cardName;
						ci.cost = cellCost.getTextValue();
						ci.strength = cellStrengh.getTextValue();
						ci.ability = cellAbility.getTextValue();
						ci.activateAbility = cellActAbility.getTextValue();
						ci.activateCost = cellActCost.getTextValue();
						ci.topRight = cellTopRight.getTextValue();
						ci.bottomLeft = cellBotRight.getTextValue();
						ci.action = cellAction.getTextValue();
						ci.credits = cellCredits.getTextValue();
						
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
								ExtraDeckInfo info = new ExtraDeckInfo();
								info.setDeckTemplate(new DeckTemplate(ci.deck));
								extraDecks.put(ci.deck, info);
							}
							usefulCards.add(ci);
						}
						// If it's not filled, we use the regular Deck column
						if (ci.deck == null) {
							if ((cellDeck.getTextValue().equals("Villain") || cellDeck.getTextValue().equals("0"))) {
								ci.deck = "0";
								usefulCards.add(ci);
							} else if (cellDeck.getTextValue().equals("Fate") || cellDeck.getTextValue().equals("1")) {
								ci.deck = "1";
								usefulCards.add(ci);
							}
						}
					}
				}
			} catch (IllegalArgumentException e) {
				// This means that some columns are combined, so we know it's not a card anyway.
				// System.err.println(e.getLocalizedMessage());
				// System.err.println("Line: " + A.getTextValue());
			}
		}

		return warnings;
	}

}
