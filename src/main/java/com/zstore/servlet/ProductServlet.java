package com.zstore.servlet;

import com.zstore.dao.ProdukDAO;
import com.zstore.model.Produk;
import com.zstore.util.FormatUtil;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProductServlet extends HttpServlet {

    private final ProdukDAO produkDAO = new ProdukDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long id = FormatUtil.parseLongOrDefault(req.getParameter("id"), 0);
        if (id <= 0) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            req.getRequestDispatcher("/WEB-INF/views/404.jsp").forward(req, resp);
            return;
        }

        try {
            Produk product = produkDAO.findById(id);
            if (product == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                req.getRequestDispatcher("/WEB-INF/views/404.jsp").forward(req, resp);
                return;
            }

            req.setAttribute("product", product);
            req.setAttribute("pageTitle", product.getNamaProduk() + " | Z-Store");
            req.getRequestDispatcher("/WEB-INF/views/product.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Database error loading product", e);
        }
    }
}

