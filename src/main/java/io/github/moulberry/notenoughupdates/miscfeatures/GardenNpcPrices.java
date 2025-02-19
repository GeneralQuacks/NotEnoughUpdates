/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.util.ItemResolutionQuery;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NEUAutoSubscribe
public class GardenNpcPrices {

	private final Pattern itemRegex = Pattern.compile("§5§o §.([a-zA-Z \\-]+)(?:§8x(\\d+))?");
	//§5§o §aEnchanted Cactus Green §8x421
	//§5§o §aEnchanted Hay Bale §8x62
	//§5§o §9Enchanted Cookie §8x4
	//§5§o §9Tightly-Tied Hay Bale

	private Map<List<String>, List<String>> prices = new HashMap<>();

	@SubscribeEvent
	public void onGardenNpcPrices(ItemTooltipEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.gardenNpcPrice) return;
		if (event.toolTip.size() <= 2 || event.itemStack.getItem() != Item.getItemFromBlock(Blocks.stained_hardened_clay)) return;

		List<String> tooltipCopy = new ArrayList<>(event.toolTip);
		if (prices.get(tooltipCopy) == null) {
			for (int i = 2; i < event.toolTip.size(); i++) {
				Matcher matcher = itemRegex.matcher(event.toolTip.get(i));

				if (matcher.matches()) {
					int amount = 1;
					if (matcher.group(2) != null) amount = Integer.parseInt(matcher.group(2));

					double cost = calculateCost(ItemResolutionQuery.findInternalNameByDisplayName(matcher.group(1).trim(), false), amount);
					event.toolTip.set(i, event.toolTip.get(i) + " §7(§6" + (cost == 0 ? "?" : Utils.shortNumberFormat(cost, 0)) + "§7 coins)");
				} else {
					prices.put(tooltipCopy, event.toolTip);
				}
			}
		} else {
			event.toolTip.clear();
			event.toolTip.addAll(prices.get(tooltipCopy));
		}
	}
	public double calculateCost(String internalName, int amount) {
		double price = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarOrBin(internalName, false);
		if (price != -1) {
			return price * amount;
		}
		return 0d;
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		prices.clear();
	}
}
