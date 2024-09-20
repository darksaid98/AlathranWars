package com.github.alathra.alathranwars.database;

import com.github.alathra.alathranwars.utility.DB;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.UUID;

/**
 * Convenience class containing utility methods for querying databases.
 */
public abstract class QueryUtil {
    /**
     * Used in {@link #transaction(Connection, LambdaInterface)}.
     */
    public interface LambdaInterface {
        void execute(Connection connection, DSLContext dslContext) throws SQLException, DataAccessException;
    }

    /**
     * A quick wrapper for creating a transaction. The changes executed in the lambda context ar rolled back if any error occurs.
     * @param con A SQL connection
     * @param lambda A lambda method which defines what code to execute
     * @throws SQLException Exception
     * @throws DataAccessException Exception
     */
    public static void transaction(Connection con, LambdaInterface lambda) throws SQLException, DataAccessException {
        DSLContext context = DB.getContext(con);

        final Savepoint savePoint = con.setSavepoint();
        final boolean autoCommit = con.getAutoCommit();

        try {
            con.setAutoCommit(false);

            lambda.execute(con, context);

            con.commit();
        } catch (SQLException | DataAccessException e) {
            con.rollback(savePoint);
        } finally {
            con.setAutoCommit(autoCommit);
        }
    }

    /**
     * Convert uuid to an array of bytes.
     *
     * @param uuid the uuid
     * @return the byte array
     */
    public static byte[] fromUUIDToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    /**
     * Convert byte array to uuid.
     *
     * @param bytes the byte array
     * @return the uuid
     */
    public static UUID fromBytesToUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }
}
