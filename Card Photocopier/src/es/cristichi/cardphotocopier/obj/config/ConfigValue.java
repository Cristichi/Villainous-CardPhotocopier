package es.cristichi.cardphotocopier.obj.config;

import jdk.internal.jline.internal.Nullable;

public enum ConfigValue {
	CONFIG_GENERATOR_VERSION("cardGeneratorVersion", "33.2", "The version of FailureFactory's Villainous Card Generator, as a number. "
			+ "For example, if you are running V33.2 you have to put here \"33.2\". "
			+ "This is improtant because the columns of the .ods file are different for version V35 onwards."),
	CONFIG_CARD_IMAGES("generatedCardsFolder", "Results", "Folder where all the generated images of your Villain's cards are. It must not contain other Villains' cards"),
	CONFIG_DOC("cardsInfoOds", "./Villainous Card Generator V35/Villainous Card Generator_Data/-TextFiles/Villainous Template.ods", "The path to the .ods file where you have your cards' info."),
	CONFIG_RESULTS("resultsFolder", "Results", "Where you want the Villain/Fate deck images to be created. I also recommend just setting it to \".\" so that they are generated in the same folder as the .jar file."),
	CONFIG_VILLAIN_NAME("villainDeckName", "Villain deck", "The name of the main deck's image file to be generated."),
	CONFIG_FATE_NAME("fateDeckName", "Fate deck", "The name of the Fate deck's image file to be generated."),
	CONFIG_VILLAIN_QUANTITY("villainDeckQuantity", "30", "The number of cards that should be expected for this Villain to have in their main deck. Useful in case you didn't count the cards well."),
	CONFIG_FATE_QUANTITY("fateDeckQuantity", "15", "The number of cards that should be expected for this Villain to have in their Fate deck. Useful in case you didn't count the cards well."),
	CONFIG_EXTRA_DECKS("extraDecks", "", "Create additional decks with this option. Write here the list of the names of each extra deck, separated by comma if there is more than one. Put those values on the \"O\" column in the document for each card contained in any extra deck to get them sorted into the appropriate extra deck."),
	CONFIG_TYPE_ORDER("cardTypeOrder", "Hero, Condition, Effect, Ally, Item", "Here you can alter the order depending the types. Cards of the same type will be ordered by name respective to each other, and cards of an unlisted type will be last by name. To make use of the default order recommended by me, write something like \"cardTypeOrder: Hero, Condition, Effect, Ally, Item\" (without quotation marks). To make it order by name, remove this value entirely."),
	CONFIG_EMPTY_ROWS_TO_END("maxEmptyRowsToEnd", "20", "So the way this tool knows when to stop reading the .ods document is when it encounters enough empty lines. This controls the number of empty lines. Setting it lower will make the tool finish considerably faster. I recommend testing it with your .ods document to see the lowest you can set it. If set too low, it finds no cards."),
	CONFIG_IMAGE_QUALITY("imageQuality", "0.9", "The quality of the resulting images. \"1\" means best quality but large image, \"0\" means poorest quality (horrible trust me) and smallest image possible. Recommended is \"0.9\", since the loss in 10% quality is not easily noticeable."),
	CONFIG_GENERATE_JSON("generateJsonDescriptions", "false", "If true, apart from generating the images, it will take the N column of the .ods document of each card and create a JSON that the Card Descriptions Loader can read in TTS in order to apply each name and description to each card. To use this JSON file, you need my Card Descriptions Loader, that you can find in this link https://steamcommunity.com/sharedfiles/filedetails/?id=2899195933"),
	CONFIG_COPY_JSON("copyJsonToClipboard", "false", "If true, and if the JSON file is generated, it will be copied to the clipboard as well."),
	CONFIG_TYPE_IN_JSON("addTypeToNameInJson", "false", "If true, and if the JSON file is generated, the name of the cards will include the type of the card like \"HYPNOTIZE [Effect]\". Useful if during gameplay it is convenient to be able to search by type, for example if you would like players to see all Effects in their discard pile."),
	CONFIG_JSON_NUM_COPIES("jsonNumberOfCopiesInDesc", "false", "If true, and if the JSON file is generated, to every description a new line will be added that informs about the number of copies of that card in the deck. Values: \"true\" (add on all cards of all decks), \"Villain\" (only for the Villain deck), \"Fate\" (only for the Fate deck), \"false\" (disabled). If \"true\", it also affects all extra decks, but you cannot configure this for specific extra decks."),
	;
	
	/**
	 * @param key
	 * @return The ConfigValue representing this key or null if it doesn't exist.
	 */
	@Nullable
	public static ConfigValue getValueOfKey(String key) {
		for (ConfigValue cv : ConfigValue.values()) {
			if (cv.getKey().equals(key)) {
//				System.out.println("Ordinal "+cv.ordinal()+": "+cv.getKey());
				return cv;
			}
		}
		return null;
	}

	private String key, info, defaultValue;

	private ConfigValue(String key, String defaultValue, String info) {
		this.key = key;
		this.info = info;
		this.defaultValue = defaultValue;
	}

	public String getKey() {
		return key;
	}

	public String getInfo() {
		return info;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
	
	public String toString() {
		return key+": "+defaultValue;
	}
}
