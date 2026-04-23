/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.smartcampus.resource;

import com.example.smartcampus.exception.ResourceNotFoundException;
import com.example.smartcampus.exception.RoomNotEmptyException;
import com.example.smartcampus.model.Room;
import com.example.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore dataStore = DataStore.getInstance();

    @GET
    public List<Room> getAllRooms() {
        return new ArrayList<>(dataStore.getRooms().values());
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        validateRoom(room);

        Map<String, Room> rooms = dataStore.getRooms();

        if (rooms.containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new com.example.smartcampus.model.ApiError(
                            409,
                            "Conflict",
                            "Room with id '" + room.getId() + "' already exists.",
                            uriInfo.getPath()
                    ))
                    .build();
        }

        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        rooms.put(room.getId(), room);

        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(location).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Room getRoomById(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room with id '" + roomId + "' was not found.");
        }
        return room;
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room with id '" + roomId + "' was not found.");
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room '" + roomId + "' cannot be deleted because it still contains assigned sensors.");
        }

        dataStore.getRooms().remove(roomId);
        return Response.noContent().build();
    }

    private void validateRoom(Room room) {
        if (room == null) {
            throw new BadRequestException("Room body is required.");
        }
        if (room.getId() == null || room.getId().isBlank()) {
            throw new BadRequestException("Room id is required.");
        }
        if (room.getName() == null || room.getName().isBlank()) {
            throw new BadRequestException("Room name is required.");
        }
        if (room.getCapacity() < 0) {
            throw new BadRequestException("Room capacity must be zero or greater.");
        }
    }
}
