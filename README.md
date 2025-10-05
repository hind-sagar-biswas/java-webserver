# Custom Java WebServer

A minimal yet flexible HTTP 1.1 server written from scratch in Java — no `HttpServer` or prebuilt networking classes used.

## Features

* **Raw Socket Programming**: Built on `ServerSocket` and multithreaded `ConnectionHandler`
* **HTTP Parsing**: Custom `Request` and `Response` objects
* **Static File Serving**: Serve static files with correct MIME types via `HttpUtils`
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
├── src
│   ├── main
│   │   └── java
│   │       └── com
│   │           └── hindbiswas
│   │               └── server
│   │                   ├── App.java
│   │                   ├── core/
│   │                   │   └── WebServer.java
│   │                   ├── http/
│   │                   │   ├── HttpResponse.java
│   │                   │   ├── HttpUtils.java
│   │                   │   ├── Request.java
│   │                   │   └── Response.java
│   │                   ├── routing/
│   │                   │   ├── AbstractMethodRouter.java
│   │                   │   ├── ApiRouter.java
│   │                   │   ├── HybridRouter.java
│   │                   │   ├── Router.java
│   │                   │   └── StaticRouter.java
│   │                   └── handler/
│   │                       ├── ConnectionHandler.java
│   │                       └── RouteHandler.java
│   └── test
│       └── java
│           └── com
│               └── hindbiswas
│                   └── server
│                       └── AppTest.java

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

### Add the Dependency to your `pom.xml` file

```xml
<dependency>
  <groupId>com.github.hind-sagar-biswas</groupId>
  <artifactId>java-webserver</artifactId>
  <version>1.1.0</version>
</dependency>
```

### Run Install

```bash
mvn install
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
```

## License

This project is open-source and available under the MIT License.

---

Built with ❤️ by \[Hind Biswas] — because sometimes, you just gotta build it yourself to *really* learn it.
