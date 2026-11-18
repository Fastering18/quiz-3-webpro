package com.zstore.servlet;

import com.zstore.dao.ProdukDAO;
import com.zstore.dao.ProviderDAO;
import com.zstore.dao.TagDAO;
import com.zstore.model.Produk;
import com.zstore.model.Provider;
import com.zstore.model.Tag;
import com.zstore.util.FormatUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HomeServlet extends HttpServlet {

    private final ProviderDAO providerDAO = new ProviderDAO();
    private final ProdukDAO produkDAO = new ProdukDAO();
    private final TagDAO tagDAO = new TagDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long activeProviderId = FormatUtil.parseLongOrDefault(req.getParameter("provider"), 0);
        long activeTagId = FormatUtil.parseLongOrDefault(req.getParameter("tag"), 0);
        String query = FormatUtil.trimOrEmpty(req.getParameter("q"));

        try {
            List<Provider> categories = providerDAO.findAll();
            List<Tag> tags = tagDAO.findAll();
            List<Produk> products = produkDAO.findFiltered(activeProviderId, activeTagId, query);

            String activeCategoryName = null;
            if (activeProviderId > 0) {
                for (Provider pr : categories) {
                    if (pr.getIdProvider() == activeProviderId) {
                        activeCategoryName = pr.getNama();
                        break;
                    }
                }
            }

            req.setAttribute("categories", categories);
            req.setAttribute("tags", tags);
            req.setAttribute("products", products);
            req.setAttribute("activeProviderId", activeProviderId);
            req.setAttribute("activeTagId", activeTagId);
            req.setAttribute("activeCategoryName", activeCategoryName);
            req.setAttribute("searchQuery", query);
            req.setAttribute("pageTitle", "Z-Store | Toko Lisensi Digital");

            req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Database error loading catalog", e);
        }
    }
}

