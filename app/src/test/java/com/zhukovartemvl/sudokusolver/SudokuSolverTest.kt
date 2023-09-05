package com.zhukovartemvl.sudokusolver

import com.zhukovartemvl.sudokusolver.interactor.SudokuSolverInteractor
import com.zhukovartemvl.sudokusolver.model.Cell
import org.junit.Test

class SudokuSolverTest {

    private val interactor: SudokuSolverInteractor = SudokuSolverInteractor()

    @Test
    fun test1() {
        testSolver(
            startValues = listOf(
                0, 3, 1, 7, 0, 0, 0, 9, 0,
                6, 7, 0, 9, 2, 0, 1, 0, 4,
                0, 4, 9, 0, 0, 1, 3, 7, 5,
                5, 0, 0, 6, 0, 0, 7, 0, 9,
                0, 0, 2, 1, 7, 9, 5, 3, 0,
                0, 8, 0, 0, 0, 0, 0, 0, 1,
                7, 5, 4, 3, 8, 6, 9, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 5, 0,
                1, 0, 0, 0, 0, 0, 6, 0, 0
            ),
        )
    }

    @Test
    fun test2() {
        testSolver(
            startValues = listOf(
                0, 6, 0, 0, 0, 0, 0, 0, 5,
                0, 7, 5, 8, 0, 9, 0, 3, 1,
                0, 3, 0, 4, 0, 0, 0, 0, 7,
                0, 0, 0, 0, 0, 7, 0, 0, 0,
                0, 2, 0, 3, 0, 5, 0, 7, 0,
                0, 0, 0, 9, 0, 0, 0, 0, 0,
                3, 0, 0, 0, 0, 6, 0, 9, 0,
                9, 5, 0, 1, 0, 8, 4, 6, 0,
                6, 0, 0, 0, 0, 0, 0, 5, 0
            ),
        )
    }

    @Test
    fun test3() {
        testSolver(
            startValues = listOf(
                0, 0, 5, 3, 0, 0, 0, 7, 1,
                0, 0, 2, 0, 7, 0, 0, 8, 0,
                0, 0, 0, 8, 0, 0, 3, 0, 9,
                9, 0, 0, 6, 0, 8, 2, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 8, 9, 0, 1, 0, 0, 4,
                6, 0, 9, 0, 0, 5, 0, 0, 0,
                0, 8, 0, 0, 1, 0, 9, 0, 0,
                2, 1, 0, 0, 0, 9, 7, 0, 0
            ),
        )
    }

    @Test
    fun test4() {
        testSolver(
            startValues = listOf(
                9, 0, 0, 0, 0, 7, 0, 0, 0,
                8, 0, 6, 9, 0, 2, 5, 0, 0,
                0, 0, 0, 0, 8, 0, 0, 0, 0,
                7, 0, 9, 0, 4, 0, 0, 0, 0,
                0, 6, 5, 2, 0, 8, 3, 9, 0,
                0, 0, 0, 0, 1, 0, 6, 0, 2,
                0, 0, 0, 0, 9, 0, 0, 0, 0,
                0, 0, 2, 8, 0, 3, 7, 0, 9,
                0, 0, 0, 4, 0, 0, 0, 0, 8
            ),
        )
    }

    @Test
    fun test5() {
        testSolver(
            startValues = listOf(
                7, 0, 0, 0, 0, 0, 0, 6, 0,
                0, 1, 3, 0, 6, 0, 0, 0, 0,
                0, 0, 9, 2, 4, 0, 3, 5, 1,
                5, 0, 8, 6, 0, 1, 0, 7, 0,
                9, 7, 0, 3, 0, 0, 8, 2, 6,
                4, 0, 6, 7, 8, 9, 5, 0, 3,
                0, 4, 0, 0, 0, 0, 9, 8, 0,
                0, 9, 0, 0, 0, 8, 0, 4, 0,
                0, 0, 0, 0, 0, 0, 6, 0, 5
            ),
        )
    }

