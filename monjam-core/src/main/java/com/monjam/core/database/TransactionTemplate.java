package com.monjam.core.database;

import com.mongodb.client.ClientSession;
import com.monjam.core.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class TransactionTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionTemplate.class);

    public void executeInTransaction(Context context, Consumer<Context> callback) {
        ClientSession session = context.getSession();
        try {
            if (session.hasActiveTransaction()) {
                LOG.warn("There is an already active transaction");
                session.commitTransaction();
            }
            session.startTransaction();

            callback.accept(context);

            session.commitTransaction();
            LOG.debug("Transaction was committed");
        } catch (Exception exception) {
            if (session.hasActiveTransaction()) {
                session.abortTransaction();
            }
            LOG.warn("Transaction was rollback", exception);
            throw exception;
        }
    }
}
