module image.module {
    requires java.desktop;
    requires java.logging;
    requires org.slf4j;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.services.rekognition;
    exports com.udacity.image.service;
    exports com.udacity.image.service.interfaces;
}