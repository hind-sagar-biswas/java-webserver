package com.hindbiswas.server.facade;

import java.io.IOException;

import com.hindbiswas.jhp.engine.IssueHandleMode;
import com.hindbiswas.jhp.engine.Settings;
import com.hindbiswas.server.core.WebServer;
import com.hindbiswas.server.http.Request;

public class JhpEngine extends com.hindbiswas.jhp.engine.JhpEngine {
    public final class RenderException extends RuntimeException {
        public RenderException(String content) {
            super((debug) ? content : "Something went wrong!");
            System.err.println(content);
        }
    }

    private final FunctionLibrary functionLibrary;
    private boolean debug = false;

    public JhpEngine(WebServer server) throws IOException {
        FunctionLibrary functionLibrary = new FunctionLibrary();
        super(Settings.builder().base(server.getWebRoot()).issueHandleMode(IssueHandleMode.THROW).build(), functionLibrary);
    }

    public void setDebugMode(boolean debug) {
        this.debug = debug;
    }

    public boolean isInDebugMode() {
        return debug;
    }

    public FunctionLibrary getFunctionLibrary() {
        return functionLibrary;
    }

    @Override
    public String render(String pathTxt, com.hindbiswas.jhp.Context context) throws RenderException, IllegalArgumentException {
        if (!(context instanceof Context)) {
            throw new IllegalArgumentException("Invalid context type");
        }
        try {
            String result = super.render(pathTxt, context);
            return result;
        } catch (Exception e) {
            throw new RenderException(e.getMessage());
        }
    }

    public String render(String pathTxt, Request req) throws RenderException {
        return render(pathTxt, new Context(req));
    }
}
