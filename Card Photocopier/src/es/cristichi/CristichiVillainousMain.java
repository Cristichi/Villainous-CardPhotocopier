package es.cristichi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import es.cristichi.card_generator.CardGenerator;
import es.cristichi.card_photocopier.CardPhotocopier;
import es.cristichi.card_photocopier.obj.ODS.OdsStructure;
import es.cristichi.exceptions.ConfigValueNotFound;
import es.cristichi.exceptions.ConfigurationException;
import es.cristichi.exceptions.IllegalConfigValue;
import es.cristichi.obj.config.ConfigValue;
import es.cristichi.obj.config.Configuration;

public class CristichiVillainousMain {

	private static String VERSION = "v2.7.3";
	private static String NAME = "Villainous Card Photocopier " + VERSION;

	private static String CONFIG_FILE = "config.yml";
	private static String ERROR_LOG_FILE = "CardPhotocopier error.log";

	public static String DOC_FIND_PATTERN_INIT = "$";

	public static void main(String[] args) {
		CristichiVillainousMain cvm = new CristichiVillainousMain();
		try {
			cvm.start();
		} catch (Exception e) {
			// If anything happens that makes the proccess unable to continue (things are
			// missing, wrong values in the .ods file, etc) then we just stop and tell
			// the user what happened. We also print in the console and a file just in case.
			System.err.println("Ended badly with error. Sadge.");
			e.printStackTrace();
			cvm.frame.replaceText(
					e.getMessage() == null ? e.toString() + ": See " + ERROR_LOG_FILE + " file." : e.getMessage());
			try {
				PrintStream ps = new PrintStream(ERROR_LOG_FILE);
				ps.println("Error in version " + VERSION);
				e.printStackTrace(ps);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				cvm.frame.replaceText("<div>" + e.getLocalizedMessage()
						+ "</div><div style=\"margin-top: 5px;\">An error occurred that prevented the error log to be created to a log file. "
						+ "Please screenshot the error now and save it if you need further assistance from Cristichi.</div>");
			}
		}
		
	}

	public MainInfoFrame frame;

