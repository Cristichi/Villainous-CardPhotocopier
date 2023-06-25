package es.cristichi.cardphotocopier.obj.ODS;

import java.util.HashMap;

public class Structure extends HashMap<Column, String> {

	private static final long serialVersionUID = -1675080188225802319L;

	public Structure(double version) {
		if (version < 35) {
			// Old .ods format
			super.put(Column.COPIES_COUNT, "A");
			super.put(Column.NAME, "B");
			super.put(Column.COST, "C");
			super.put(Column.STRENGTH, "D");
			super.put(Column.EFFECT, "E");
			super.put(Column.TYPE, "F");
			super.put(Column.ACTIVATE_EFFECT, "G");
			super.put(Column.ACTIVATE_COST, "H");
			super.put(Column.TOP_RIGHT, "I");
			super.put(Column.BOTTOM_RIGHT, "J");
			super.put(Column.DECK, "K");
			super.put(Column.ACTION_SYMBOL, "L");
			super.put(Column.AUTO_LAYOUT, "M");
			super.put(Column.DESCRIPTION, "N");
			super.put(Column.EXTRA_DECK, "O");
			
		} else {
			// New .ods format
			super.put(Column.COPIES_COUNT, "A");
			super.put(Column.NAME, "B");
			super.put(Column.COST, "C");
			super.put(Column.STRENGTH, "D");
			super.put(Column.EFFECT, "E");
			super.put(Column.TYPE, "F");
			super.put(Column.ACTIVATE_EFFECT, "G");
			super.put(Column.ACTIVATE_COST, "H");
			super.put(Column.TOP_RIGHT, "I");
			super.put(Column.BOTTOM_RIGHT, "J");
			super.put(Column.CREDITS, "K");
			super.put(Column.DECK, "L");
			super.put(Column.ACTION_SYMBOL, "M");
			super.put(Column.DESCRIPTION, "N");
			super.put(Column.EXTRA_DECK, "O");
		}
		
		// System.out.println("Using Structure: "+toString());
	}
	
	@Override
	public String put(Column key, String value) {
		throw new UnsupportedOperationException("You cannot edit the Structure of the .ods file manually.");
	}
	
	
}
