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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.jopendocument.dom.spreadsheet.Cell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import es.cristichi.cardphotocopier.obj.CardInfo;
import es.cristichi.cardphotocopier.obj.Range;

/**
* Feel free to edit the code. Have an evil day!
@author Cristichi#5193
 */
public class CardPhotocopier {
	private static String CONFIG = "config.txt";
	private static int VILLAIN_DECK_SIZE = 30;
	private static int FATE_DECK_SIZE = 15;
	private static HashMap<Range, Dimension> DECK_SIZES;
	static {
		DECK_SIZES = new HashMap<>(18);
		DECK_SIZES.put(new Range(1, 1), new Dimension(1, 1));
		DECK_SIZES.put(new Range(2, 2), new Dimension(2, 1));
		DECK_SIZES.put(new Range(3, 3), new Dimension(3, 1));
		DECK_SIZES.put(new Range(4, 4), new Dimension(2, 2));
		DECK_SIZES.put(new Range(5, 6), new Dimension(3, 2));
		DECK_SIZES.put(new Range(7, 8), new Dimension(4, 2));
		DECK_SIZES.put(new Range(9, 9), new Dimension(3, 3));
		DECK_SIZES.put(new Range(10, 12), new Dimension(4, 3));
		DECK_SIZES.put(new Range(13, 15), new Dimension(5, 3));
		DECK_SIZES.put(new Range(16, 20), new Dimension(5, 4));
		DECK_SIZES.put(new Range(21, 24), new Dimension(6, 4));
		DECK_SIZES.put(new Range(25, 25), new Dimension(5, 5));
		DECK_SIZES.put(new Range(26, 30), new Dimension(6, 5));
		DECK_SIZES.put(new Range(31, 35), new Dimension(7, 5));
		DECK_SIZES.put(new Range(36, 36), new Dimension(6, 6));
		DECK_SIZES.put(new Range(37, 42), new Dimension(7, 6));
		DECK_SIZES.put(new Range(43, 48), new Dimension(7, 6));
	}
	
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
		//File directory = new File("./");
		//System.out.println("\n\nAll relative paths are relative to: "+directory.getAbsolutePath());

		
		//TODO: Completely rework the config file to be .yml. Way easier to use.
		File configFile = new File(CONFIG);
		if (!configFile.exists()){
			configFile.createNewFile();
			try (FileWriter fw = new FileWriter(CONFIG)){
				fw.write("This line is ignored. Write in the 2nd line the path to the cards folder (-Exports) with the generated images of the cards, on the 3rd line the path to the result, and on the 4th line the path to the .ods file with all the information on the cards. Remove the 5th line to let the window open after it's done (it won't close if there is an error).\n"
				+"../Villainous Card Generator V33.2/Villainous Card Generator V33_Data/-Exports\n"
				+"Results\n"
				+"../Villainous Card Generator V33.2/Villainous Card Generator V33_Data/-TextFiles/Villainous Template.ods\n"
				+ "Autoclose\n"
				+ "");
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

			// First we read every .png, .jpg and .jpeg file.
			
			HashMap<String, CardInfo> cardsInfo = new HashMap<>(50);
			
			for (File cardFile : imagesFolder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
				}
			})){
				String name = cardFile.getName().substring(0, cardFile.getName().length() - (cardFile.getName().endsWith(".jpeg") ? 5 : 4));
				label.setText("Loading "+name+"'s image data from it's file.");
				CardInfo info = new CardInfo(load(cardFile));
				if (info.imageData == null) {
					System.err.println("Image "+cardFile+" could not be loaded.");
					problems.add("Image \""+cardFile.getName()+"\" could not be loaded.");
				} else {
					cardsInfo.put(name, info);	
				}
			}
			
			int copiesToV = 0, copiesToF= 0;
			int xV = 0, yV = 0;
			int xF = 0, yF = 0;
			
			boolean forceFate = false;
			int done = 0;
			//We are going to look into each row in the .ods and check if it's a card that exists withing the images folder and draw it into it's corresponding deck.
			//TODO: do the loop twice, once for reading everything so we have the number of copies of each ddeck, then factorize those numbers to create the proper size for the images, then do the painting. 
			for (int row = 4; done <=20; row++) {
				Cell<SpreadSheet> A = sheet.getCellAt("A"+row);
				try {
					Cell<SpreadSheet> B = sheet.getCellAt("B"+row);
					Cell<SpreadSheet> C = sheet.getCellAt("C"+row);
					Cell<SpreadSheet> D = sheet.getCellAt("D"+row);
					Cell<SpreadSheet> E = sheet.getCellAt("E"+row);
					Cell<SpreadSheet> F = sheet.getCellAt("F"+row);
					Cell<SpreadSheet> G = sheet.getCellAt("G"+row);
					Cell<SpreadSheet> H = sheet.getCellAt("H"+row);
					Cell<SpreadSheet> I = sheet.getCellAt("I"+row);
					Cell<SpreadSheet> J = sheet.getCellAt("J"+row);
					Cell<SpreadSheet> K = sheet.getCellAt("K"+row);
					Cell<SpreadSheet> L = sheet.getCellAt("L"+row);
					Cell<SpreadSheet> M = sheet.getCellAt("M"+row);

					done++;
					if (!B.isEmpty()) {
						if (cardsInfo.containsKey(B.getTextValue())) {
							CardInfo ci = cardsInfo.get(B.getTextValue());
							
							if (A.isEmpty() || K.isEmpty()
									&& (!C.isEmpty() || !D.isEmpty() || !E.isEmpty() || !F.isEmpty() || 
											!G.isEmpty() || !H.isEmpty() || !I.isEmpty() || !J.isEmpty() || 
											!L.isEmpty() || !M.isEmpty())
								) {
								problems.add("Detected error in card "+B.getTextValue()+"."
										+(A.getTextValue().isBlank() ? " Number of copies (Column A) not filled.":"")
										+(K.getTextValue().isBlank() ? " Villain/Fate deck (Column K) not filled.":"")
										);
								System.err.println("Error reading: "+row+ " (Card "+B.getTextValue()+" not proper)");
							} else {
								done = 0;

								ci.copies = Integer.parseInt(A.getTextValue());
								
								ci.deck = 0;
								if (K.getTextValue().equals("Fate") || K.getTextValue().equals("1") || forceFate) {
									ci.deck = 1;
									copiesToF+=ci.copies;
								} else {
									copiesToV+=ci.copies;
								}
							}
						}
					}
				} catch (IllegalArgumentException e) {
					//This probably means that some columns are combined, so we know it's not a card.
					System.err.println(e.getLocalizedMessage());
					System.err.println("Line: "+A.getTextValue());
					done++;
					//If the column A contains "- Fate -", it's Fate forcing time. This allows villains that need to generate Fate cards as Villain cards with
					//a different layout to still tell my tool which cards are Fate. Read the Usage guide to know how.
					if (A.getTextValue().contains("- Fate -")) {
						forceFate = true;
						System.out.println("Detected \"- Fate -\". Forcing fate from now on");
					} else {
						forceFate = false;
						System.out.println("Interpreted as end of force Fate. No longer forcing fate");
					}
				}
			}

			Dimension gridV = getGrid(copiesToV);
			Dimension gridF = getGrid(copiesToF);	
			
			//This is the data of the two images. We now create it empty (black) and we'll draw each card over it.
			int cardW = 620, cardH = 880;

