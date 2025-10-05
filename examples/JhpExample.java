package examples;

import com.hindbiswas.server.core.WebServer;
import com.hindbiswas.server.facade.Context;
import com.hindbiswas.server.http.Response;
import com.hindbiswas.server.routing.HybridRouter;

import java.io.File;

/**
 * Example demonstrating JHP integration with the Java WebServer.
 * 
 * This example shows:
 * 1. Automatic .jhp file rendering (file-based routing)
 * 2. Manual rendering using Response.render() with Context
 * 3. Manual rendering using Response.render() with Request
 */
public class JhpExample {
    public static void main(String[] args) {
        int port = 8080;
        String webRoot = "/home/shinigami/www";
        
        WebServer server = new WebServer(port, webRoot);
        HybridRouter router = new HybridRouter();

        // Example 1: Automatic .jhp rendering
        // When you visit http://localhost:8080/index.jhp
        // The server automatically detects the .jhp file and renders it
        // No explicit route needed!

        // Example 2: Manual rendering with custom context using Response.render()
        router.get("/custom", request -> {
            try {
                // Create a custom context with additional data
                Context context = new Context(request);
                context.add("username", "John Doe");
                context.add("role", "Administrator");
                context.add("message", "Welcome to the custom page!");
                
                // Render a JHP file with the custom context
                File jhpFile = new File(webRoot + "/index.jhp");
                return Response.render(jhpFile, context, server.getEngine());
            } catch (Exception e) {
                return Response.error(500);
            }
        });

        // Example 3: Manual rendering with Request (simpler)
        router.get("/simple", request -> {
            try {
                File jhpFile = new File(webRoot + "/index.jhp");
                return Response.render(jhpFile, request, server.getEngine());
            } catch (Exception e) {
                return Response.error(500);
            }
        });

        // Example 4: Accessing and customizing the JHP engine
        // You can add custom functions before starting the server
        router.get("/time", request -> {
            return Response.text("Current time: " + System.currentTimeMillis());
        });

        // Optional: Add custom functions to the JHP engine
        // server.getEngine().getFunctionLibrary().register("myFunction", (String input) -> {
        //     return "Custom: " + input;
        // });

        // Optional: Enable debug mode for detailed error messages
        // server.getEngine().setDebugMode(true);

        server.setRouter(router);
        
        System.out.println("=================================================");
        System.out.println("JHP Integration Example Server");
        System.out.println("=================================================");
        System.out.println("Server starting on port " + port);
        System.out.println("Web root: " + webRoot);
        System.out.println();
        System.out.println("Try these URLs:");
        System.out.println("  http://localhost:8080/index.jhp    - Auto-rendered JHP file");
        System.out.println("  http://localhost:8080/test.html    - Static HTML file");
        System.out.println("  http://localhost:8080/custom       - Manual render with custom context");
        System.out.println("  http://localhost:8080/simple       - Manual render with request");
        System.out.println("  http://localhost:8080/time         - Regular text response");
        System.out.println("=================================================");
        
        server.start();
    }
}
