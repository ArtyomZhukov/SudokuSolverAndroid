package com.zhukovartemvl.sudokusolver.domain.model

/**
 * Represents an image search match, containing the match area and the matching score.
 */
data class Match(val region: Region, val score: Double) : Comparable<Match> {
    override fun compareTo(other: Match): Int {
        return region.compareTo(other.region)
    }
}
