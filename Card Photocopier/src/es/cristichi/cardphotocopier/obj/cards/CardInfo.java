package es.cristichi.cardphotocopier.obj.cards;

import java.awt.image.BufferedImage;

public class CardInfo {
	
	public int copies;
	public String name, type, desc, deck;
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
