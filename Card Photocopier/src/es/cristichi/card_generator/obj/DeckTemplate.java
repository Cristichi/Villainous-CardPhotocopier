package es.cristichi.card_generator.obj;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import es.cristichi.obj.Util;

public class DeckTemplate {
	private String name;

	public DeckTemplate(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public BufferedImage getTemplateFile(File folder, TemplateType templateType) throws IOException {
		File ret = new File(folder, name+templateType.getFileName()+".png");
		if (!ret.exists()) {
			throw new FileNotFoundException("A template file is missing ("+ret.getAbsolutePath()+")");
		}
		return Util.load(ret);
	}
}
