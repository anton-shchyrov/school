package org.example.school.users.services;

import io.helidon.security.annotations.Authenticated;
import io.helidon.security.annotations.Authorized;
import org.example.school.users.ExceptionHandler;
import org.example.school.users.UserRole;
import org.example.school.users.protocol.*;
import org.example.school.users.protocol.admin.UserAdd;
import org.example.school.users.protocol.admin.UserLogin;
import org.example.school.users.protocol.admin.UserModify;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@Authorized
@RolesAllowed(UserRole.ADMIN_ROLE)
public class UserService extends CustomService {
    private static final String OK_MESSAGE = GSON.toJson(Status.OK);
    private static final String NOT_FOUND_MESSAGE = GSON.toJson(Status.NOT_FOUND);

    @POST
    @Path("add")
    public Response add(String data) {
        Response.ResponseBuilder respBuilder;
        try {
            UserAdd user = GSON.fromJson(data, UserAdd.class);
            user.validate();
            getDbClient().execute(exec -> exec
                .createNamedInsert("user-add")
                .params(user.createParams())
                .execute()
            ).get();
            respBuilder = Response.ok().entity(OK_MESSAGE);
        } catch (Throwable e) {
            respBuilder = ExceptionHandler.handle(e);
        }
        return respBuilder.build();
    }

    @POST
    @Path("delete")
    public Response delete(String data) {
        Response.ResponseBuilder respBuilder;
        try {
            UserLogin user = GSON.fromJson(data, UserLogin.class);
            user.validate();
            Long cnt = getDbClient().execute(exec -> exec
                .createNamedDelete("user-del")
                .params(user.createParams())
                .execute()
            ).get();
            respBuilder = Response.ok().entity((cnt > 0) ? OK_MESSAGE : NOT_FOUND_MESSAGE);
        } catch (Throwable e) {
            respBuilder = ExceptionHandler.handle(e);
        }
        return respBuilder.build();
    }

    @POST
    @Path("update")
    public Response update(String data) {
        Response.ResponseBuilder respBuilder;
        try {
            UserModify user = GSON.fromJson(data, UserModify.class);
            user.validate();
            StringBuilder builder = new StringBuilder("UPDATE users usr SET ");
            if (QueryData.testNotEmptyString(user.password))
                builder.append("usr.\"PASSWORD\" = :password, ");
            if (QueryData.testNotEmptyString(user.name))
                builder.append("usr.\"NAME\" = :name, ");
            builder.deleteCharAt(builder.length() - 2);
            builder.append("WHERE usr.\"LOGIN\" = :login");
            Long cnt = getDbClient().execute(exec -> exec
                .createUpdate(builder.toString())
                .params(user.createParams())
                .execute()
            ).get();
            respBuilder = Response.ok().entity((cnt > 0) ? OK_MESSAGE : NOT_FOUND_MESSAGE);
        } catch (Throwable e) {
            respBuilder = ExceptionHandler.handle(e);
        }
        return respBuilder.build();
    }
}
