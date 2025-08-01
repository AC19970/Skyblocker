package de.hysky.skyblocker.skyblock.item.tooltip;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.bazaar.ReorderHelper;
import de.hysky.skyblocker.skyblock.chocolatefactory.ChocolateFactorySolver;
import de.hysky.skyblocker.skyblock.dwarven.fossil.FossilSolver;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.*;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.container.ContainerMatcher;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TooltipManager {
	private static final TooltipAdder[] adders = new TooltipAdder[]{
			new LineSmoothener(), // Applies before anything else
			new TrueHexDisplay(),
			new TrueHexDyeScreenDisplay(),
			new SupercraftReminder(),
			ChocolateFactorySolver.INSTANCE,
			BitsHelper.INSTANCE,
			new FossilSolver(),
			new ReorderHelper(),
			new StackingEnchantProgressTooltip(0), //Would be best to have after the lore but the tech doesn't exist for that
			new NpcPriceTooltip(1),
			new BazaarPriceTooltip(2),
			new LBinTooltip(3),
			new AvgBinTooltip(4),
			new EssenceShopPrice(5),
			new CraftPriceTooltip(6),
			new EstimatedItemValueTooltip(7),
			new DungeonQualityTooltip(8),
			new MotesTooltip(9),
			new ObtainedDateTooltip(10),
			new MuseumTooltip(11),
			new ColorTooltip(12),
			new AccessoryTooltip(13),
			new DateCalculatorTooltip(14),
			new HuntingBoxPriceTooltip(15)
	};
	private static List<TooltipAdder> currentScreenAdders = new ArrayList<>();

	private TooltipManager() {
	}

	@Init
	public static void init() {
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
			if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> handledScreen) {
				addToTooltip(((HandledScreenAccessor) handledScreen).getFocusedSlot(), stack, lines);
			} else {
				addToTooltip(null, stack, lines);
			}
		});
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			onScreenChange(screen);
			ScreenEvents.remove(screen).register(ignored -> currentScreenAdders = List.of());
		});
	}

	private static void onScreenChange(Screen screen) {
		currentScreenAdders = Arrays.stream(adders)
				.filter(ContainerMatcher::isEnabled)
				.filter(adder -> adder.test(screen))
				.sorted(Comparator.comparingInt(TooltipAdder::getPriority))
				.toList();
	}

	/**
	 * <p>Adds additional text from all adders that are applicable to the current screen.
	 * This method is run on each tooltip render, so don't do any heavy calculations here.</p>
	 *
	 * <p>If you want to add info to the tooltips of multiple items, consider using a switch statement with {@code focusedSlot.getIndex()}</p>
	 *
	 * @param focusedSlot The slot that is currently focused by the cursor.
	 * @param stack       The stack to render the tooltip for.
	 * @param lines       The tooltip lines of the focused item. This includes the display name, as it's a part of the tooltip (at index 0).
	 * @return The lines list itself after all adders have added their text.
	 * @deprecated This method is public only for the sake of the mixin. Don't call directly, not that there is any point to it.
	 */
	@Deprecated
	public static List<Text> addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		if (!Utils.isOnSkyblock()) return lines;
		for (TooltipAdder adder : currentScreenAdders) {
			adder.addToTooltip(focusedSlot, stack, lines);
		}
		return lines;
	}
}
