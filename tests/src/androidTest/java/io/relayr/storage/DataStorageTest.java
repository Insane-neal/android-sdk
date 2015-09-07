package io.relayr.storage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import io.relayr.RelayrSdk;
import io.relayr.TestEnvironment;

import static org.fest.assertions.api.Assertions.assertThat;

public class DataStorageTest extends TestEnvironment {

    @Before
    public void init() {
        super.init();
        DataStorage.logOut();
    }

    @Test
    public void storageSaveTest() {
        DataStorage.saveUserId("user");

        assertThat(DataStorage.getUserId()).isNotNull();
        assertThat(DataStorage.getUserId()).isEqualTo("user");
    }

    @Test
    public void storageLogInTest() {
        DataStorage.saveUserId("user");
        assertThat(DataStorage.isUserLoggedIn()).isFalse();

        DataStorage.saveUserToken("token");
        assertThat(DataStorage.isUserLoggedIn()).isTrue();
    }
}
