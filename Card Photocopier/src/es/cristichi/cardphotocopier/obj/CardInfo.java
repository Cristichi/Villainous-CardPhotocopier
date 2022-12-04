package es.cristichi.cardphotocopier.obj;

import java.awt.image.BufferedImage;

public class CardInfo {
	
	public int copies, deck;
	public String name, type, desc;
	public BufferedImage imageData;
	
	
	public CardInfo(BufferedImage imageData) {
		this.imageData = imageData;
	}
	
}
