package com.connect_group.iprocrastinate;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name="iProcrastinate", urlPatterns={"/wait/*"}, asyncSupported = true)
public class ProcrastinateServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(ProcrastinateServlet.class.getName());

    private static final RequestProcessor requestProcessor = new RequestProcessor();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requestProcessor.add(new Request(req.getPathInfo(), req.getParameter("redirect"), req.startAsync()));
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requestProcessor.add(new Request(req.getPathInfo(), req.getParameter("redirect"), req.startAsync()));
    }

    @Override
    public void destroy() {
        super.destroy();
        requestProcessor.destroy();
    }

}
