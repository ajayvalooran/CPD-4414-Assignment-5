package test;

import java.awt.Event;
import static java.awt.Event.DELETE;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import static javax.ws.rs.HttpMethod.POST;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.jboss.logging.Param;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author c0647711
 */
@Path("/products")
public class products {

    @GET
    @Produces("application/json")
    public String doGet() {

        String result = getResults("SELECT * FROM PRODUCT");
        return result;
    }

    @GET
    @Path("{id}")
    @Produces("application/json")
    public String doGet(@PathParam("id") String id) {

        String result = getResults("SELECT * FROM PRODUCT where product_id = ?", id);
        return result;
    }

    /**
     * Provides POST /servlet?name=XXX&age=XXX
     *
     * @param request - the request object
     * @param response - the response object
     */
    @POST
    @Consumes("application/json")
    public Response doPost(JsonObject obj) {

        String id = obj.getString("product_id");
        String name = obj.getString("product_name");
        String desc = obj.getString("product_description");
        String qty = obj.getString("quantity");

        int ins = doUpdate("INSERT INTO PRODUCT ( product_id, product_name, product_description, quantity) VALUES (?, ?, ?, ?)", id, name, desc, qty);

        if (ins <= 0) {
            return Response.status(500).build();
        } else {
            return Response.ok("http://localhost:8080/Assignment4/webresources/products/" + id).build();
        }

    }

    @PUT
    @Path("{id}")
    @Consumes("application/json")
    public Response doPut(@PathParam("id") String id, JsonObject obj) {
        
        String name = obj.getString("name");
        String desc = obj.getString("description");
        String qty = obj.getString("quantity");

        int upd = doUpdate("UPDATE PRODUCT SET product_id = ?, product_name = ?, product_description = ?, quantity = ? WHERE product_id = ?", id, name, desc, qty, id);
                     
        if (upd <= 0) {
            return Response.status(500).build();
        } else {
            return Response.ok("http://localhost:8080/Assignment4/webresources/products/" + id).build();
        }
    }

    @DELETE
    @Path("{id}")
    public void doDelete(@PathParam("id") String id, String str) {

        doUpdate("DELETE FROM PRODUCT WHERE product_id = ?", id);

    }

    private String getResults(String query, String... params) {

        StringWriter str = new StringWriter();
        JsonGeneratorFactory JSfac = Json.createGeneratorFactory(null);
        JsonGenerator generator = JSfac.createGenerator(str);

        try (Connection conn = credentials.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            ResultSet res = pstmt.executeQuery();

            generator.writeStartArray();

            while (res.next()) {

                generator.writeStartObject()
                        .write("id", res.getInt("product_id"))
                        .write("name", res.getString("product_name"))
                        .write("desc", res.getString("product_description"))
                        .write("qty", res.getInt("quantity"))
                        .writeEnd();

            }

            generator.writeEnd();
            generator.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(products.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return str.toString();
    }

    private int doUpdate(String query, String... params) {
        int numChanges = 0;
        try (Connection conn = credentials.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            numChanges = pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(products.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numChanges;
    }
    
    private int getId(String query, String name, String desc) {
        int id = 0;
        try (Connection conn = credentials.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "name");
            pstmt.setString(2, "desc");

            ResultSet res = pstmt.executeQuery();

            if (res.next()) {
                id = res.getInt("product_id");
            }

        } catch (SQLException ex) {
            Logger.getLogger(products.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

}
