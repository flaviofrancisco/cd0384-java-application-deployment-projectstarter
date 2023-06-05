package com.udacity.security.service;

import com.udacity.image.service.interfaces.ImageService;
import com.udacity.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    private SecurityService securityService;

    @Mock
    SecurityRepository securityRepository;

    @Mock
    ImageService imageService;

    @BeforeEach
    public void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ARMED_HOME", "ARMED_AWAY"})
    @DisplayName("1. If alarm is armed and a sensor becomes activated, put the system into pending alarm status.")
    public void setAlarmStatus_whenArmedAndSensorActivated_thenPendingAlarm(String armingStatusStr ) {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.valueOf(armingStatusStr));
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        Sensor sensor = new Sensor("sensor", SensorType.DOOR);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ARMED_HOME", "ARMED_AWAY"})
    @DisplayName("2. If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm on.\n" +
                 "[This is the case where all sensors are deactivated and then one gets activated]")
    public void setAlarmStatus_whenArmedAndSensorActivatedAndPendingAlarm_thenAlarm(String armingStatusStr) {

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.valueOf(armingStatusStr));
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        Sensor sensor = new Sensor("sensor", SensorType.DOOR);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    @DisplayName("3. If pending alarm and all sensors are inactive, return to no alarm state.")
    public void setAlarmStatus_whenPendingAlarmAndAllSensorsDeactivated_thenNoAlarm() {

        Set<Sensor> sensors = Set.of(new Sensor("sensor1", SensorType.DOOR),
                                     new Sensor("sensor2", SensorType.DOOR),
                                     new Sensor("sensor3", SensorType.DOOR));

        when(securityRepository.getSensors()).thenReturn(sensors);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.checkSensors();
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @DisplayName("4. If alarm is active, change in sensor state should not affect the alarm state.")
    @ValueSource(booleans = {true, false})
    public void setAlarmStatus_whenAlarmAndSensorActivated_thenAlarm(boolean active) {

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        Set<Sensor> sensors = Set.of(new Sensor("sensor1", SensorType.DOOR),
                                     new Sensor("sensor2", SensorType.DOOR));

        for(Sensor sensor : sensors) {
            securityService.changeSensorActivationStatus(sensor, true);
        }

        // Gets first sensor in set
        Sensor sensor = sensors.iterator().next();
        securityService.changeSensorActivationStatus(sensor, active);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test
    @DisplayName("5. If a sensor is activated while already active and the system is in pending state, change it to alarm state. " +
                 "[This is the case where one sensor is already active and then another gets activated]")
    public void setAlarmStatus_whenSensorActivatedAndPendingAlarm_thenAlarm() {

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        Sensor sensor = new Sensor("sensor1", SensorType.DOOR);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, true);

        sensor = new Sensor("sensor2", SensorType.DOOR);
        sensor.setActive(false);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    @DisplayName("6. If a sensor is deactivated while already inactive, make no changes to the alarm state.")
    public void setAlarmStatus_whenSensorDeactivatedAndNoAlarm_thenNoAlarm() {
        securityService.changeSensorActivationStatus(new Sensor("sensor", SensorType.DOOR), false);
        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    @DisplayName("7. If the camera image contains a cat while the system is armed-home, put the system into alarm status.")
    public void setAlarmStatus_whenCameraImageContainsCatAndArmedHome_thenAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        BufferedImage image = mock(BufferedImage.class);
        when(imageService.imageContainsCat(image, 50.0f)).thenReturn(true);
        securityService.processImage(image);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    @DisplayName("8. If the camera image does not contain a cat, change the status to no alarm as long as the sensors are not active.")
    public void setAlarmStatus_whenCameraImageDoesNotContainCatAndSensorsNotActive_thenNoAlarm() {

        when(securityRepository.getSensors()).thenReturn(
                Set.of(new Sensor("sensor1", SensorType.DOOR),
                new Sensor("sensor2", SensorType.DOOR))
        );

        BufferedImage image = mock(BufferedImage.class);
        when(imageService.imageContainsCat(image, 50.0f)).thenReturn(false);
        securityService.processImage(image);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    @DisplayName("9. If the system is disarmed, set the status to no alarm.")
    public void setAlarmStatus_whenDisarmed_thenNoAlarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ARMED_HOME", "ARMED_AWAY"})
    @DisplayName("10. If the system is armed, reset all sensors to inactive.")
    public void setAlarmStatus_whenArmed_thenResetSensors(String armingStatusStr) {

        Set<Sensor> sensors = new HashSet<>();

        sensors.add(new Sensor("sensor1", SensorType.DOOR));
        sensors.add(new Sensor("sensor2", SensorType.DOOR));
        sensors.add(new Sensor("sensor3", SensorType.DOOR));

        for (Sensor sensor : sensors) {
            sensor.setActive(true);
        }

        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.setArmingStatus(ArmingStatus.valueOf(armingStatusStr));

        for (Sensor sensor : sensors) {
            assertFalse(sensor.getActive());
        }

    }

    @ParameterizedTest
    @ValueSource(strings = {"DISARMED", "ARMED_AWAY", "ARMED_HOME"})
    @DisplayName("11. If the system is armed-home while the camera shows a cat, set the alarm status to alarm.")
    public void setAlarmStatus_whenArmedHomeAndCameraShowsCat_thenAlarm(String armingStatus) {

        ArmingStatus armingStatusEnum = ArmingStatus.valueOf(armingStatus);
        when(securityRepository.getArmingStatus()).thenReturn(armingStatusEnum);


        Set<Sensor> sensors = new HashSet<>();
        sensors.add(new Sensor("sensor1", SensorType.DOOR));
        sensors.add(new Sensor("sensor2", SensorType.DOOR));
        for(Sensor sensor : sensors) {
            sensor.setActive(true);
        }
        when(securityRepository.getSensors()).thenReturn(sensors);

        BufferedImage image = mock(BufferedImage.class);
        when(imageService.imageContainsCat(image, 50.0f)).thenReturn(true);
        securityService.processImage(image);

        if (armingStatusEnum == ArmingStatus.ARMED_HOME) {
            verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
        } else {
            verify(securityRepository, never()).setAlarmStatus(AlarmStatus.ALARM);
        }
    }

}