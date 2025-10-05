package com.hindbiswas.server.facade;

import java.io.IOException;

import com.hindbiswas.jhp.engine.IssueHandleMode;
import com.hindbiswas.jhp.engine.Settings;
import com.hindbiswas.server.core.WebServer;
import com.hindbiswas.server.http.Request;
import com.hindbiswas.server.util.StringUtils;
import com.hindbiswas.server.util.MathUtils;
import com.hindbiswas.server.util.DateUtils;
import com.hindbiswas.server.util.CollectionUtils;
import com.hindbiswas.server.util.HtmlUtils;

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
        super(Settings.builder().base(server.getWebRoot()).issueHandleMode(IssueHandleMode.THROW).build(),
                functionLibrary);
        this.functionLibrary = functionLibrary;
        registerUtilityFunctions();
    }

    private void registerUtilityFunctions() {
        // String utilities
        functionLibrary.register("upper", StringUtils.upper);
        functionLibrary.register("lower", StringUtils.lower);
        functionLibrary.register("capitalize", StringUtils.capitalize);
        functionLibrary.register("trim", StringUtils.trim);
        functionLibrary.register("length", StringUtils.length);
        functionLibrary.register("contains", StringUtils.contains);
        functionLibrary.register("replace", StringUtils.replace);
        functionLibrary.register("split", StringUtils.split);
        functionLibrary.register("substring", StringUtils.substring);
        functionLibrary.register("repeat", StringUtils.repeat);
        functionLibrary.register("reverse", StringUtils.reverse);
        functionLibrary.register("startsWith", StringUtils.startsWith);
        functionLibrary.register("endsWith", StringUtils.endsWith);

        // Math utilities
        functionLibrary.register("abs", MathUtils.abs);
        functionLibrary.register("round", MathUtils.round);
        functionLibrary.register("ceil", MathUtils.ceil);
        functionLibrary.register("floor", MathUtils.floor);
        functionLibrary.register("max", MathUtils.max);
        functionLibrary.register("min", MathUtils.min);
        functionLibrary.register("pow", MathUtils.pow);
        functionLibrary.register("sqrt", MathUtils.sqrt);
        functionLibrary.register("random", MathUtils.random);
        functionLibrary.register("clamp", MathUtils.clamp);

        // Date utilities
        functionLibrary.register("now", DateUtils.now);
        functionLibrary.register("formatDate", DateUtils.formatDate);
        functionLibrary.register("currentDateTime", DateUtils.currentDateTime);
        functionLibrary.register("currentYear", DateUtils.currentYear);
        functionLibrary.register("currentMonth", DateUtils.currentMonth);
        functionLibrary.register("currentDay", DateUtils.currentDay);

        // Collection utilities
        functionLibrary.register("size", CollectionUtils.size);
        functionLibrary.register("isEmpty", CollectionUtils.isEmpty);
        functionLibrary.register("join", CollectionUtils.join);
        functionLibrary.register("first", CollectionUtils.first);
        functionLibrary.register("last", CollectionUtils.last);
        functionLibrary.register("reverseList", CollectionUtils.reverse);

        // HTML utilities
        functionLibrary.register("escape", HtmlUtils.escape);
        functionLibrary.register("stripTags", HtmlUtils.stripTags);
        functionLibrary.register("nl2br", HtmlUtils.nl2br);
        functionLibrary.register("truncate", HtmlUtils.truncate);
        functionLibrary.register("urlEncode", HtmlUtils.urlEncode);
        functionLibrary.register("urlDecode", HtmlUtils.urlDecode);
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