    @Test
    fun test6() {
        testSolver(
            startValues = listOf(
                0, 8, 0, 0, 0, 0, 7, 2, 4,
                9, 0, 0, 0, 5, 7, 3, 0, 0,
                0, 0, 6, 0, 0, 0, 0, 0, 0,
                4, 9, 0, 0, 0, 0, 0, 0, 8,
                3, 6, 1, 5, 7, 0, 0, 0, 9,
                2, 0, 0, 0, 0, 0, 6, 0, 7,
                0, 0, 0, 0, 0, 5, 4, 6, 0,
                0, 0, 9, 4, 0, 3, 8, 7, 5,
                5, 7, 0, 8, 6, 2, 0, 1, 0
            ),
        )
    }

    @Test
    fun test7() {
        testSolver(
            startValues = listOf(
                3, 0, 0, 0, 5, 0, 7, 0, 0,
                0, 9, 0, 0, 0, 3, 8, 0, 0,
                0, 4, 7, 0, 2, 0, 5, 0, 0,
                7, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 6, 5, 8, 0, 9, 3, 2, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 7,
                0, 0, 2, 0, 8, 0, 1, 6, 0,
                0, 0, 3, 2, 0, 0, 0, 8, 0,
                0, 0, 8, 0, 9, 0, 0, 0, 5
            ),
        )
    }

    @Test
    fun test8() {
        testSolver(
            startValues = listOf(
                0, 0, 0, 0, 9, 0, 0, 7, 8,
                0, 0, 0, 0, 5, 2, 3, 4, 0,
                0, 0, 0, 3, 0, 0, 5, 9, 0,
                6, 0, 0, 0, 0, 0, 0, 8, 9,
                0, 0, 0, 5, 0, 6, 0, 0, 0,
                1, 8, 0, 0, 0, 0, 0, 0, 7,
                0, 6, 9, 0, 0, 5, 0, 0, 0,
                0, 2, 8, 7, 6, 0, 0, 0, 0,
                7, 5, 0, 0, 2, 0, 0, 0, 0
            ),
        )
    }

    // @Test
    // fun test9() {
    //     testSolver(
    //         startValues = listOf(
    //
    //         ),
    //     )
    // }
    //
    // @Test
    // fun test10() {
    //     testSolver(
    //         startValues = listOf(
    //
    //         ),
    //     )
    // }

    private fun testSolver(startValues: List<Int>) {
        val resultValues = interactor.solve(numbers = startValues)

        if (resultValues.any { it !is Cell.Number }) {
            println()
            println("TEST FAILED")
            println()
            println("---startValues:")
            printSudoku(startValues)
            println()
            val resultValuesToPrint = resultValues.map { (it as? Cell.Number)?.number ?: 0 }
            println("---resultValues:")
            printSudoku(resultValuesToPrint)
            assert(false)
        }

        val gameField: Map<Int, Cell> = buildMap {
            resultValues.forEach { cell ->
                put(cell.index, cell)
            }
        }


        resultValues.forEach { cell ->
            val cellNumber = (cell as Cell.Number).number

            val numbersInRowValid = cell.getNumbersInRow(gameField = gameField).count { it == cellNumber } == 1
            val numbersInColumnValid = cell.getNumbersInColumn(gameField = gameField).count { it == cellNumber } == 1
            val numbersInChunkValid = cell.getNumbersInChunk(gameField = gameField).count { it == cellNumber } == 1

            if (!numbersInRowValid || !numbersInColumnValid || !numbersInChunkValid) {
                val resultValuesToPrint = resultValues.map { (it as Cell.Number).number }
                println()
                println("TEST FAILED")
                println()
                println("---startValues:")
                printSudoku(startValues)
                println()
                println("---resultValues:")
                printSudoku(resultValuesToPrint)
                println()
                assert(false)
            }
        }
        assert(true)
    }

    private fun printSudoku(sudoku: List<Int>) {
        sudoku.chunked(size = 9).forEach {
            println(it.map { it.toString() }.map { if (it == "0") "_" else it })
        }
    }
}
