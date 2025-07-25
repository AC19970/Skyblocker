package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;

/**
 * The Skyblock Crafting Tab which handles the mouse clicks & rendering for the results page and the search field.
 */
record SkyblockCraftingTab(SkyblockRecipeBookWidget recipeBook, ItemStack icon, SkyblockRecipeResults results) implements RecipeTab {
	static final ItemStack CRAFTING_TABLE = new ItemStack(Items.CRAFTING_TABLE);

	@Override
	public void initialize(MinecraftClient client, int parentLeft, int parentTop) {
		results.initialize(client, parentLeft, parentTop);
	}

	@Override
	public void draw(DrawContext context, int x, int y, int mouseX, int mouseY, float delta) {
		assert recipeBook.searchField != null;

		if (ItemRepository.filesImported()) {
			recipeBook.searchField.render(context, mouseX, mouseY, delta);
			recipeBook.filterOption.render(context, mouseX, mouseY, delta);
			results.draw(context, x, y, mouseX, mouseY, delta);
		} else {
			//68 is from 137 / 2 and 137 is the height from which the page flip buttons are rendered
			context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, "Loading...", x + (SkyblockRecipeBookWidget.IMAGE_WIDTH / 2), y + 68, 0xFFFFFFFF);
		}
	}

	@Override
	public void drawTooltip(DrawContext context, int x, int y) {
		if (ItemRepository.filesImported()) results.drawTooltip(context, x, y);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (ItemRepository.filesImported()) {
			if (results.mouseClicked(mouseX, mouseY, button)) {
				return true;
			} else {
				if (recipeBook.searchField != null) {
					boolean magnifyingGlassClicked = recipeBook.searchFieldRect != null && recipeBook.searchFieldRect.contains(MathHelper.floor(mouseX), MathHelper.floor(mouseY));

					if (magnifyingGlassClicked || recipeBook.searchField.mouseClicked(mouseX, mouseY, button)) {
						results.closeRecipeView();
						recipeBook.searchField.setFocused(true);

						return true;
					}
					recipeBook.searchField.setFocused(false);
					return recipeBook.filterOption.mouseClicked(mouseX, mouseY, button);
				}
			}
		}

		return false;
	}

	@Override
	public boolean keyPressed(double mouseX, double mouseY, int keyCode, int scanCode, int modifiers) {
		return ItemRepository.filesImported() ? this.results.keyPressed(mouseX, mouseY, keyCode, scanCode, modifiers) : false;
	}

	@Override
	public void updateSearchResults(String query, FilterOption filterOption, boolean refresh) {
		if (ItemRepository.filesImported()) results.updateSearchResults(query, filterOption, refresh);
	}
}
