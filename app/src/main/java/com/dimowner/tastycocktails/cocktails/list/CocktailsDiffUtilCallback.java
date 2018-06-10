package com.dimowner.tastycocktails.cocktails.list;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class CocktailsDiffUtilCallback extends DiffUtil.Callback {

	private final List<ListItem> oldList;
	private final List<ListItem> newList;

	public CocktailsDiffUtilCallback(List<ListItem> oldList, List<ListItem> newList) {
		this.oldList = oldList;
		this.newList = newList;
	}

	@Override
	public int getOldListSize() {
		return oldList.size();
	}

	@Override
	public int getNewListSize() {
		return newList.size();
	}

	@Override
	public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
		ListItem oldProduct = oldList.get(oldItemPosition);
		ListItem newProduct = newList.get(newItemPosition);
		return oldProduct.getId() == newProduct.getId();
	}

	@Override
	public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
		ListItem oldProduct = oldList.get(oldItemPosition);
		ListItem newProduct = newList.get(newItemPosition);

		return oldProduct.getName().equals(newProduct.getName())
//				&& oldProduct.getCategory().equals(newProduct.getCategory())
//				&& oldProduct.getAlcoholic().equals(newProduct.getAlcoholic())
//				&& oldProduct.getGlass().equals(newProduct.getGlass())
				&& oldProduct.getHistory() == newProduct.getHistory()
				&& oldProduct.isFavorite() == newProduct.isFavorite();
	}
}
