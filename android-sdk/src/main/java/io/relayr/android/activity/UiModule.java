package io.relayr.android.activity;

import dagger.Module;

@Module(
        injects = {
                LoginActivity.class
        },
        complete = false,
        library = true
)
public class UiModule { }
