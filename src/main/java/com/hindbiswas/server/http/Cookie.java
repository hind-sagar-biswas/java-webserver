package com.hindbiswas.server.http;

public class Cookie {
    public enum SameSite {
        LAX,
        STRICT,
        NONE
    };

    private String name;
    private String value;
    private String path = "/";
    private String domain;
    private int maxAge = -1; // -1 = session cookie
    private boolean httpOnly = true;
    private boolean secure = false;
    private SameSite sameSite = SameSite.LAX;

    private Cookie() {
    }
    
    public Cookie(String name, String value, String path, String domain, int maxAge, boolean httpOnly, boolean secure,
            SameSite sameSite) {
        this.name = name;
        this.value = value;
        this.path = path;
        this.domain = domain;
        this.maxAge = maxAge;
        this.httpOnly = httpOnly;
        this.secure = secure;
        this.sameSite = sameSite;
    }
    
    public Cookie(String name, String value, int maxAge) {
        this.name = name;
        this.value = value;
        this.maxAge = maxAge;
    }

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public SameSite getSameSite() {
        return sameSite;
    }

    public void setSameSite(SameSite sameSite) {
        this.sameSite = sameSite;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value == null ? "" : value);

        if (path != null) {
            sb.append("; Path=").append(path);
        }
        if (domain != null) {
            sb.append("; Domain=").append(domain);
        }
        if (maxAge >= 0) {
            sb.append("; Max-Age=").append(maxAge);
        }
        if (httpOnly) {
            sb.append("; HttpOnly");
        }
        if (secure) {
            sb.append("; Secure");
        }
        if (sameSite != null) {
            sb.append("; SameSite=").append(sameSite.name());
        }

        return sb.toString();
    }

    public static Cookie parse(String cookieString) {
        if (cookieString == null || cookieString.trim().isEmpty()) {
            return null;
        }

        Cookie cookie = new Cookie();
        String[] parts = cookieString.split(";");

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (i == 0) {
                int eq = part.indexOf('=');
                if (eq > 0) {
                    cookie.name = part.substring(0, eq);
                    cookie.value = part.substring(eq + 1);
                } else {
                    cookie.name = part; // value-less cookie
                }
            } else {
                String[] kv = part.split("=", 2);
                String key = kv[0].trim().toLowerCase();
                String val = kv.length > 1 ? kv[1].trim() : null;

                switch (key) {
                    case "path":
                        cookie.path = val;
                        break;
                    case "domain":
                        cookie.domain = val;
                        break;
                    case "max-age":
                        try { cookie.maxAge = Integer.parseInt(val); } catch (Exception ignored) {}
                        break;
                    case "httponly":
                        cookie.httpOnly = true;
                        break;
                    case "secure":
                        cookie.secure = true;
                        break;
                    case "samesite":
                        if (val != null) {
                            try { cookie.sameSite = SameSite.valueOf(val.toUpperCase()); } catch (Exception ignored) {}
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        return cookie;
    }


    public static Cookie sessionCookie(String name, String value) {
        Cookie c = new Cookie(name, value);
        c.setHttpOnly(true);
        c.setMaxAge(-1);
        return c;
    }

    public static Cookie secureCookie(String name, String value, int maxAgeSeconds) {
        Cookie c = new Cookie(name, value);
        c.setHttpOnly(true);
        c.setSecure(true);
        c.setMaxAge(maxAgeSeconds);
        c.setSameSite(SameSite.STRICT);
        return c;
    }
}
