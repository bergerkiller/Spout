/*
 * This file is part of SpoutAPI.
 *
 * Copyright (c) 2011-2012, SpoutDev <http://www.spout.org/>
 * SpoutAPI is licensed under the SpoutDev License Version 1.
 *
 * SpoutAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * SpoutAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spout.api.inventory.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.spout.api.Spout;

import org.spout.api.material.Material;
import org.spout.api.plugin.Plugin;

public class SimpleRecipeManager implements RecipeManager {
	private final Map<Plugin, Map<Integer, RecipeTree>> registeredShapedRecipes = new ConcurrentHashMap<Plugin, Map<Integer, RecipeTree>>();
	private final Map<Plugin, Map<Integer, Set<ShapelessRecipe>>> registeredShapelessRecipes = new ConcurrentHashMap<Plugin, Map<Integer, Set<ShapelessRecipe>>>();
	private final Map<Integer, Set<Recipe>> allRecipes = new ConcurrentHashMap<Integer, Set<Recipe>>();
	private final Map<Integer, RecipeTree> allShapedRecipes = new ConcurrentHashMap<Integer, RecipeTree>();
	private final Map<Integer, Set<ShapelessRecipe>> allShapelessRecipes = new ConcurrentHashMap<Integer, Set<ShapelessRecipe>>();

	@Override
	public boolean register(Recipe recipe) {
		boolean failed = false;
		if (recipe instanceof ShapedRecipe) {
			failed = !registerShaped((ShapedRecipe) recipe);
		} else if (recipe instanceof ShapelessRecipe) {
			failed = !registerShapeless((ShapelessRecipe) recipe);
		} else {
			Spout.log("Unknown recipe type!");
		}
		if (allRecipes.get(recipe.getIngredients().size()) == null) {
			allRecipes.put(recipe.getIngredients().size(), Collections.newSetFromMap(new ConcurrentHashMap<Recipe, Boolean>()));
		}
		failed = !allRecipes.get(recipe.getIngredients().size()).add(recipe) || failed;
		return !failed;
	}

	private boolean registerShaped(ShapedRecipe recipe) {
		boolean failed = false;
		Plugin plugin = recipe.getPlugin();
		if (plugin != null) {
			ConcurrentHashMap<Integer, RecipeTree> recipesMap = (ConcurrentHashMap<Integer, RecipeTree>) registeredShapedRecipes.get(plugin);
			if (recipesMap == null) {
				recipesMap = new ConcurrentHashMap<Integer, RecipeTree>();
				registeredShapedRecipes.put(plugin, recipesMap);
			}
			if (recipesMap.get(recipe.getIngredients().size()) == null) {
				RecipeTree recipes = new RecipeTree();
				registeredShapedRecipes.get(plugin).put(recipe.getIngredients().size(), recipes);
			}
			failed = !registeredShapedRecipes.get(plugin).get(recipe.getIngredients().size()).addRecipe(recipe) || failed;
		}

		if (allShapedRecipes.get(recipe.getIngredients().size()) == null) {
			RecipeTree recipes = new RecipeTree();
			allShapedRecipes.put(recipe.getIngredients().size(), recipes);
		}
		failed = !allShapedRecipes.get(recipe.getIngredients().size()).addRecipe(recipe) || failed;
		return !failed;
	}

	private boolean registerShapeless(ShapelessRecipe recipe) {
		boolean failed = false;
		Plugin plugin = recipe.getPlugin();
		if (plugin != null) {
			ConcurrentHashMap<Integer, Set<ShapelessRecipe>> recipesMap = (ConcurrentHashMap<Integer, Set<ShapelessRecipe>>) registeredShapelessRecipes.get(plugin);
			if (recipesMap == null) {
				recipesMap = new ConcurrentHashMap<Integer, Set<ShapelessRecipe>>();
				registeredShapelessRecipes.put(plugin, recipesMap);
			}
			if (recipesMap.get(recipe.getIngredients().size()) == null) {
				Set<ShapelessRecipe> recipes = Collections.newSetFromMap(new ConcurrentHashMap<ShapelessRecipe, Boolean>());
				registeredShapelessRecipes.get(plugin).put(recipe.getIngredients().size(), recipes);
			}
			failed = !registeredShapelessRecipes.get(plugin).get(recipe.getIngredients().size()).add(recipe) || failed;
		}

		if (allShapelessRecipes.get(recipe.getIngredients().size()) == null) {
			Set<ShapelessRecipe> recipes = Collections.newSetFromMap(new ConcurrentHashMap<ShapelessRecipe, Boolean>());
			allShapelessRecipes.put(recipe.getIngredients().size(), recipes);
		}
		failed = !allShapelessRecipes.get(recipe.getIngredients().size()).add(recipe) || failed;
		return !failed;
	}

	@Override
	public boolean registerAll(Set<Recipe> recipes) {
		boolean failed = false;
		for (Recipe recipe : recipes) {
			failed |= register(recipe);
		}
		return failed;
	}

	@Override
	public boolean remove(Recipe recipe) {
		if (allRecipes.get(recipe.getIngredients().size()) == null) {
			return false;
		}
		boolean failed = false;
		if (recipe instanceof ShapedRecipe) {
			failed = !removeShaped((ShapedRecipe) recipe);
		} else if (recipe instanceof ShapelessRecipe) {
			failed = !removeShapeless((ShapelessRecipe) recipe);
		}
		failed = !allRecipes.get(recipe.getIngredients().size()).remove(recipe) || failed;
		return !failed;
	}

	private boolean removeShaped(ShapedRecipe recipe) {
		boolean failed = false;
		Plugin plugin = recipe.getPlugin();
		if (plugin != null) {
			if (!registeredShapedRecipes.containsKey(plugin)) {
				return false;
			}
			if (!registeredShapedRecipes.get(plugin).containsKey(recipe.getIngredients().size())) {
				return false;
			}
			failed = !registeredShapedRecipes.get(plugin).get(recipe.getIngredients().size()).removeRecipe(recipe) || failed;
		}
		if (!allShapedRecipes.containsKey(recipe.getIngredients().size())) {
			return false;
		}
		failed = !allShapedRecipes.get(recipe.getIngredients().size()).removeRecipe(recipe) || failed;
		return !failed;
	}

	private boolean removeShapeless(ShapelessRecipe recipe) {
		boolean failed = false;
		Plugin plugin = recipe.getPlugin();
		if (plugin != null) {
			if (!registeredShapelessRecipes.containsKey(plugin)) {
				return false;
			}
			if (!registeredShapelessRecipes.get(plugin).containsKey(recipe.getIngredients().size())) {
				return false;
			}
			if (!registeredShapelessRecipes.get(plugin).get(recipe.getIngredients().size()).contains(recipe)) {
				return false;
			}
			failed = !registeredShapelessRecipes.get(recipe.getPlugin()).get(recipe.getIngredients().size()).remove(recipe) || failed;
		}
		if (!allShapelessRecipes.containsKey(recipe.getIngredients().size())) {
			return false;
		}
		failed = !allShapelessRecipes.get(recipe.getIngredients().size()).remove(recipe) || failed;
		return !failed;
	}

	@Override
	public void clear() {
		registeredShapedRecipes.clear();
		registeredShapelessRecipes.clear();
		allRecipes.clear();
		allShapedRecipes.clear();
		allShapelessRecipes.clear();
	}

	@Override
	public Set<Recipe> getAllRecipes() {
		Set<Recipe> all = new HashSet<Recipe>();
		for (Set<Recipe> recipes : allRecipes.values()) {
			all.addAll(recipes);
		}
		return Collections.unmodifiableSet(all);
	}

	@Override
	public boolean replaceRecipe(Recipe oldRecipe, Recipe newRecipe) {
		return remove(oldRecipe) && register(newRecipe);
	}

	@Override
	public Set<Recipe> getShapedRecipes(Plugin plugin) {
		Set<Recipe> set = new HashSet<Recipe>();
		if (registeredShapedRecipes.containsKey(plugin)) {
			for (int i : registeredShapedRecipes.get(plugin).keySet()) {
				set.addAll(registeredShapedRecipes.get(plugin).get(i).getAllRecipes());
			}
		}
		return set;
	}

	@Override
	public Set<Recipe> getShapelessRecipes(Plugin plugin) {
		Set<Recipe> set = new HashSet<Recipe>();
		if (registeredShapelessRecipes.containsKey(plugin)) {
			for (int i : registeredShapelessRecipes.get(plugin).keySet()) {
				set.addAll(registeredShapelessRecipes.get(plugin).get(i));
			}
		}
		return set;
	}

	@Override
	public ShapedRecipe matchShapedRecipe(List<List<Material>> materials) {
		Set<Material> set = new HashSet<Material>();
		List<List<Material>> list = new ArrayList<List<Material>>();
		List<List<Material>> parentList = new ArrayList<List<Material>>();
		for (List<Material> materialsRow : materials) {
			List<Material> parentRow = new ArrayList<Material>();
			List<Material> row = new ArrayList<Material>();
			for (Material m : materialsRow) {
				row.add(m);
				Material parent = m;
				if (m != null) {
					set.add(m);
					if (m.isSubMaterial()) {
						parent = m.getParentMaterial();
					}
				}
				parentRow.add(parent);
			}
			parentList.add(parentRow);
			list.add(row);
		}

		if (!allShapedRecipes.containsKey(set.size())) {
			return null;
		}
		ShapedRecipe recipe = allShapedRecipes.get(set.size()).matchShapedRecipe(list, true);
		if (recipe == null) {
			recipe = allShapedRecipes.get(set.size()).matchShapedRecipe(parentList, false);
		}
		return recipe;
	}

	@Override
	public ShapelessRecipe matchShapelessRecipe(Collection<Material> materials) {
		Set<Material> set = new HashSet<Material>();
		List<Material> parentList = new ArrayList<Material>();
		for (Material m : materials) {
			if (m.isSubMaterial()) {
				m = m.getParentMaterial();
			}
			parentList.add(m);
		}
		set.addAll(parentList);
		set.removeAll(Collections.singletonList(null));

		ShapelessRecipe recipe = null;
		if (!allShapelessRecipes.containsKey(set.size())) {
			return null;
		}
		for (ShapelessRecipe r : allShapelessRecipes.get(set.size())) {
			if (r.getIncludeData()) {
				if (materials.containsAll(r.getIngredients()) && r.getIngredients().containsAll(materials)) {
					recipe = r;
					break;
				}
			} else {
				if (parentList.containsAll(r.getIngredients()) && r.getIngredients().containsAll(parentList)) {
					recipe = r;
					break;
				}
			}
		}
		return recipe;
	}

	@Override
	public ShapedRecipe matchShapedRecipe(Plugin plugin, List<List<Material>> materials) {
		Set<Material> set = new HashSet<Material>();
		List<List<Material>> parentList = new ArrayList<List<Material>>();
		for (List<Material> row : materials) {
			List<Material> parentRow = new ArrayList<Material>();
			for (Material m : row) {
				parentRow.add(m);
				if (m != null) {
					set.add(m);
				}
			}
			parentList.add(parentRow);
		}

		ShapedRecipe recipe = null;
		if (registeredShapedRecipes.containsKey(plugin) && registeredShapedRecipes.get(plugin).containsKey(set.size())) {
			recipe = registeredShapedRecipes.get(plugin).get(set.size()).matchShapedRecipe(materials, true);
			if (recipe == null) {
				recipe = registeredShapedRecipes.get(plugin).get(set.size()).matchShapedRecipe(parentList, false);
			}
		}

		if (recipe == null) {
			recipe = matchShapedRecipe(materials);
		}

		return recipe;
	}

	@Override
	public ShapelessRecipe matchShapelessRecipe(Plugin plugin, Collection<Material> materials) {
		Set<Material> set = new HashSet<Material>();
		List<Material> parentList = new ArrayList<Material>();
		for (Material m : materials) {
			if (m.isSubMaterial()) {
				m = m.getParentMaterial();
			}
			parentList.add(m);
		}
		set.addAll(parentList);
		set.removeAll(Collections.singletonList(null));

		ShapelessRecipe recipe = null;
		if (registeredShapelessRecipes.containsKey(plugin) && registeredShapelessRecipes.get(plugin).containsKey(set.size())) {
			for (ShapelessRecipe r : registeredShapelessRecipes.get(plugin).get(set.size())) {
				if (r.getIncludeData()) {
					if (materials.containsAll(r.getIngredients()) && r.getIngredients().containsAll(materials)) {
						recipe = r;
						break;
					}
				} else {
					if (parentList.containsAll(r.getIngredients()) && r.getIngredients().containsAll(parentList)) {
						recipe = r;
						break;
					}
				}
			}
		}
		if (recipe == null) {
			recipe = matchShapelessRecipe(materials);
		}

		return recipe;
	}
}
