package es.cristichi.exceptions;

public class IllegalConfigValue extends ConfigurationException  {
	private static final long serialVersionUID = 1L;

	public IllegalConfigValue() {
		super();
	}

	public IllegalConfigValue(String message) {
		super(message);
	}
}