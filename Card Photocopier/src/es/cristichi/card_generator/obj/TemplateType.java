package es.cristichi.card_generator.obj;

public enum TemplateType {
	DECK("Deck"), COST("Cost"), TOP_RIGHT("TopRightElement"), STRENGTH("Strength"), BOTTOM_RIGHT("BottomRightElement");
	
	/**
	 * bear in mind that this is not the final File name, it should have the form DECKTYPE + fileName + ".jpg" where DECKTYPE is "Villain", "Fate" or the name of the extra dekc it belongs to.
	 */
	private String fileName;

	private TemplateType(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}
}
