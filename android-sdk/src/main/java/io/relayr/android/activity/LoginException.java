package io.relayr.android.activity;

public class LoginException extends Exception {

    protected static final String UNKNOWN_EXCEPTION = "Unknown exception!";
    protected static final String LOGIN_PERMISSION_EXCEPTION = "Relayr login permission missing exception";
    protected static final String LOGIN_CONNECTIVITY_EXCEPTION = "Internet connection not available.";
    protected static final String LOGIN_PLATFORM_EXCEPTION = "Platform not reachable.";

    public LoginException() {
        super(UNKNOWN_EXCEPTION);
    }

    public LoginException(String message) {
        super(message);
    }

    public LoginException(Throwable t) {
        super(UNKNOWN_EXCEPTION, t);
    }

    public static LoginPermissionException permissionException() {
        return new LoginPermissionException();
    }

    public static LoginException connectivityException() {return new LoginConnectivityException();}

    public static LoginException platformException() {return new LoginPlatformException();}
}
