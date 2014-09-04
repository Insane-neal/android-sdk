package io.relayr.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import io.relayr.RelayrApp;

public final class RelayrProperties {

    private static final String PROPERTIES_FILE_NAME = "relayrsdk.properties";
    private static final String PROPERTIES_KEY_CLIENT_ID = "clientId";
    private static final String PROPERTIES_KEY_CLIENT_SECRET = "clientSecret";
    private static final String PROPERTIES_KEY_APP_ID = "appId";

    private static RelayrProperties mRelayrProperties = null;

	public final String clientId;
    public final String clientSecret;
    public final String appId;

    private RelayrProperties(String clientId, String clientSecret, String appId) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.appId = appId;
    }

    public static RelayrProperties get() {
        if (mRelayrProperties == null) {
            synchronized (RelayrProperties.class) {
                if (mRelayrProperties == null) {
                    mRelayrProperties = loadPropertiesFile();
                }
            }
        }
        return mRelayrProperties;
    }

    private static RelayrProperties loadPropertiesFile() {
        Properties properties = new Properties();

		try {
			InputStream inputStream = RelayrApp.get().getAssets().open(PROPERTIES_FILE_NAME);
			properties.load(inputStream);
            inputStream.close();
		} catch (IOException e) {
			//throw new Relayr_Exception("Can't find properties file");
		}

        String clientId = getProperty(properties.getProperty(PROPERTIES_KEY_CLIENT_ID));
        String clientSecret = getProperty(properties.getProperty(PROPERTIES_KEY_CLIENT_SECRET));
        String appId = getProperty(properties.getProperty(PROPERTIES_KEY_APP_ID));
        return new RelayrProperties(clientId, clientSecret, appId);
	}

    private static String getProperty(String property) {
        return property == null ? "": property;
    }

}
