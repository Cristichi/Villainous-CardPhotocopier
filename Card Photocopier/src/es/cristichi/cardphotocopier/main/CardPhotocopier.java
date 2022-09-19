package es.cristichi.cardphotocopier.main;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.jopendocument.dom.spreadsheet.Cell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import es.cristichi.cardphotocopier.obj.CardInfo;

/**
* Feel free to edit the code. Have an evil day!
@author Cristichi#5193
 */
public class CardPhotocopier {
	private static String CONFIG = "config.txt";
	
	private static ArrayList<String> problems;
	
	private static JFrame window;
	private static JLabel label;
	
	public static void main(String[] args){
		problems = new ArrayList<>(10);
		try {
			window = new JFrame("Villainous Card Photocopier");
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			label = new JLabel("Starting");
			label.setHorizontalAlignment(JLabel.CENTER);
			window.add(label);
			window.setMinimumSize(new Dimension(500, 200));
			window.setLocationRelativeTo(null);
			window.setAlwaysOnTop(true);
			window.setVisible(true);
			generate();
			label.setText("Done. Close this window to finish.");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Ended with error.");
			label.setText(e.getLocalizedMessage());
		} finally {
			System.out.println("Done.");
			if (problems.size()>0) {
				window.remove(label);
				window.setLayout(new GridLayout(problems.size()+1, 1));
				JLabel problemTitle = new JLabel("Process completed succesfully but with some weird things:");
				problemTitle.setBorder(new EmptyBorder(2, 5, 2, 5));
				window.add(problemTitle);
				for(String problem : problems) {
					JLabel lbl = new JLabel(problem);
					lbl.setBorder(new EmptyBorder(2, 5, 2, 5));
					window.add(lbl);
				}
				window.pack();
				window.setLocationRelativeTo(null);
			}
		}
	}

