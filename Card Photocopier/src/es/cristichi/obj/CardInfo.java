package es.cristichi.obj;

import java.awt.image.BufferedImage;

public class CardInfo {
	
	public int copies;
	public String name, type, desc, deck, cost, strength, ability, activateAbility, activateCost, topRight, bottomLeft, action, credits;
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
		return String.format("CardInfo [copies=%s, name=%s, type=%s, desc=%s, deck=%s, imageData=%s]", copies, name,
				type, desc, deck, imageData);
	}
	
	
}
