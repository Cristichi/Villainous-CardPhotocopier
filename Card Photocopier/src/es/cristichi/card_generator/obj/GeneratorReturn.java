package es.cristichi.card_generator.obj;

import java.util.ArrayList;

import es.cristichi.obj.CardInfo;

public class GeneratorReturn {
	
	public ArrayList<String> warnings;
	public ArrayList<CardInfo> usefulCards;
	public GeneratorReturn(ArrayList<String> warnings, ArrayList<CardInfo> usefulCards) {
		this.warnings = warnings;
		this.usefulCards = usefulCards;
	}
	
	

}