//			BufferedImage resultImageV = new BufferedImage(3720, 4400, BufferedImage.TYPE_INT_RGB);
			BufferedImage resultImageV = new BufferedImage(cardW*gridV.width, cardH*gridV.height, BufferedImage.TYPE_INT_RGB);
			Graphics gV = resultImageV.getGraphics();
//			BufferedImage resultImageF = new BufferedImage(3100, 2640, BufferedImage.TYPE_INT_RGB);
			BufferedImage resultImageF = new BufferedImage(cardW*gridF.width, cardH*gridF.height, BufferedImage.TYPE_INT_RGB);
			Graphics gF = resultImageF.getGraphics();
			//Each ard is 620x880 (WxH)
			//Those numbers are calculated for 30 Villain cards and 15 Fate cards and won't allow MORE (but will allow less)
			//It will resize, growing always higher and never wider, if there are more than 30 Villain/15 Fate.
			//It's not quite optimal, ngl but it gets the work done and it's exactly the same if the number is expected or lower.
			//Solution: because it is expected that each villain has its own config, maybe it could be configured in the config file the number of cards.
			
			for(String cardName : cardsInfo.keySet()) {
				CardInfo ci = cardsInfo.get(cardName);
				
				System.out.println("Photocopying card "+cardName+": "+ci.copies+" copies in deck "+ci.deck);
				label.setText("Photocopying card "+cardName+": "+ci.copies+" copies in "+(ci.deck==0?"Villain":"Fate")+" deck.");
				
				for (int i = 0; i < ci.copies; i++) {
					if (ci.deck==0) {
						gV.drawImage(ci.imageData, xV, yV, null);
						xV+=620;
						if (xV >= resultImageV.getWidth()) {
							xV = 0;
							yV+=880;
						}
					} else {
						gF.drawImage(ci.imageData, xF, yF, null);
						xF+=620;
						if (xF >= resultImageF.getWidth()) {
							xF = 0;
							yF+=880;
						}
					}
				}
			}
			
			label.setText("Removing Herobrine.");

			//If the number of copies is not the expected, we notify the user in case they forgot to save their .ods after some changes.
			if (copiesToV != VILLAIN_DECK_SIZE) {
				problems.add("Unexpected number of copies to Vilain deck. Expected was "+VILLAIN_DECK_SIZE+" but it was \""+copiesToV+"\".");
			}
			if (copiesToF != FATE_DECK_SIZE) {
				problems.add("Unexpected error number of copies to Fate deck. Expected was "+FATE_DECK_SIZE+" but it was \""+copiesToF+"\".");
			}
			
			label.setText("Writing the images for TTS decks.");

			if (!resultsFolder.exists()) {
				resultsFolder.mkdir();
			}
			
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
	
	private static Dimension getGrid(int quantity) {
		for (Range index : DECK_SIZES.keySet()) {
			if (index.inRange(quantity)) {
				return DECK_SIZES.get(index);
			}
		}
		List<Integer> divisors = getDivisors(quantity);
		divisors.add(1);
		divisors.add(quantity);
		Collections.sort(divisors);
		double sqrt = Math.sqrt(quantity);
		List<Integer> sol;
		if (divisors.size()==2) {
			sol = divisors;
		} else {
			sol = findKClosestElements(divisors, 2, ((int) Math.round(sqrt)));
		}
		System.out.println("Grid for "+quantity+": "+sol);
		System.out.println("Divisors: "+divisors+" sqrt: "+sqrt);
		return new Dimension(sol.get(1), sol.get(0));
	}
	
	private static ArrayList<Integer> getDivisors(int n){
		ArrayList<Integer> divisors = new ArrayList<>();
		for (int i = 2; i * i <= n; ++i)
			if (n % i == 0) {
				divisors.add(i);
				if (i != n / i)
					divisors.add(n / i);
			}
		return divisors;
    }
	
	private static List<Integer> findKClosestElements(List<Integer> input, int k, int target) {
		int i = Collections.binarySearch(input, target);
		if (i < 0) {
			i = -(i + 1);
		}

		int left = i - 1;
		int right = i;

		while (k-- > 0) {
			if (left < 0 || (right < input.size()
					&& Math.abs(input.get(left) - target) > Math.abs(input.get(right) - target))) {
				right++;
			} else {
				left--;
			}
		}

		return input.subList(left + 1, right);
	}
}