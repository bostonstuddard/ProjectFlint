package dev.projectflint.client;

import dev.projectflint.ProjectFlint;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.state.properties.ChestType;

public final class WoodenChestMaterials {
	private static final Material SINGLE=new Material(Sheets.CHEST_SHEET,ProjectFlint.id("entity/chest/wooden_chest"));
	private static final Material LEFT=new Material(Sheets.CHEST_SHEET,ProjectFlint.id("entity/chest/wooden_chest_left"));
	private static final Material RIGHT=new Material(Sheets.CHEST_SHEET,ProjectFlint.id("entity/chest/wooden_chest_right"));
	private WoodenChestMaterials(){}
	public static Material choose(ChestType type){return type==ChestType.LEFT?LEFT:type==ChestType.RIGHT?RIGHT:SINGLE;}
}
