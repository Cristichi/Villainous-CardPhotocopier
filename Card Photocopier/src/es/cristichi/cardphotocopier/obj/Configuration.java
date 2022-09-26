package es.cristichi.cardphotocopier.obj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import es.cristichi.cardphotocopier.excep.ConfigValueNotFound;
import es.cristichi.cardphotocopier.excep.ConfigValueNotParsed;

public class Configuration extends File implements Cloneable {
	private static final long serialVersionUID = 115L;

	private String header;
	private Map<String, Object> settings;
	private Map<String, String> info;

	public Configuration(String file, String header) {
		super(file);
		settings = new HashMap<>();
		info = new HashMap<>();
		this.header = header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getHeader() {
		return header;
	}

	public void setValue(String key, Object value) {
		settings.put(key, value.toString());
	}

	public void setValue(String key, Object value, String info) {
		settings.put(key, value.toString());
		this.info.put(key, info);
	}

	public String getString(String key, String defaultValue) {
		return settings.getOrDefault(key, defaultValue).toString();
	}

	public String getString(String key) throws ConfigValueNotFound {
		String sol = settings.get(key).toString();
		if (sol == null)
			throw new ConfigValueNotFound("The key \"" + key + "\" was never set in the config file.");
		return sol;
	}

	public int getInt(String key, int defaultValue) {
		String str = settings.getOrDefault(key, defaultValue + "").toString();
		try {
			Double num = null;
			try {
				num = Double.parseDouble(str);
				return num.intValue();
			} catch (NumberFormatException e) {
				return Integer.parseInt(str);
			}
		} catch (NumberFormatException e) {
			System.err.println("Error trying to get integer value from config file");
			System.err.println("(Value \"" + str + "\" could not be parsed to integer)");
			return defaultValue;
		}
	}

	public int getInt(String key) throws ConfigValueNotFound, ConfigValueNotParsed {
		String str = settings.get(key).toString();
		if (str == null)
			throw new ConfigValueNotFound("The key \"" + key + "\" is not set in the config file.");
		try {
			Double num = null;
			try {
				num = Double.parseDouble(str);
				return num.intValue();
			} catch (NumberFormatException e) {
				return Integer.parseInt(str);
			}
		} catch (NumberFormatException e) {
			throw new ConfigValueNotParsed("Error trying to get integer value from config file (Value \"" + str
					+ "\" could not be parsed to integer)");
		}
	}

	public double getDouble(String key, double defaultValue) {
		String str = settings.getOrDefault(key, defaultValue + "").toString();
		try {
			return Double.parseDouble(str.replace(",", "."));
		} catch (NumberFormatException e) {
			System.err.println("Error trying to get double value from config file");
			System.err.println("(Value \"" + str + "\" could not be parsed to double)");
			return defaultValue;
		}
	}

	public double getDouble(String key) throws ConfigValueNotParsed, ConfigValueNotFound {
		String str = settings.get(key).toString();
		if (str == null)
			throw new ConfigValueNotFound("The key \"" + key + "\" was never set in the config file.");
		try {
			return Double.parseDouble(str.replace(",", "."));
		} catch (NumberFormatException e) {
			throw new ConfigValueNotParsed("Error trying to get double value from config file (Value \"" + str
					+ "\" could not be parsed to double)");
		}
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		String str = settings.getOrDefault(key, defaultValue + "").toString();
		switch (str) {
		case "true":
		case "yes":
			return true;
		case "false":
		case "no":
			return false;

		default:
			System.err.println("Error trying to get boolean value from config file");
			System.err.println("(Value \"" + str + "\" could not be parsed to boolean)");
			return defaultValue;
		}
	}

	public boolean getBoolean(String key) throws ConfigValueNotFound, ConfigValueNotParsed {
		Object obj = settings.get(key);
		if (obj == null)
			throw new ConfigValueNotFound("The field \"" + key + "\" is missing in the config file.");
		String str = settings.get(key).toString();
		if (str == null)
			throw new ConfigValueNotFound("The field \"" + key + "\" is missing in the config file.");
		switch (str) {
		case "true":
		case "yes":
			return true;
		case "false":
		case "no":
			return false;

		default:
			throw new ConfigValueNotParsed("Error trying to get boolean value from config file (Value \"" + str
					+ "\" is not a valid boolean, like \"true\", \"false\", \"yes\" or \"no\")");
		}
	}

	public void setInfo(String key, String info) {
		this.info.put(key, info);
	}

	/**
	 * It takes every value stored in memory and writes it in a new configuration file, overwriting any existing one.
	 * @throws IOException
	 * 
	 */
	public void saveConfig() throws IOException {
		String configTxt = header == null ? "" : "#\t" + header.replace("\n", "\n#\t") + "\n\n";
		Set<String> keys = settings.keySet();
		for (String key : keys) {
			String value = settings.get(key).toString();
			String info = this.info.get(key);
			if (info != null) {
				configTxt += "#" + info + "\n";
			}
			configTxt += key + ": " + value + "\n\n";
		}

		if (exists()) {
			delete();
		}
		try {
			getParentFile().mkdirs();
		} catch (NullPointerException e) {
		}
		createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(this));
		writer.write(configTxt);
		writer.close();
	}

	/**
	 * @throws IOException
	 * 
	 */
	public void reloadConfigFromFile() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this));
			String line;
			int cont = 0;
			while ((line = reader.readLine()) != null) {
				cont++;
				line = line.trim();
				if (!line.startsWith("#") && !line.trim().isEmpty()) {
					StringTokenizer st = new StringTokenizer(line, ":");
					if (st.countTokens() < 2) {
						reader.close();
						throw new IOException("Looks like the file content is not correct. Broken line " + cont + " ("
								+ st.countTokens() + " tokens, should be 2)");
					}
					String key = st.nextToken().trim();
					String value = st.nextToken();
					while (st.hasMoreElements()) {
						value+=":".concat(st.nextToken());
					}
					setValue(key, value.trim());
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("Configuration file not created yet. Skipping load.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
