package dev.projectflint.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** Optional Mod Menu entry point; never loaded when Mod Menu is absent. */
@Environment(EnvType.CLIENT)
public final class ProjectFlintModMenu implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new dev.projectflint.client.screen.ProjectFlintConfigScreen(parent);
    }
}
