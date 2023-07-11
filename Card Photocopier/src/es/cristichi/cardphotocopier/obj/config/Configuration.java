package es.cristichi.cardphotocopier.obj.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.StringTokenizer;

import es.cristichi.cardphotocopier.excep.ConfigValueNotFound;
import es.cristichi.cardphotocopier.excep.ConfigValueNotParsed;

public class Configuration extends File implements Cloneable {
	private static final long serialVersionUID = 115L;

	private String header;
	private LinkedHashMap<String, Object> settings;
	private LinkedHashMap<String, String> info;

	public Configuration(String file, String header) {
		super(file);
		settings = new LinkedHashMap<>();
		info = new LinkedHashMap<>();
		this.header = header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getHeader() {
		return header;
	}

	public void setValue(ConfigValue key, Object value) {
		settings.put(key.getKey(), value.toString());
	}

	public void setValueAndInfo(ConfigValue key, Object value) {
		settings.put(key.getKey(), value.toString());
		this.info.put(key.getKey(), key.getInfo());
	}

	public String getString(ConfigValue key, String defaultValue) {
		return settings.getOrDefault(key.getKey(), defaultValue).toString();
	}

	public String getString(ConfigValue key) throws ConfigValueNotFound {
		if (key == null)
			throw new ConfigValueNotFound("The key is null.");
		Object obj = settings.get(key.getKey());
		if (obj == null)
			throw new ConfigValueNotFound("The key \"" + key + "\" is not properly set in the config file. Check the config file.");
		String sol = obj.toString();
		return sol;
	}

	public int getInt(ConfigValue key, int defaultValue) {
		String str = settings.getOrDefault(key.getKey(), defaultValue + "").toString();
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

	public int getInt(ConfigValue key) throws ConfigValueNotFound, ConfigValueNotParsed {
		String str = settings.get(key.getKey()).toString();
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
	
	public double getDouble(ConfigValue key) throws ConfigValueNotParsed, ConfigValueNotFound {
		String str = settings.get(key.getKey()).toString();
		if (str == null)
			throw new ConfigValueNotFound("The key \"" + key + "\" was never set in the config file.");
		try {
			return Double.parseDouble(str.replace(",", "."));
		} catch (NumberFormatException e) {
			throw new ConfigValueNotParsed("Error trying to get double value from config file (Value \"" + str
					+ "\" could not be parsed to double)");
		}
	}

	public float getFloat(ConfigValue key, float defaultValue) {
		String str = settings.getOrDefault(key.getKey(), defaultValue + "").toString();
		try {
			return Float.parseFloat(str.replace(",", "."));
		} catch (NumberFormatException e) {
			System.err.println("Error trying to get double value from config file");
			System.err.println("(Value \"" + str + "\" could not be parsed to double)");
			return defaultValue;
		}
	}

	public boolean getBoolean(ConfigValue key, boolean defaultValue) {
		String str = settings.getOrDefault(key.getKey(), defaultValue + "").toString();
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

	public void setInfo(ConfigValue cv) {
		this.info.put(cv.getKey(), cv.getInfo());
	}

	/**
	 * It takes every value stored in memory and writes it in a new configuration file, overwriting any existing one.
	 * @throws IOException
	 * 
	 */
	public void saveToFile() throws IOException {
		String configTxt = header == null ? "" : "#\t" + header.replace("\n", "\n#\t") + "\n\n";
		Set<String> keys = settings.keySet();
		ArrayList<String> sortedKeys = new ArrayList<>(keys);
		Collections.sort(sortedKeys, new ConfigComparator());
		for (String key : sortedKeys) {
			if (ConfigValue.getValueOfKey(key) != null) {
				String value = settings.get(key).toString();
				String info = this.info.get(key);
				if (info != null) {
					configTxt += "#" + info + "\n";
				}
				configTxt += key + ": " + value + "\n\n";
			}
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
	 * @throws ConfigValueNotParsed 
	 * @throws IOException
	 * 
	 */
	public void readFromFile() throws ConfigValueNotParsed {
		int cont = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this));
			String line = null;
			System.out.println("line: "+(line==null?"null":line));
			while ((line = reader.readLine()) != null) {
				cont++;
				line = line.trim();
				if (!line.startsWith("#") && !line.trim().isEmpty()) {
					StringTokenizer st = new StringTokenizer(line, ":");
					String key = st.nextToken().trim();
					String value;

					if (st.countTokens() < 1) {
						reader.close();
						throw new ConfigValueNotParsed("Value for line "+cont+" \""+line+"\" in configuration could not be parsed.");
					} else {
						value = st.nextToken();
					}
					while (st.hasMoreElements()) {
						value+=":".concat(st.nextToken());
					}
					ConfigValue cv = ConfigValue.getValueOfKey(key);
					if (cv !=null)
						setValue(cv, value.trim());
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("Configuration file not created yet. Skipping load.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean contains(ConfigValue key) {
		return settings.containsKey(key.getKey());
	}

	public boolean contains(Object key) {
		return settings.containsKey(key);
	}
}
