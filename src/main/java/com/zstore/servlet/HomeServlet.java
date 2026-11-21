package com.zstore.servlet;

import com.zstore.dao.ProdukDAO;
import com.zstore.dao.ProviderDAO;
import com.zstore.dao.TagDAO;
import com.zstore.model.Produk;
import com.zstore.model.Provider;
import com.zstore.model.Tag;
import com.zstore.util.FormatUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        Set<Long> activeTagIds = parseTagIds(req);
        String query = FormatUtil.trimOrEmpty(req.getParameter("q"));

        try {
            List<Provider> categories = providerDAO.findAll();
            List<Tag> tags = tagDAO.findAll();
            List<Produk> products = produkDAO.findFiltered(activeProviderId, new ArrayList<>(activeTagIds), query);

            String activeCategoryName = null;
            if (activeProviderId > 0) {
                for (Provider pr : categories) {
                    if (pr.getIdProvider() == activeProviderId) {
                        activeCategoryName = pr.getNama();
                        break;
                    }
                }
            }

            String ctx = req.getContextPath();
            List<String> activeTagNames = new ArrayList<>();
            Map<Long, String> tagToggleUrls = new LinkedHashMap<>();
            for (Tag t : tags) {
                long tid = t.getIdTag();
                if (activeTagIds.contains(tid)) {
                    activeTagNames.add(t.getNamaTag());
                }
                Set<Long> nextTags = new LinkedHashSet<>(activeTagIds);
                if (nextTags.contains(tid)) {
                    nextTags.remove(tid);
                } else {
                    nextTags.add(tid);
                }
                tagToggleUrls.put(tid, buildHomeUrl(ctx, activeProviderId, nextTags, query));
            }

            Map<Long, String> providerUrls = new LinkedHashMap<>();
            for (Provider cat : categories) {
                providerUrls.put(cat.getIdProvider(), buildHomeUrl(ctx, cat.getIdProvider(), activeTagIds, query));
            }

            req.setAttribute("categories", categories);
            req.setAttribute("tags", tags);
            req.setAttribute("products", products);
            req.setAttribute("activeProviderId", activeProviderId);
            req.setAttribute("activeTagIds", activeTagIds);
            req.setAttribute("activeTagsParam", joinTagIds(activeTagIds));
            req.setAttribute("activeTagNames", activeTagNames);
            req.setAttribute("activeCategoryName", activeCategoryName);
            req.setAttribute("searchQuery", query);
            req.setAttribute("tagToggleUrls", tagToggleUrls);
            req.setAttribute("clearTagsUrl", buildHomeUrl(ctx, activeProviderId, new LinkedHashSet<>(), query));
            req.setAttribute("providerUrls", providerUrls);
            req.setAttribute("allProvidersUrl", buildHomeUrl(ctx, 0, activeTagIds, query));
            req.setAttribute("pageTitle", "Z-Store | Toko Lisensi Digital");

            req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Database error loading catalog", e);
        }
    }

    private Set<Long> parseTagIds(HttpServletRequest req) {
        Set<Long> ids = new LinkedHashSet<>();
        String tagsParam = req.getParameter("tags");
        if (tagsParam != null && !tagsParam.trim().isEmpty()) {
            for (String part : tagsParam.split(",")) {
                long parsed = FormatUtil.parseLongOrDefault(part, 0);
                if (parsed > 0) {
                    ids.add(parsed);
                }
            }
        }
        String[] singleTags = req.getParameterValues("tag");
        if (singleTags != null) {
            for (String raw : singleTags) {
                for (String part : raw.split(",")) {
                    long parsed = FormatUtil.parseLongOrDefault(part, 0);
                    if (parsed > 0) {
                        ids.add(parsed);
                    }
                }
            }
        }
        return ids;
    }

    private String joinTagIds(Set<Long> tagIds) {
        StringBuilder sb = new StringBuilder();
        for (Long id : tagIds) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(id);
        }
        return sb.toString();
    }

    private String buildHomeUrl(String ctx, long providerId, Set<Long> tagIds, String query) {
        List<String> parts = new ArrayList<>();
        if (providerId > 0) {
            parts.add("provider=" + providerId);
        }
        if (tagIds != null && !tagIds.isEmpty()) {
            parts.add("tags=" + joinTagIds(tagIds));
        }
        if (query != null && !query.isEmpty()) {
            parts.add("q=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
        }
        return ctx + "/home" + (parts.isEmpty() ? "" : "?" + String.join("&", parts));
    }
}
