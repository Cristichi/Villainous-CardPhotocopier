package es.cristichi.card_generator.obj;

import java.util.ArrayList;
import java.util.HashMap;

import es.cristichi.obj.CardInfo;
import es.cristichi.obj.ExtraDeckInfo;

public class GeneratorReturn {
	
	ArrayList<String> warnings;
	ArrayList<CardInfo> usefulCards;
	HashMap<String, ExtraDeckInfo> extraDecks;
	public GeneratorReturn(ArrayList<String> warnings, ArrayList<CardInfo> usefulCards,
			HashMap<String, ExtraDeckInfo> extraDecks) {
		this.warnings = warnings;
		this.usefulCards = usefulCards;
		this.extraDecks = extraDecks;
	}
	
	

}
