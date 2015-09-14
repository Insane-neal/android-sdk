package io.relayr.android.storage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static io.relayr.android.storage.RelayrProperties.DEFAULT_REDIRECT_URI;
import static io.relayr.android.storage.RelayrProperties.PROPERTIES_KEY_APP_ID;
import static io.relayr.android.storage.RelayrProperties.PROPERTIES_KEY_CLIENT_SECRET;
import static io.relayr.android.storage.RelayrProperties.PROPERTIES_KEY_REDIRECT_URI;
import static io.relayr.android.storage.RelayrProperties.loadPropertiesFile;

public class RelayrPropertiesTest {

    private Properties mProperties;

    @Before public void setUp() {
        mProperties = new Properties();
        mProperties.put(PROPERTIES_KEY_APP_ID, PROPERTIES_KEY_APP_ID);
        mProperties.put(PROPERTIES_KEY_CLIENT_SECRET, PROPERTIES_KEY_CLIENT_SECRET);
        mProperties.put(PROPERTIES_KEY_REDIRECT_URI, PROPERTIES_KEY_REDIRECT_URI);
    }

    @Test public void loadPropertiesFile_assertNotNull() {
        Assert.assertNotNull(loadPropertiesFile(mProperties));
    }

    @Test public void loadPropertiesFile_assertPropertiesExist() {
        io.relayr.android.storage.RelayrProperties properties = loadPropertiesFile(mProperties);
        Assert.assertEquals(properties.appId, PROPERTIES_KEY_APP_ID);
        Assert.assertEquals(properties.clientSecret, PROPERTIES_KEY_CLIENT_SECRET);
        Assert.assertEquals(properties.redirectUri, PROPERTIES_KEY_REDIRECT_URI);
    }

    @Test public void loadPropertiesFile_assertDefaultRedirectUriIsUsed() {
        io.relayr.android.storage.RelayrProperties properties = loadPropertiesFile(new Properties());
        Assert.assertEquals(properties.redirectUri, DEFAULT_REDIRECT_URI);
    }

}