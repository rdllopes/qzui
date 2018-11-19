package qzui;

import com.google.common.base.Optional;
import restx.server.JettyWebServer;
import restx.server.WebServer;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;

/**
 * This class can be used to run the app.
 *
 * Alternatively, you can deploy the app as a war in a regular container like tomcat or jetty.
 *
 * Reading the port from system env PORT makes it compatible with heroku.
 */
public class AppServer {
    public static final String WEB_INF_LOCATION = "./webapp/WEB-INF/web.xml";
    public static final String WEB_APP_LOCATION = "build/libs/quartz-ui-0.1-SNAPSHOT.war";

    public static void main(String[] args) throws Exception {
        int port = Integer.valueOf(Optional.fromNullable(System.getenv("PORT")).or("8080"));
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(WEB_INF_LOCATION);

        ProtectionDomain domain = AppServer.class.getProtectionDomain();
        URL location = domain.getCodeSource().getLocation();
        String war_location = location.toExternalForm();

        System.out.println(war_location);
        WebServer server = new JettyWebServer(new File(url.toURI()).toString(), WEB_APP_LOCATION, port, "0.0.0.0");

        /*
         * load mode from system property if defined, or default to dev
         * be careful with that setting, if you use this class to launch your server in production, make sure to launch
         * it with -Drestx.mode=prod or change the default here
         */
        System.setProperty("restx.mode", System.getProperty("restx.mode", "dev"));
        System.setProperty("restx.app.package", "qzui");

        server.startAndAwait();
    }
}
