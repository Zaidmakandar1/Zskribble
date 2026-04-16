package com.example.zskribble.data

object WordDatabase {
    private val easyWords = listOf(
        "cat", "dog", "sun", "moon", "star", "tree", "car", "house", "book", "phone",
        "apple", "ball", "fish", "bird", "flower", "chair", "table", "door", "window", "clock"
    )
    
    private val mediumWords = listOf(
        "elephant", "guitar", "pizza", "rainbow", "mountain", "ocean", "castle", "dragon",
        "rocket", "butterfly", "umbrella", "camera", "laptop", "bicycle", "airplane",
        "sandwich", "penguin", "volcano", "treasure", "lighthouse"
    )
    
    private val hardWords = listOf(
        "astronaut", "microscope", "pyramid", "helicopter", "waterfall", "skyscraper",
        "submarine", "telescope", "dinosaur", "chandelier", "saxophone", "parachute",
        "crocodile", "trampoline", "refrigerator", "escalator", "kangaroo", "octopus",
        "pineapple", "rollercoaster"
    )
    
    fun getRandomWord(): String {
        val allWords = easyWords + mediumWords + hardWords
        return allWords.random()
    }
    
    fun getWordHint(word: String): String {
        return word.map { if (it.isLetter()) '_' else it }.joinToString(" ")
    }
}
