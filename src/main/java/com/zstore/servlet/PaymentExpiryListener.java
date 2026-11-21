package com.zstore.servlet;

import com.zstore.config.DBConnection;
import com.zstore.dao.TransaksiDAO;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Periodically expires stale Pending invoices and releases reserved license stocks.
 */
public class PaymentExpiryListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "zstore-payment-expiry-worker");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                transaksiDAO.expireStalePendingTransactions();
            } catch (SQLException ignored) {
                // Database may not be reachable yet on container startup; next tick will retry.
            }
        }, 1, 2, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        DBConnection.shutdownPool();
    }
}

