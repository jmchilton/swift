<%@ page import="edu.mayo.mprc.MprcException" %>
<%@ page import="edu.mayo.mprc.ServletIntialization" %>
<%@ page import="edu.mayo.mprc.swift.SwiftWebContext" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% ServletIntialization.initServletConfiguration(getServletConfig()); %>
<html>
<head><title>Simple jsp page</title>
    <style type="text/css">
        body {
            border: 0;
            margin: 0;
            padding: 0;
            background-color: #fff;

            font-family: Verdana, sans-serif;
            font-size: 12px;
        }

        .user-info {
            display: block;
            line-height: 25px;
            overflow: hidden;
        }

        .user-name {
            background-image: url(user.gif);
            background-repeat: no-repeat;
            background-position: 0 7px;
            padding-left: 18px;
            float: left;
            line-height: 25px;
        }

        .success, .error, .running {
            display: block;
            float: left;
            width: 20px;
            color: #fff;
            margin-left: 3px;
            padding-left: 20px;
            overflow: hidden;
            background-repeat: no-repeat;
            background-position: 0 center;
        }

        .success {
            background-image: url(report/task_ok.gif);
        }

        .error {
            background-image: url(report/task_fail.gif);
        }

        .running {
            background-image: url(report/task_running.gif);
        }

    </style>
    <meta http-equiv="refresh" content="20">
<body>
<div class="user-info"><%
    Cookie[] cookies = request.getCookies();
    String user = null;
    for (Cookie cookie : cookies) {
        if ("email".equals(cookie.getName())) {
            user = cookie.getValue();
        }
    }
    if (user != null) {
        user = URLDecoder.decode(user, "utf8");
        SwiftWebContext.getServletConfig().getSwiftDao().begin();
        try {
            out.print(SwiftWebContext.getServletConfig().getSwiftDao().getSearchRunStatusForUser(user));
            SwiftWebContext.getServletConfig().getSwiftDao().commit();
        } catch (Exception e) {
            SwiftWebContext.getServletConfig().getSwiftDao().rollback();
            throw new MprcException(e);
        }
    }
%></div>
</body>
</html>
