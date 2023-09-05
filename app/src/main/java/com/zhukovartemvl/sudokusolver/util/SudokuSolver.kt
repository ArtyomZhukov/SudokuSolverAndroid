package com.zhukovartemvl.sudokusolver.util

// https://github.com/thomasnield/kotlin-sudoku-solver
object SudokuSolver {

    private var grid = listOf<GridCell>()

    fun solve(sudoku: List<Int>): List<Int> {
        // generates the entire Sudoku grid of GridCells
        grid = (0..2).asSequence().flatMap { parentX -> (0..2).asSequence().map { parentY -> parentX to parentY } }
            .flatMap { (parentX, parentY) ->
                (0..2).asSequence().flatMap { x -> (0..2).asSequence().map { y -> x to y } }
                    .map { (x, y) -> GridCell(parentX, parentY, x, y) }
            }.toList()

        grid.forEach { gridCell ->
            val number = sudoku[gridCell.globalIndex]
            gridCell.value = if (number == 0) null else number
        }

        // Order Sudoku cells by count of how many candidate values they have left
        // Starting with the most constrained cells (with fewest possible values left) will greatly reduce the search space
        // Fixed cells will have only 1 candidate and will be processed first
        val sortedByCandidateCount = grid.asSequence()
            .sortedBy {
                val value = it.candidatesLeft.count()
                value
            }
            .toList()

        // hold onto fixed values snapshot as they are going to mutate during animation
        val fixedCellValues = grid.asSequence().map { it to it.value }
            .filter { it.second != null }
            .toMap()

        // this is a recursive function for exploring nodes in a branch-and-bound tree
        fun traverse(index: Int, currentBranch: GridCellBranch): GridCellBranch? {

            val nextCell = sortedByCandidateCount[index + 1]

            val fixedValue = fixedCellValues[nextCell]

            // we want to explore possible values 1..9 unless this cell is fixed already
            // infeasible values should terminate the branch
            val range = if (fixedValue == null) (1..9) else (fixedValue..fixedValue)

            for (candidateValue in range) {

                val nextBranch = GridCellBranch(candidateValue, nextCell, currentBranch)

                if (nextBranch.isSolution)
                    return nextBranch

                if (nextBranch.isContinuable) {
                    val terminalBranch = traverse(index + 1, nextBranch)
                    if (terminalBranch?.isSolution == true) {
                        return terminalBranch
                    }
                }
            }
            return null
        }

        // start with the first sorted Sudoku cell and set it as the seed
        val seed = sortedByCandidateCount.first()
            .let { GridCellBranch(it.value ?: 1, it) }

        // recursively traverse from the seed and get a solution
        val solution = traverse(0, seed)

        // apply solution back to game board
        solution?.traverseBackwards?.forEach { it.applyToCell() }

        val results = buildMap {
            grid.forEach { gridCell ->
                put(gridCell.globalIndex, gridCell.value ?: 0)
            }
        }

        return buildList {
            repeat(81) { index ->
                add(results[index] ?: 0)
            }
        }
    }

    data class GridCell(val squareX: Int, val squareY: Int, val x: Int, val y: Int, var value: Int? = 0) {

        private val allRow by lazy { grid.filter { it.y == y && it.squareY == squareY }.toSet() }
        private val allColumn by lazy { grid.filter { it.x == x && it.squareX == squareX }.toSet() }
        private val allSquare by lazy { grid.filter { it.squareY == squareY && it.squareX == squareX }.toSet() }

        val candidatesLeft
            get() = if (value != null) {
                setOf()
            } else {
                allRow.asSequence()
                    .plus(allColumn.asSequence())
                    .plus(allSquare.asSequence())
                    .map { it.value }
                    .filterNotNull()
                    .distinct()
                    .toSet().let { taken -> (1..9).asSequence().filter { it !in taken } }.toSet()
            }

        val globalIndex: Int = (squareY * 3 + y) * 9 + squareX * 3 + x
    }

    class GridCellBranch(
        private val selectedValue: Int,
        private val gridCell: GridCell,
        private val previous: GridCellBranch? = null
    ) {

        private val x = gridCell.x
        private val y = gridCell.y
        private val squareX = gridCell.squareX
        private val squareY = gridCell.squareY

        // traverses this entire branch backwards, revealing the solution so far
        val traverseBackwards = generateSequence(this) { it.previous }.toList()

        // Be able to retrieve a given row, column, or square of assigned values from this branch
        private val allRow = traverseBackwards.filter { it.y == y && it.squareY == squareY }
        private val allColumn = traverseBackwards.filter { it.x == x && it.squareX == squareX }
        private val allSquare = traverseBackwards.filter { it.squareY == squareY && it.squareX == squareX }

        // Determines whether our current branch does not break any Sudoku rules
        private val constraintsMet = allRow.count { it.selectedValue == selectedValue } <= 1
                && allColumn.count { it.selectedValue == selectedValue } <= 1
                && allSquare.count { it.selectedValue == selectedValue } <= 1

        // Determines whether this branch should continue to be traversed
        val isContinuable = constraintsMet && traverseBackwards.count() < 81

        // Determines if this branch is a full solution
        val isSolution = traverseBackwards.count() == 81 && constraintsMet

        // Animations put back on the game board
        fun applyToCell() {
            gridCell.value = selectedValue
        }

        init {
            if (isContinuable) applyToCell()
        }
    }
}
