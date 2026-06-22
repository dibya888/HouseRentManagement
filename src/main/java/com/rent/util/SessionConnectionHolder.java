package com.rent.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

/**
 * Caches a single real, already-decrypted SQLCipher connection per login
 * session, instead of opening (and SQLCipher-handshaking) a brand new
 * connection on every DAO call.
 *
 * Every DAO already does:
 *     try (Connection conn = DBUtil.connect()) { ... }
 *
 * That try-with-resources block calls conn.close() when it's done. If we
 * handed out the real, shared connection directly, the FIRST DAO call to
 * finish would close it for everyone else. To keep every existing DAO's
 * code completely unchanged, DBUtil.connect() instead returns a thin proxy
 * connection: every method except close() is forwarded to the real, shared
 * connection; close() is a no-op. The real connection is only ever closed
 * by invalidate(), called at logout / factory reset / key rotation.
 */
final class SessionConnectionHolder {

    private static Connection realConnection;
    private static String openedForUserId;

    private SessionConnectionHolder() {
    }

    static synchronized Connection getSharedConnection() {
        String currentUserId = CurrentSession.getUserId();

        if (realConnection == null
                || openedForUserId == null
                || !openedForUserId.equals(currentUserId)) {

            closeRealConnectionQuietly();

            realConnection = EncryptedDbConnectionFactory.open(
                    CurrentSession.getCurrentUserDatabasePath(),
                    CurrentSession.getDatabaseKey()
            );
            openedForUserId = currentUserId;
        }

        return wrapAsNonClosing(realConnection);
    }

    /**
     * Closes the real underlying connection (if any) and forgets it.
     * Must be called any time the on-disk database file or the
     * encryption key might change: logout, factory reset, restore-from-
     * backup, or any other operation that needs exclusive file access.
     */
    static synchronized void invalidate() {
        closeRealConnectionQuietly();
        openedForUserId = null;
    }

    private static void closeRealConnectionQuietly() {
        if (realConnection != null) {
            try {
                realConnection.close();
            } catch (Exception ignored) {
                // Best-effort close; nothing useful to do if this fails.
            }
            realConnection = null;
        }
    }

    /**
     * Wraps the real connection so that close() is a no-op, while every
     * other method call (createStatement, prepareStatement, etc.) is
     * forwarded unchanged. This lets every existing
     * try (Connection conn = DBUtil.connect()) { ... }
     * block keep working exactly as written.
     */
    private static Connection wrapAsNonClosing(Connection real) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (InvocationHandler) (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        return null;
                    }
                    if ("isClosed".equals(method.getName())) {
                        return real.isClosed();
                    }
                    try {
                        return method.invoke(real, args);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
        );
    }
}