package com.zhukovartemvl.sudokusolver.di

import com.zhukovartemvl.sudokusolver.domain.SudokuSolverInteractor
import com.zhukovartemvl.sudokusolver.preferences.SudokuPreferences
import com.zhukovartemvl.sudokusolver.tools.ImageCVScanner
import com.zhukovartemvl.sudokusolver.tools.MediaProjectionKeeper
import com.zhukovartemvl.sudokusolver.tools.PermissionsChecker
import com.zhukovartemvl.sudokusolver.tools.ScreenshotMaker
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::MediaProjectionKeeper)
    factoryOf(::SudokuPreferences)
    factoryOf(::PermissionsChecker)
    factoryOf(::ScreenshotMaker)
    factoryOf(::ImageCVScanner)
    factoryOf(::SudokuSolverInteractor)
}
