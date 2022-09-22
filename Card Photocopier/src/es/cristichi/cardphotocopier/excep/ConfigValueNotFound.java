package es.cristichi.cardphotocopier.excep;

public class ConfigValueNotFound extends ConfigurationException {
	private static final long serialVersionUID = 1L;

	public ConfigValueNotFound() {
		super();
	}

	public ConfigValueNotFound(String message) {
		super(message);
	}
}
