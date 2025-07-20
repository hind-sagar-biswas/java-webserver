# Custom Java WebServer

A minimal yet flexible HTTP 1.1 server written from scratch in Java — no `HttpServer` or prebuilt networking classes used.

## Features

* **Raw Socket Programming**: Built on `ServerSocket` and multithreaded `ConnectionHandler`
* **HTTP Parsing**: Custom `Request` and `Response` objects
* **Static File Serving**: Serve static files with correct MIME types via `HttpUtils`
* **Routing**:

  * `HybridRouter` for custom GET/POST route handlers
  * `StaticRouter` for static file routing only
* **Redirection Support**: 301/302 redirects
* **Simple JSON & Plaintext Responses**: Easy-to-use API
* **Extensible**: Modular structure for easy enhancements

## Project Structure

```plaintext
.
├── pom.xml
├── README.md
├── LICENSE
└── src
    └── main
        └── java
            └── com
                └── hindbiswas
                    └── server
                        ├── App.java
                        ├── ConnectionHandler.java
                        ├── HttpResponse.java
                        ├── HttpUtils.java
                        ├── HybridRouter.java
                        ├── Request.java
                        ├── Response.java
                        ├── RouteHandeler.java
                        ├── Router.java
                        ├── StaticRouter.java
                        └── WebServer.java
```

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

## Example Usage

```java
package com.hindbiswas.server;

public class App {
    public static void main(String[] args) {
        int port = 8080;
        String root = "/home/username/www"; // Change to your local static directory

        WebServer server = new WebServer(port, root);
        Router router = new HybridRouter(); // not required for servinf only static files from `root`

        router.get("/time", request ->
            Response.text("Current time: " + System.currentTimeMillis())
        );

        router.post("/echo", request ->
            Response.json(request.body)
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
