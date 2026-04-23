/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smartcampus.store;

import com.example.smartcampus.model.Room;
import com.example.smartcampus.model.Sensor;
import com.example.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readingsBySensorId = new ConcurrentHashMap<>();

    private DataStore() {
        seedData();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Map<String, List<SensorReading>> getReadingsBySensorId() {
        return readingsBySensorId;
    }

    public List<SensorReading> getOrCreateReadingsList(String sensorId) {
        return readingsBySensorId.computeIfAbsent(sensorId,
                k -> java.util.Collections.synchronizedList(new ArrayList<>()));
    }

    private void seedData() {
        Room room = new Room("LIB-301", "Library Quiet Study", 50);
        rooms.put(room.getId(), room);

        Sensor sensor = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.3, "LIB-301");
        sensors.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());

        List<SensorReading> readings = getOrCreateReadingsList(sensor.getId());
        readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 22.3));
    }
}