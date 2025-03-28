package de.hysky.skyblocker.skyblock.dungeon.secrets;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Range;

import com.mojang.util.UndashedUuid;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.skyblock.dungeon.DungeonClass;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.ApiUtils;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class DungeonPlayerManager {
	/**
	 * @implNote Same as {@link de.hysky.skyblocker.skyblock.tabhud.widget.DungeonPlayerWidget#PLAYER_PATTERN}
	 */
	private static final Pattern PLAYER_TAB_PATTERN = Pattern.compile("\\[\\d*\\] (?:\\[[A-Za-z]+\\] )?(?<name>[A-Za-z0-9_]*) (?:.* )?\\((?<class>\\S*) ?(?<level>[LXVI]*)\\)");
	private static final Object2ReferenceMap<String, DungeonClass> PLAYER_CLASSES = new Object2ReferenceOpenHashMap<>(5);

	@Init
	public static void init() {
		DungeonEvents.DUNGEON_STARTED.register(DungeonPlayerManager::onDungeonStart);
	}

	public static DungeonClass getClassFromPlayer(PlayerEntity player) {
		return getClassFromPlayer(player.getGameProfile().getName());
	}

	public static DungeonClass getClassFromPlayer(String name) {
		return PLAYER_CLASSES.getOrDefault(name, DungeonClass.UNKNOWN);
	}

	private static void onDungeonStart() {
		reset();

		for (int i = 0; i < 5; i++) {
			Matcher matcher = getPlayerFromTab(i + 1);

			if (matcher != null) {
				String name = matcher.group("name");
				DungeonClass dungeonClass = DungeonClass.from(matcher.group("class"));

				if (dungeonClass != DungeonClass.UNKNOWN) PLAYER_CLASSES.put(name, dungeonClass);
			}
		}

		//Pre-fetch game profiles for rendering skins in the leap overlay
		for (Object2ReferenceMap.Entry<String, DungeonClass> entry : PLAYER_CLASSES.object2ReferenceEntrySet()) {
			CompletableFuture.runAsync(() -> {
				UUID uuid = UndashedUuid.fromString(ApiUtils.name2Uuid(entry.getKey()));
				MinecraftClient.getInstance().getSessionService().fetchProfile(uuid, false);
			});
		}
	}

	private static void reset() {
		PLAYER_CLASSES.clear();
	}

	private static Matcher getPlayerFromTab(@Range(from = 1, to = 5) int index) {
		return PlayerListManager.regexAt(1 + (index - 1) * 4, PLAYER_TAB_PATTERN);
	}
}
