# Custom Java WebServer

[![Version](https://img.shields.io/badge/version-2.1.0-blue.svg)](https://github.com/hind-sagar-biswas/java-webserver/releases)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)

A minimal yet flexible HTTP 1.1 server written from scratch in Java — no `HttpServer` or prebuilt networking classes used.

> **Latest Release:** v2.1.0 (2025-10-24) - Session Management, Cookies & Logger System

## Features

* **Raw Socket Programming**: Built on `ServerSocket` and multithreaded `ConnectionHandler`
* **HTTP Parsing**: Custom `Request` and `Response` objects
* **Static File Serving**: Serve static files with correct MIME types via `HttpUtils`
* **Session Management**: Complete session handling system
  * Multiple storage backends: In-Memory, File-based, SQLite
  * Configurable session lifecycle and cleanup
  * Thread-safe session operations
* **Cookie Support**: Full HTTP cookie implementation with attributes (MaxAge, Path, Domain, Secure, HttpOnly, SameSite)
* **Logger System**: Thread-safe logging with color-coded output and multiple log levels
* **JHP Template Engine**: Integrated JHP (Java Hypertext Preprocessor) for dynamic server-side rendering
  * Automatic `.jhp` file rendering (like Apache with PHP)
  * `Response.render()` API for programmatic rendering
  * Built-in utility functions (String, Math, Date, Collection, HTML)
  * Access to request data via `__server`, `__header`, `__get`, `__post`
* **Routing**:

  * `HybridRouter` for custom GET/POST/PUT/PATCH/DELETE route handlers
  * `StaticRouter` for static file routing only
  * `ApiRouter` for API routing (no static files)
* **Redirection Support**: 301/302 redirects
* **Simple JSON & Plaintext Responses**: Easy-to-use API
* **Extensible**: Modular structure for easy enhancements

## Project Structure

```plaintext
.
├── LICENSE
├── pom.xml
├── README.md
└── src
    └── main
        └── java
            └── com
                └── hindbiswas
                    └── server
                        ├── App.java
                        ├── core
                        │   └── WebServer.java
                        ├── facade
                        │   ├── Context.java
                        │   ├── FunctionLibrary.java
                        │   └── JhpEngine.java
                        ├── handler
                        │   ├── ConnectionHandler.java
                        │   └── RouteHandler.java
                        ├── http
                        │   ├── Cookie.java
                        │   ├── HttpResponse.java
                        │   ├── HttpUtils.java
                        │   ├── Request.java
                        │   └── Response.java
                        ├── logger
                        │   ├── Color.java
                        │   ├── LogType.java
                        │   ├── Logger.java
                        │   └── TextFormatter.java
                        ├── routing
                        │   ├── AbstractMethodRouter.java
                        │   ├── ApiRouter.java
                        │   ├── HybridRouter.java
                        │   ├── Router.java
                        │   └── StaticRouter.java
                        ├── session
                        │   ├── Session.java
                        │   ├── SessionConfig.java
                        │   ├── SessionManager.java
                        │   ├── StorageType.java
                        │   └── storage
                        │       ├── FileSessionStorage.java
                        │       ├── InMemorySessionStorage.java
                        │       ├── SQLiteSessionStorage.java
                        │       └── SessionStorage.java
                        └── util
                            ├── CollectionUtils.java
                            ├── DateUtils.java
                            ├── HtmlUtils.java
                            ├── MathUtils.java
                            ├── RandomUtils.java
                            ├── SessionUtils.java
                            └── StringUtils.java

## 🏁 Getting Started

### Requirements

* Java 17+
* Maven (for building)

### Run the Server

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.hindbiswas.server.App"
```

This will start the server on port `8080` and serve files from `/home/user/www`.

## Installation (Maven)

#### Step 1: Configure GitHub Package Repositories

Add both the Java WebServer and JHP repositories to your `pom.xml`:

```xml
<repositories>
    <!-- Java WebServer Repository -->
    <repository>
        <id>github-webserver</id>
        <name>GitHub hind-sagar-biswas Java WebServer</name>
        <url>https://maven.pkg.github.com/hind-sagar-biswas/java-webserver</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
        <releases>
            <enabled>true</enabled>
        </releases>
    </repository>
    <!-- JHP Template Engine Repository -->
    <repository>
        <id>github-jhp</id>
        <name>GitHub hind-sagar-biswas JHP Package</name>
        <url>https://maven.pkg.github.com/hind-sagar-biswas/java-hypertext-preprocessor</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
        <releases>
            <enabled>true</enabled>
        </releases>
    </repository>
</repositories>
```

#### Step 2: Add the Dependency

Add the Java WebServer dependency:

```xml
<dependencies>
    <dependency>
        <groupId>com.github.hind-sagar-biswas</groupId>
        <artifactId>java-webserver</artifactId>
        <version>2.1.0</version>
    </dependency>
</dependencies>
```

#### Step 3: Install Dependencies

```bash
mvn clean install
```

## Example Usage

```java
import com.hindbiswas.server.core.WebServer;
import com.hindbiswas.server.http.Response;
import com.hindbiswas.server.routing.HybridRouter;

public class App {
    public static void main(String[] args) {
        int port = 8080;
        String root = "/home/username/www"; // Change to your local static directory

        WebServer server = new WebServer(port, root);
        HybridRouter router = new HybridRouter(); // not required for serving only static files from `root`

        router.get("/time", request ->
            Response.text("Current time: " + System.currentTimeMillis())
        );

        router.post("/echo", request ->
            Response.json(request.body.toString())
        );

        server.setRouter(router);
        server.start();
    }
}
```

## Define Routes

In `App.java`:

```java
router.get("/time", req -> Response.text("Current time: " + System.currentTimeMillis()));

router.post("/submit", req -> {
    String data = req.body; // raw post data
    return Response.json("{\"received\": true}");
});
```

## Example Response Object

```java
Response.redirect("/login");
Response.text("Hello, World!");
Response.json("{\"status\":\"ok\"}");
Response.error(404);

// JHP rendering
Response.render(new File("/path/to/template.jhp"), context, server.getEngine());
Response.render(new File("/path/to/template.jhp"), request, server.getEngine());
```

## JHP Template Engine

The server includes integrated JHP (Java Hypertext Preprocessor) support for dynamic server-side rendering.

### Automatic .jhp File Rendering

Place `.jhp` files in your webroot and they'll be automatically rendered:

**File: `/home/user/www/index.jhp`**
```html
<!DOCTYPE html>
<html>
<body>
    <h1>Hello from JHP!</h1>
    <p>Request method: {{ __server.method }}</p>
    <p>Current time: {{ currentDateTime() }}</p>
    <p>Random number: {{ random() }}</p>
</body>
</html>
```

Access: `http://localhost:8080/index.jhp` - automatically rendered!

### Manual Rendering with Response.render()

```java
router.get("/profile", request -> {
    Context context = new Context(request);
    context.add("username", "Alice");
    context.add("role", "Admin");
    
    File template = new File(webRoot + "/profile.jhp");
    return Response.render(template, context, server.getEngine());
});
```

### Add Custom Functions

```java
server.getEngine().getFunctionLibrary().register("greet", (String name) -> {
    return "Hello, " + name + "!";
});
// Use in template: {{ greet("World") }}
```

For complete JHP documentation, see the [Wiki](https://github.com/hind-sagar-biswas/java-webserver/wiki)

## What's New in v2.0.0

🎉 **Major Features:**
- JHP Template Engine integration for dynamic rendering
- Built-in utility functions (String, Math, Date, Collection, HTML)
- Facade pattern architecture (Context, FunctionLibrary, JhpEngine)
- Enhanced error handling and MIME type support

See [CHANGELOG.txt](CHANGELOG.txt) for complete release notes.

## Documentation

- **Wiki**: [https://github.com/hind-sagar-biswas/java-webserver/wiki](https://github.com/hind-sagar-biswas/java-webserver/wiki)
- **Changelog**: [CHANGELOG.txt](CHANGELOG.txt)
- **Examples**: [examples/](examples/)

## License

This project is open-source and available under the MIT License.

---

Built with ❤️ by [Hind Biswas](https://github.com/hind-sagar-biswas) — because sometimes, you just gotta build it yourself to *really* learn it.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

If you encounter any issues or have questions, please [open an issue](https://github.com/hind-sagar-biswas/java-webserver/issues).
