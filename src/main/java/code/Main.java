package code;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private final static Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(new Option("p", "port", true, "server listin port"));
        options.addOption(new Option("b", "base", true, "resource base"));

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        int port = 8080;
        String p = cmd.getOptionValue('p');
        if (p != null) {
            port = Integer.parseInt(p);
        }

        String b = cmd.getOptionValue('b');
        if (b == null) {
            b = "src/main/html";
        }

        ResourceHandler rh = new ResourceHandler();
        rh.setResourceBase(b);

        ServletContextHandler ch = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        ch.setContextPath("/");
        ch.addServlet(WordsGenerateServlet.class, "/genwords");
        ch.addServlet(WordsCrossBuildServlet.class, "/buildcross");

        HandlerList hl = new HandlerList();
        hl.setHandlers(new Handler[] { rh, ch, new DefaultHandler() });

        Server server = new Server(port);
        server.setHandler(hl);
        server.start();

        LOG.info("server started, listen port: " + port);

        server.join();
    }
}
