package me.piggypiglet.docdex.shutdown;

import co.aikar.idb.Database;
import com.google.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ------------------------------
// Copyright (c) PiggyPiglet 2020
// https://www.piggypiglet.me
// ------------------------------
public final class ShutdownHook extends Thread {
    private static final Logger JDA = LoggerFactory.getLogger("JDA");
    private static final Logger MYSQL = LoggerFactory.getLogger("MySQL");
    private static final Logger SHUTDOWN_SPACER = LoggerFactory.getLogger("Shutdown Spacer");

    private final ShardManager shardManager;
    private final Database database;

    @Inject
    public ShutdownHook(@NotNull final ShardManager shardManager, @NotNull final Database database) {
        this.shardManager = shardManager;
        this.database = database;
    }

    @Override
    public void run() {
        shardManager.shutdown();
        JDA.info("Shut down JDA.");

        database.close();
        MYSQL.info("Shut down MySQL.");

        for (int i = 0; i < 5; ++i) {
            SHUTDOWN_SPACER.info("");
        }
    }
}
