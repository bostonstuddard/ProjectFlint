package dev.projectflint.recipe;

import java.util.Set;

/**
 * Server-side safeguard for obsolete or material-duplicating recipe IDs.
 * The cleanup script removes the JSONs, but this must also block old JSONs
 * left behind by incremental ZIP extraction, old build resources or datapacks.
 * These IDs are never executable in the Clay or Wooden Grinder, nor visible
 * in the server recipe registry (including PF-EMI/recipe discovery).
 */
public final class RetiredRecipePolicy {
    private static final Set<String> RETIRED_PATHS = Set.of(
        "loose_dirt_from_slabs",             // 2 loose dirt -> 6 slabs -> 3 loose dirt
        "loose_mud_bricks_from_slabs",       // 2 loose bricks -> 6 slabs -> 3 loose bricks
        "sanded_wood_slabs_from_grinded",    // 3 sanded -> 2 grinded -> 4 sanded
        "primitive_wooden_sheet_from_stripped_log", // retired v0.31.67-b shortcut
        "personal_pmi_sheets"                // retired personal-crafting shortcut
    );

    private RetiredRecipePolicy() {}

    public static boolean isRetired(String namespace, String path) {
        return "projectflint".equals(namespace)
            && (path.endsWith("_stripped_log_from_slabs") || RETIRED_PATHS.contains(path));
    }
}
