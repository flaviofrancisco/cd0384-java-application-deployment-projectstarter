module catpoint.module {
    requires java.desktop;
    requires java.logging;
    requires transitive security.module;
    requires transitive image.module;
    exports com.udacity.catpoint.application;
    exports com.udacity.catpoint.data;
    exports com.udacity.catpoint.service;
}