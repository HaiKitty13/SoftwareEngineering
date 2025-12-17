package edu.bluejack24_2.nasigoyeng.ui.adapter

import edu.bluejack24_2.nasigoyeng.data.models.Recipe

interface OnRecipeActionListener {
    fun onLikeClicked(recipe: Recipe)
    fun onBookmarkClicked(recipe: Recipe)
    fun onRecipeClicked(recipe: Recipe)
}