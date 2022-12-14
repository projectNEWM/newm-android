package io.newm.shared.models

data class Song (
    val image: String,
    val title: String,
    val artist: Artist,
    val isNft: Boolean,
    val id: String,
    var favorited: Boolean,
    var duration: Int,
    var genre: Genre,
)
