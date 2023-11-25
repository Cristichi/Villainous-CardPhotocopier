package es.cristichi.obj;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ExtraDeckInfo {

	private int count;
	private Dimension grid;
	private BufferedImage image;
	private JSONObject jsonObject;
	private JSONArray jsonArrayCards;
	private int x = 0;
	private int y = 0;

	public ExtraDeckInfo() {
		count = 1;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Dimension getGrid() {
		return grid;
	}

	public void setGrid(Dimension grid) {
		this.grid = grid;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public Graphics getGraphics() {
		return image.getGraphics();
	}

	public void addCount(int quantity) {
		this.count += quantity;
	}

	public JSONObject getJsonObject() {
		return jsonObject;
	}

	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public JSONArray getJsonArrayCards() {
		return jsonArrayCards;
	}

	public void setJsonArrayCards(JSONArray jsonArrayCards) {
		this.jsonArrayCards = jsonArrayCards;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void addX(int quantity) {
		this.x += quantity;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void addY(int quantity) {
		this.y += quantity;
	}

}