	private void start() throws Exception {
		frame = new MainInfoFrame(NAME);

		frame.replaceText("Reading config file.");

		Configuration config = new Configuration(CONFIG_FILE,
				NAME + " configuration.\n For help, please contact Cristichi on Discord. I will be happy to help!");

		if (!config.exists()) {
			for (ConfigValue cValue : ConfigValue.values()) {
				config.setValueAndInfo(cValue, cValue.getDefaultValue());
			}

			config.saveToFile();

			frame.replaceText("Configuration file (" + CONFIG_FILE
					+ ") not found. We generated one for you, please close this window and edit it accordingly.");
		} else {
			String steps;
			Exception configError = null;
			ArrayList<String> warnings = new ArrayList<>(5);

			for (ConfigValue cValue : ConfigValue.values()) {
				config.setValueAndInfo(cValue, cValue.getDefaultValue());
			}

			config.readFromFile();

			steps = config.getString(ConfigValue.CONFIG_STEPS, ConfigValue.CONFIG_STEPS.getDefaultValue());

			if (!config.contains(ConfigValue.CONFIG_EMPTY_ROWS_TO_END)) {
				config.setValue(ConfigValue.CONFIG_EMPTY_ROWS_TO_END,
						ConfigValue.CONFIG_EMPTY_ROWS_TO_END.getDefaultValue());
			}

			if (!config.contains(ConfigValue.CONFIG_TYPE_ORDER)) {
				config.setValue(ConfigValue.CONFIG_TYPE_ORDER, ConfigValue.CONFIG_TYPE_ORDER.getDefaultValue());
			}

			if (!config.contains(ConfigValue.CONFIG_TYPE_IN_JSON)) {
				config.setValue(ConfigValue.CONFIG_TYPE_IN_JSON, ConfigValue.CONFIG_TYPE_IN_JSON.getDefaultValue());
			}

			if (!config.contains(ConfigValue.CONFIG_JSON_NUM_COPIES)) {
				config.setValue(ConfigValue.CONFIG_JSON_NUM_COPIES,
						ConfigValue.CONFIG_JSON_NUM_COPIES.getDefaultValue());
			}

			if (!config.contains(ConfigValue.CONFIG_COPY_JSON)) {
				config.setValue(ConfigValue.CONFIG_COPY_JSON, ConfigValue.CONFIG_COPY_JSON.getDefaultValue());
			}

			if (!config.contains(ConfigValue.CONFIG_RESULTS_QUALITY)) {
				config.setValue(ConfigValue.CONFIG_RESULTS_QUALITY, ConfigValue.CONFIG_RESULTS_QUALITY.getDefaultValue());
			}

			if (!config.contains(ConfigValue.CONFIG_GENERATOR_VERSION)) {
				config.setValue(ConfigValue.CONFIG_GENERATOR_VERSION,
						ConfigValue.CONFIG_GENERATOR_VERSION.getDefaultValue());
				configError = new ConfigValueNotFound(
						"You need to specify the version of the Card Generator that you are using in order to determine the layout of the .ods file.");
			}

			if (config.contains(ConfigValue.CONFIG_RESULTS_QUALITY)) {
				float quality = config.getFloat(ConfigValue.CONFIG_RESULTS_QUALITY, .9f);
				if (quality < 0 || quality > 1) {
					configError = new IllegalConfigValue(
							"The quality of the images must be between 0 (poorest quality) to 1 (best quality). Value was \""
									+ config.getString(ConfigValue.CONFIG_RESULTS_QUALITY, "???") + "\".");
				}
			} else {
				config.setValue(ConfigValue.CONFIG_RESULTS_QUALITY, ConfigValue.CONFIG_RESULTS_QUALITY.getDefaultValue());
			}

			config.saveToFile();
			
			File imagesFolder = new File(config.getString(ConfigValue.CONFIG_CARD_IMAGES));
			File resultsFolder = new File(config.getString(ConfigValue.CONFIG_RESULTS));

			File openDocumentFile = null;
			String configDoc = config.getString(ConfigValue.CONFIG_DOC);
			if (configDoc.startsWith(CristichiVillainousMain.DOC_FIND_PATTERN_INIT)) {
				frame.replaceText("Finding most recent .ods document from set pattern: "+configDoc);
				// Example of pattern:
				// cardsInfoOds: >C:/Users/(Windows User)/Villainous/Villain/^Villain( \(\d+\))?.ods$
				// This takes files like "Villain.ods"m "Villain (1).ods", "Villain (516).ods", etc

				configDoc = configDoc.substring(CristichiVillainousMain.DOC_FIND_PATTERN_INIT.length());

				String patternName = null;
				File configParent = null;
				if (configDoc.contains("/")) {
					patternName = configDoc.substring(configDoc.lastIndexOf('/') + 1);
					configParent = new File(configDoc.substring(0, configDoc.lastIndexOf('/')));
				} else if (configDoc.contains("\\")) {
					patternName = configDoc.substring(configDoc.lastIndexOf('\\') + 1);
					configParent = new File(configDoc.substring(0, configDoc.lastIndexOf('\\')));
				} else {
					patternName = configDoc;
					configParent = new File(".");
				}
				frame.replaceText("Looking for .ods documents with the given pattern.");

				FileTime old = null;
				Stream<Path> list = Files.list(configParent.toPath());
				for (Iterator<Path> iterator = list.iterator(); iterator.hasNext();) {
					Path path = iterator.next();
					File file = path.toFile();

					if (file.getName().toString().matches(patternName)) {
						FileTime newTime = Files.getLastModifiedTime(path);
						if (old == null || old.compareTo(newTime) < 0) {
							old = newTime;
							openDocumentFile = file;
						}
					}
				}
				list.close();

				if (openDocumentFile == null || !openDocumentFile.isFile()) {
					throw new ConfigurationException(
							"The configured pattern for the .ods document found no files. Pattern: \"" + patternName
									+ "\"");
				}
				if (old.toInstant().plus(20, ChronoUnit.HOURS).isBefore(Instant.now())) {
					warnings.add("Document chosen from pattern: \"" + openDocumentFile.getName() + "\". Last Modified: "
							+ old.toString());
				}

			} else {
				openDocumentFile = new File(config.getString(ConfigValue.CONFIG_DOC));
			}

			if (!imagesFolder.exists() && steps.contains("generate")) {
				imagesFolder.mkdirs();
			}
			
			if (!imagesFolder.exists()) {
				frame.replaceText("The folder where the already existing images for the cards are supposed to be ("
						+ imagesFolder.getAbsolutePath() + ") was not found. We need that one.");
				throw new FileNotFoundException(
						"The folder where the already existing images for the cards are supposed to be ("
								+ imagesFolder.getAbsolutePath() + ") was not found. Please edit the config file.");
			}
			if (!openDocumentFile.exists()) {
				frame.replaceText("The .ods document with the information for each card (" + openDocumentFile.getAbsolutePath()
						+ ") was not found. We need that one.");
				throw new FileNotFoundException("The .ods document with the information for each card ("
						+ openDocumentFile.getAbsolutePath() + ") was not found. Please edit the config file.");
			}

			OdsStructure odsStructure = new OdsStructure(config.getDouble(ConfigValue.CONFIG_GENERATOR_VERSION));
			SpreadSheet sheetDoc = SpreadSheet.createFromFile(openDocumentFile);
			Sheet sheet = sheetDoc.getFirstSheet();
			
			if (configError != null) {
				throw configError;
			} else {
				if (steps.contains("generate")) {
					CardGenerator cardGenerator = new CardGenerator();
					cardGenerator.generate(config, frame, openDocumentFile, imagesFolder, odsStructure, sheet);
				}
				if (steps.contains("photocopy")) {
					CardPhotocopier cardPhotocopier = new CardPhotocopier();
					warnings.addAll(cardPhotocopier.generate(config, frame, openDocumentFile, imagesFolder, resultsFolder, odsStructure, sheet));	
				}

				if (warnings.isEmpty()) {
					frame.dispose();
				} else {
					frame.replaceText("Process completed without errors but with some warnings:", warnings);
				}
			}
		}
	}
}
