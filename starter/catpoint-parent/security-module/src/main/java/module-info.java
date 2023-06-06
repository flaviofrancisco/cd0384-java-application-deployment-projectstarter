module com.udacity.catpoint.security {
    requires java.desktop;
    requires java.logging;
    requires guava;
    requires miglayout;
    requires com.google.gson;
    requires java.prefs;
    requires com.udacity.catpoint.image;
    opens com.udacity.security.data;
}