	public static void generate() throws Exception{
		boolean autoclose = false;
		
		label.setText("Reading config file.");
		File directory = new File("./");
		System.out.println("\n\nAll relative paths are relative to: "+directory.getAbsolutePath());

		File configFile = new File(CONFIG);
		if (!configFile.exists()){
			configFile.createNewFile();
			try (FileWriter fw = new FileWriter(CONFIG)){
				fw.write("This line is ignored. Write in the 2nd line the path to the cards folder (-Exports) with the generated images of the cards, on the 3rd line the path to the result, and on the 4th line the path to the .ods file with all the information on the cards. Remove the 5th line to let the window open after it's done (it won't close if there is an error).\n"
				+"../Villainous Card Generator V33.2/Villainous Card Generator V33_Data/-Exports\n"
				+"Results\n"
				+"../Villainous Card Generator V33.2/Villainous Card Generator V33_Data/-TextFiles/Villainous Template.ods\n"
				+ "Autoclose");
				fw.close();
				System.out.println("Successfully created config.txt. Please edit it.");
			} catch (IOException e) {
				System.out.println("An error occurred trying to write in the config file. Perhaps you need to give me permits?");
				e.printStackTrace();
			}
			window.setMinimumSize(new Dimension(800, 200));
			window.pack();
			window.setLocationRelativeTo(null);
			label.setText("Configuration file (config.txt) not found. We generated one for you, please close this window and edit it accordingly.");
			throw new FileNotFoundException("Configuration file (config.txt) not found. We generated one for you, please close this window and edit it.");
		}

		try(BufferedReader br = new BufferedReader(new FileReader(CONFIG))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			
			String[] everything = sb.toString().split(System.lineSeparator());
			if (everything.length < 4) {
				throw new IndexOutOfBoundsException("The config file is lacking some information. There should be at least 4 lines of text.");
			}
			
			File imagesFolder = new File(everything[1]);
			File resultsFolder = new File(everything[2]);
			File documentFile = new File(everything[3]);
			if (everything.length>4) {
				if (everything[4].toLowerCase().startsWith("autoclose")) {
					autoclose = true;
				}
			}
			
			if (!imagesFolder.exists()){
				window.setMinimumSize(new Dimension(800, 200));
				window.pack();
				window.setLocationRelativeTo(null);
				label.setText("The folder where the already existing images for the cards are supposed to be ("+imagesFolder.getAbsolutePath()+") was not found. We need that one.");
				throw new FileNotFoundException("The folder where the already existing images for the cards are supposed to be ("+imagesFolder.getAbsolutePath()+") was not found. We need that one.");
			}
			if (!documentFile.exists()){
				window.setMinimumSize(new Dimension(800, 200));
				window.pack();
				window.setLocationRelativeTo(null);
				label.setText("The .ods document with the information for each card ("+documentFile.getAbsolutePath()+") was not found. We need that one.");
				throw new FileNotFoundException("The .ods document with the information for each card ("+documentFile.getAbsolutePath()+") was not found. We need that one.");
			}

			label.setText("Reading card data from "+documentFile.getName()+".");
			
			SpreadSheet sheetDoc = SpreadSheet.createFromFile(documentFile);
			Sheet sheet = sheetDoc.getFirstSheet();

			HashMap<String, CardInfo> cardData = new HashMap<>(50);
			
			boolean forceFate = false;
			int done = 0;
			for (int i = 4; done <=20; i++) {
				Cell<SpreadSheet> A = sheet.getCellAt("A"+i);
				try {
					Cell<SpreadSheet> B = sheet.getCellAt("B"+i);
	//				Cell<SpreadSheet> C = sheet.getCellAt("C"+i);
	//				Cell<SpreadSheet> D = sheet.getCellAt("D"+i);
	//				Cell<SpreadSheet> E = sheet.getCellAt("E"+i);
	//				Cell<SpreadSheet> F = sheet.getCellAt("F"+i);
	//				Cell<SpreadSheet> G = sheet.getCellAt("G"+i);
	//				Cell<SpreadSheet> H = sheet.getCellAt("H"+i);
	//				Cell<SpreadSheet> I = sheet.getCellAt("I"+i);
	//				Cell<SpreadSheet> J = sheet.getCellAt("J"+i);
					Cell<SpreadSheet> K = sheet.getCellAt("K"+i);
	//				Cell<SpreadSheet> L = sheet.getCellAt("L"+i);
	//				Cell<SpreadSheet> M = sheet.getCellAt("M"+i);
					if (A.isEmpty() || B.isEmpty() || K.isEmpty()) {
						//Line skip
						if (A.isEmpty() && B.isEmpty() && K.isEmpty()) {
							//System.out.println("Line skipped: "+i+ " (empty)");
						} else {
							//TODO: add to problems, then at the end show problems. Also check for irregular number of cards at the end.
							if (!B.getTextValue().isBlank()) {
								problems.add("Detected error in card "+B.getTextValue()+". Number of copies or deck (Villain/Fate) was not filled.");
							}
							System.out.println("Line skipped: "+i + " \""+A.getTextValue()+"|"+B.getTextValue()+"|"+K.getTextValue()+"\"");	
						}
						done++;
					} else {
						done = 0;
						int deck = 0;
						if (K.getTextValue().equals("Fate") || K.getTextValue().equals("1") || forceFate) {
							deck = 1;
						}
						cardData.put(B.getTextValue(), new CardInfo(Integer.parseInt(A.getTextValue()), deck));
						System.out.println("Added card "+B.getTextValue()+": "+Integer.parseInt(A.getTextValue())+" copies in deck "+deck);
					}
				} catch (IllegalArgumentException e) {
					System.err.println(e.getLocalizedMessage());
					System.err.println("Line: "+A.getTextValue());
					done++;
					if (A.getTextValue().contains("- Fate -")) {
						forceFate = true;
						System.out.println("Forcing fate from now on");
					} else {
						forceFate = false;
						System.out.println("No longer forcing fate");
					}
				}
			}
			
			//System.out.println(cardsFromSheet.toString());
			label.setText("Photocopying the cards.");

			BufferedImage resultImageV = new BufferedImage(3720, 4400, BufferedImage.TYPE_INT_RGB);
			Graphics gV = resultImageV.getGraphics();
			BufferedImage resultImageF = new BufferedImage(3100, 2640, BufferedImage.TYPE_INT_RGB);
			Graphics gF = resultImageF.getGraphics();
			//Each ard is 620x880 (WxH)
			//Those numbers are calculated for 30 Villain cards and 15 Fate cards and won't allow MORE (but will allow less)
			
			int copiesToV = 0, copiesToF= 0;
			int xV = 0, yV = 0;
			int xF = 0, yF = 0;
			for (File cardFile : imagesFolder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
				}
			})){
				String name = cardFile.getName().substring(0, cardFile.getName().length() - (cardFile.getName().endsWith(".jpeg") ? 5 : 4));
				if (cardData.containsKey(name)){
					CardInfo info = cardData.get(name);
					label.setText("Loading "+name+"'s image data from it's file.");
					BufferedImage imageData = load(cardFile);
					for (int i = 0; i < info.copies; i++) {
						if (info.deck==0) {
							gV.drawImage(imageData, xV, yV, null);
							xV+=620;
							if (xV >= resultImageV.getWidth()) {
								xV = 0;
								yV+=880;
							}
							copiesToV++;
						} else {
							gF.drawImage(imageData, xF, yF, null);
							xF+=620;
							if (xF >= resultImageF.getWidth()) {
								xF = 0;
								yF+=880;
							}
							copiesToF++;
						}
					}
				} else {
					System.err.println("!!!!!!! Data does not contain image \"" + name + "\" !!!!!!!");
				}
			}

			if (copiesToV != 30) {
				problems.add("Detected error number of copies to Vilain deck. Expected was 30 but it was \""+copiesToV+"\".");
			}
			if (copiesToF != 15) {
				problems.add("Detected error number of copies to Fate deck. Expected was 15 but it was \""+copiesToF+"\".");
			}
			

			label.setText("Creating the image for TTS decks.");

			File villainDeck = new File(resultsFolder, "Villain Deck.jpg");
			File fateDeck = new File(resultsFolder, "Fate Deck.jpg");

			if (villainDeck.exists()) {
				villainDeck.delete();
			}
			if (fateDeck.exists()) {
				fateDeck.delete();
			}
			
			ImageIO.write(resultImageV, "jpg", villainDeck);
			ImageIO.write(resultImageF, "jpg", fateDeck);
			
			label.setText("Removing Herobrine.");
			
			if (autoclose && problems.isEmpty()) {
				System.out.println("Autoclose goes brr");
				label.setText("Done. Autoclosing.");
				Thread.sleep(500);
				System.exit(0);
			}
		}
	}
	
	private static BufferedImage load(File f) throws IOException{
	    byte[] bytes = Files.readAllBytes(f.toPath());
	    try (InputStream is = new ByteArrayInputStream(bytes)){
	        return ImageIO.read(is);
	    }
	}
}