package com.zstore.servlet;

import com.google.gson.Gson;
import com.zstore.model.Pelanggan;
import com.zstore.storage.S3Client;
import com.zstore.util.FormatUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Issues short-lived AWS SigV4 Presigned PUT URLs so browsers upload images
 * directly to Neon Object Storage without proxying large files through the VPS.
 */
public class UploadPresignServlet extends HttpServlet {

    private static final Gson GSON = new Gson();
    private final S3Client s3Client = new S3Client();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        HttpSession session = req.getSession(false);
        Pelanggan user = session != null ? (Pelanggan) session.getAttribute("user") : null;
        if (user == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Silakan login terlebih dahulu.");
            return;
        }

        String folder = FormatUtil.trimOrEmpty(req.getParameter("folder")).toLowerCase();
        if ("products".equals(folder) || "providers".equals(folder)) {
            if (!user.isAdmin()) {
                writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Akses khusus admin.");
                return;
            }
        } else if (!"avatars".equals(folder)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Folder upload tidak valid.");
            return;
        }

        String filename = FormatUtil.trimOrEmpty(req.getParameter("filename"));
        String contentType = FormatUtil.trimOrEmpty(req.getParameter("content_type"));
        long size = FormatUtil.parseLongOrDefault(req.getParameter("size"), 0);

        try {
            S3Client.PresignedUpload presigned = s3Client.createPresignedPutUrl(folder, filename, contentType, size);
            Map<String, Object> out = new HashMap<>();
            out.put("uploadUrl", presigned.getUploadUrl());
            out.put("publicUrl", presigned.getPublicUrl());
            out.put("contentType", presigned.getContentType());
            resp.getWriter().write(GSON.toJson(out));
        } catch (IOException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void writeError(HttpServletResponse resp, int status, String msg) throws IOException {
        resp.setStatus(status);
        Map<String, Object> out = new HashMap<>();
        out.put("error", msg);
        resp.getWriter().write(GSON.toJson(out));
    }
}

