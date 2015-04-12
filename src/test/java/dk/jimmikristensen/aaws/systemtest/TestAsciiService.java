package dk.jimmikristensen.aaws.systemtest;

import dk.jimmikristensen.aaws.persistence.database.DataSources;
import dk.jimmikristensen.aaws.systemtest.doubles.FakeDataSourceMySql;
import dk.jimmikristensen.aaws.webservice.config.ApplicationConfig;
import dk.jimmikristensen.aaws.webservice.dto.response.AsciidocList;
import dk.jimmikristensen.aaws.webservice.dto.response.AsciidocProperties;
import dk.jimmikristensen.aaws.webservice.dto.response.adaptor.DateAdapter;
import dk.jimmikristensen.aaws.webservice.service.AsciidocService;
import dk.jimmikristensen.aaws.webservice.service.AsciidocServiceImpl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.test.JerseyTest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestAsciiService extends JerseyTest {
    
    @Before
    public void setup() throws ClassNotFoundException {
        DataSources.put("asciidoc_service", new FakeDataSourceMySql());
    }

    @Override
    protected Application configure() {
        return new ApplicationConfig();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(MultiPartFeature.class);
    }

    @Test
    public void uploadAsciidocFile() throws IOException {    
        String apikey = "testkey";
        String asciidocServicePath = UriBuilder.fromMethod(AsciidocService.class, "uploadFile").build().toString();

        FormDataMultiPart part = getTestCase("asciidoc-testcase5.adoc");

        Response response = target(asciidocServicePath)
                .queryParam("apikey", apikey)
                .request()
                .post(Entity.entity(part, MediaType.MULTIPART_FORM_DATA), Response.class);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }
    
    @Test
    public void uploadAsciidocFileShouldFailWithForbiddenWhenSuppyingInvalidKey() {
        String apikey = "invalidkey";
        String asciidocServicePath = UriBuilder.fromMethod(AsciidocService.class, "uploadFile").build().toString();
        
        FormDataMultiPart part = getTestCase("asciidoc-testcase2.adoc");
        
        Response response = target(asciidocServicePath)
                .queryParam("apikey", apikey)
                .request()
                .post(Entity.entity(part, MediaType.MULTIPART_FORM_DATA), Response.class);
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    } 
   
    @Test
    public void uploadAsciidocFileShouldFailDocumentHasNoTitle() {
        String apikey = "testkey";
        String asciidocServicePath = UriBuilder.fromMethod(AsciidocService.class, "uploadFile").build().toString();
        
        FormDataMultiPart part = getTestCase("asciidoc-testcase4.adoc");
        
        Response response = target(asciidocServicePath)
                .queryParam("apikey", apikey)
                .request()
                .post(Entity.entity(part, MediaType.MULTIPART_FORM_DATA), Response.class);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }
    
    @Test
    public void uploadSameAsciidocFileShouldFailWithBadRequest() {
        String apikey = "testkey";
        String asciidocServicePath = UriBuilder.fromMethod(AsciidocService.class, "uploadFile").build().toString();
        
        FormDataMultiPart part = getTestCase("asciidoc-testcase2.adoc");
        
        Response response = target(asciidocServicePath)
                .queryParam("apikey", apikey)
                .request()
                .post(Entity.entity(part, MediaType.MULTIPART_FORM_DATA), Response.class);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        
        response = target(asciidocServicePath)
                .queryParam("apikey", apikey)
                .request()
                .post(Entity.entity(part, MediaType.MULTIPART_FORM_DATA), Response.class);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }
    
    @Test
    public void getAsciidocByTitle() {
        String apikey = "testkey";
        String docTitle = "Introduction to AsciiDoc";
        String asciidocServicePath = UriBuilder.fromMethod(AsciidocService.class, "getAsciidoc").build(docTitle).toString();
        Response response = target(asciidocServicePath)
                .queryParam("apikey", apikey)
                .request()
                .get();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        String docResp = response.readEntity(String.class);
        assertTrue(docResp.startsWith("= Introduction to AsciiDoc"));
        assertTrue(docResp.endsWith("puts \"Hello, World!\""));
    }
    
    @Test
    public void getAsciidocByUnknownTitle() {
        String apikey = "testkey";
        String docTitle = "Unknown title";
        String asciidocServicePath = UriBuilder.fromMethod(AsciidocService.class, "getAsciidoc").build(docTitle).toString();
        Response response = target(asciidocServicePath).queryParam("apikey", apikey).request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
    
    @Test
    public void getListOfDocuments() throws Exception {
        String apikey = "testkey";
        String asciidocServicePath = UriBuilder.fromMethod(AsciidocService.class, "listAsciidocs").build().toString();
        Response response = target(asciidocServicePath).queryParam("apikey", apikey).request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
     
        AsciidocList docList = response.readEntity(AsciidocList.class);
        DateAdapter dateAdaptor = new DateAdapter();
        
        assertEquals(2, docList.getProps().size());
        AsciidocProperties props = docList.getProps().get(0);
        assertEquals(1, props.getId());
        assertEquals("test@jimmikristensen.dk", props.getOwner());
        assertEquals("Introduction to AsciiDoc", props.getTitle());
        assertEquals("2015-03-31T20:59:59+0200", dateAdaptor.marshal(props.getCreationDate()));
        
        props = docList.getProps().get(1);
        assertEquals(2, props.getId());
        assertEquals("test@jimmikristensen.dk", props.getOwner());
        assertEquals("Example of AsciiDoc", props.getTitle());
        assertEquals("2015-03-30T20:25:01+0200", dateAdaptor.marshal(props.getCreationDate()));
    }

    private FormDataMultiPart getTestCase(String fileName) {
        InputStream is = getClass().getResourceAsStream("/"+fileName);

        String contents = "";
        try {
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int result = bis.read();
            while (result != -1) {
                byte b = (byte) result;
                buf.write(b);
                result = bis.read();
            }

            contents = buf.toString();

            FormDataMultiPart part = new FormDataMultiPart();
            FormDataContentDisposition dispo = FormDataContentDisposition
                    .name("file")
                    .fileName(fileName)
                    .size(contents.getBytes().length)
                    .build();
            FormDataBodyPart bodyPart = new FormDataBodyPart(dispo, contents);
            part.bodyPart(bodyPart);
            
            return part;

        } catch (IOException ex) {
            Logger.getLogger(AsciidocServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
