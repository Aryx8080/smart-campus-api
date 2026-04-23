/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smartcampus.resource;

import com.example.smartcampus.exception.LinkedResourceNotFoundException;
import com.example.smartcampus.exception.ResourceNotFoundException;
import com.example.smartcampus.model.Sensor;
import com.example.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    @GET
    public List<Sensor> getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(dataStore.getSensors().values());

        if (type == null || type.isBlank()) {
            return sensors;
        }

        return sensors.stream()
                .filter(sensor -> sensor.getType() != null &&
                        sensor.getType().toLowerCase(Locale.ROOT).equals(type.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{sensorId}")
    public Sensor getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with id '" + sensorId + "' was not found.");
        }
        return sensor;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        validateSensor(sensor);

        if (!dataStore.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "Cannot create sensor because roomId '" + sensor.getRoomId() + "' does not exist."
            );
        }

        if (dataStore.getSensors().containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new com.example.smartcampus.model.ApiError(
                            409,
                            "Conflict",
                            "Sensor with id '" + sensor.getId() + "' already exists.",
                            uriInfo.getPath()
                    ))
                    .build();
        }

        dataStore.getSensors().put(sensor.getId(), sensor);
        dataStore.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());
        dataStore.getOrCreateReadingsList(sensor.getId());

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        if (!dataStore.getSensors().containsKey(sensorId)) {
            throw new ResourceNotFoundException("Sensor with id '" + sensorId + "' was not found.");
        }
        return new SensorReadingResource(sensorId);
    }

    private void validateSensor(Sensor sensor) {
        if (sensor == null) {
            throw new BadRequestException("Sensor body is required.");
        }
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            throw new BadRequestException("Sensor id is required.");
        }
        if (sensor.getType() == null || sensor.getType().isBlank()) {
            throw new BadRequestException("Sensor type is required.");
        }
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            throw new BadRequestException("Sensor status is required.");
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            throw new BadRequestException("Sensor roomId is required.");
        }
    }
}