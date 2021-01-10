package me.piggypiglet.docdex.bootstrap;

import me.piggypiglet.docdex.bootstrap.framework.Registerable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

// ------------------------------
// Copyright (c) PiggyPiglet 2020
// https://www.piggypiglet.me
// ------------------------------
public final class UpdaterBootstrap extends DocDexBootstrap {
    @NotNull
    @Override
    protected List<Class<? extends Registerable>> provideRegisterables() {
        return Collections.emptyList();
    }
}
