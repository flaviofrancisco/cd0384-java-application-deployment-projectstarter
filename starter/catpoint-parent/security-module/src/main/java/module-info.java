module security.module {
    requires java.desktop;
    requires java.logging;
    requires guava;
    requires image.module;
    requires miglayout;
    requires com.google.gson;
    requires java.prefs;
    exports com.udacity.security.data;
    exports com.udacity.security.application;
    exports com.udacity.security.service;
    opens com.udacity.security.data to com.google.gson;
}