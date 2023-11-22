package es.cristichi.obj;

import java.awt.image.BufferedImage;

public class CardInfo {

	public int copies;
	public String name, type, desc, deck, extraDeck, cost, strength, ability, activateAbility, activateCost, topRight,
			bottomRight, action, credits;
	public BufferedImage imageData;
	/**
	 * The row in the .ods file. Used to sort by .ods order.
	 */
	public int row;

	public CardInfo(BufferedImage imageData) {
		this.imageData = imageData;
	}

	@Override
	public String toString() {
		return String.format(
				"CardInfo [copies=%s, name=%s, type=%s, desc=%s, deck=%s, extraDeck=%s, cost=%s, strength=%s, ability=%s, activateAbility=%s, activateCost=%s, topRight=%s, bottomLeft=%s, action=%s, credits=%s, imageData=%s, row=%s]",
				copies, name, type, desc, deck, extraDeck, cost, strength, ability, activateAbility, activateCost,
				topRight, bottomRight, action, credits, imageData, row);
	}

}
