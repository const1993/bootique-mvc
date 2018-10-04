package io.bootique.mvc.mustache;

import com.github.mustachejava.Mustache;
import io.bootique.jcache.JCacheModule;
import io.bootique.jersey.JerseyModule;
import io.bootique.mvc.mustache.view.ConcreteView;
import io.bootique.test.junit.BQTestFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class MvcMustacheJcacheIT {
    @ClassRule
    public static BQTestFactory TEST_SERVER = new BQTestFactory();

    @BeforeClass
    public static void beforeClass() {
        TEST_SERVER.app()
                .args("--config=classpath:MvcMustacheModuleIT.yml", "-s")
                .module(new MvcMustacheModuleProvider())
                .modules(new JCacheModule())
                .module(binder -> {
                    JerseyModule.extend(binder).addResource(MvcMustacheModuleIT.Api.class);

                    Factory<ExpiryPolicy> _100ms = CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MILLISECONDS, 100));

                    Configuration<String, Mustache> boundConfig = new MutableConfiguration<String, Mustache>()
                            .setTypes(String.class, Mustache.class)
                            .setExpiryPolicyFactory(_100ms);
                    JCacheModule.extend(binder).setConfiguration("mustacheMvc", boundConfig);
                }).run();
    }

    @Test
    public void testV1() {
        WebTarget base = ClientBuilder.newClient().target("http://localhost:8080");
        Response r1 = base.path("/v1").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("\nv1_string_p1_number_564", r1.readEntity(String.class));
    }

    @Test
    public void testV2() {
        WebTarget base = ClientBuilder.newClient().target("http://localhost:8080");
        Response r1 = base.path("/v2").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("\nv2_string_p2_number_5649", r1.readEntity(String.class));
    }

    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public static class Api {

        @GET
        @Path("/v1")
        public ConcreteView getV1() {
            MvcMustacheModuleIT.Model m = new MvcMustacheModuleIT.Model();
            m.setProp1("p1");
            m.setProp2(564);
            return new ConcreteView("MvcMustacheModuleIT_v1.mustache", m);
        }

        @GET
        @Path("/v2")
        public ConcreteView getV2() {
            MvcMustacheModuleIT.Model m = new MvcMustacheModuleIT.Model();
            m.setProp1("p2");
            m.setProp2(5649);
            return new ConcreteView("MvcMustacheModuleIT_v2.mustache", m);
        }
    }

    public static class Model {
        private String prop1;
        private int prop2;

        public String getProp1() {
            return prop1;
        }

        public void setProp1(String prop1) {
            this.prop1 = prop1;
        }

        public int getProp2() {
            return prop2;
        }

        public void setProp2(int prop2) {
            this.prop2 = prop2;
        }
    }
}
