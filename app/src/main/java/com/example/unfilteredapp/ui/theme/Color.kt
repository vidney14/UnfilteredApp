package com.example.unfilteredapp.ui.theme

import androidx.compose.ui.graphics.Color

// Premium Clean Palette
val PrimaryIndigo = Color(0xFF6366F1)
val SecondaryPink = Color(0xFFEC4899)
val AccentTeal = Color(0xFF14B8A6)

// Dark Theme Colors
val BackgroundDark = Color(0xFF0F172A)
val SurfaceDark = Color(0xFF1E293B)
val OnBackgroundDark = Color(0xFFF8FAFC)
val OnSurfaceDark = Color(0xFFF1F5F9)

// Light Theme Colors
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val OnBackgroundLight = Color(0xFF0F172A)
val OnSurfaceLight = Color(0xFF1E293B)

// Status Colors
val ErrorRed = Color(0xFFEF4444)
val SuccessGreen = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)

// Mood Category Colors — single source of truth used across all screens
val MoodHighEnergyPleasantStart = Color(0xFFFBDA63)
val MoodHighEnergyPleasantEnd   = Color(0xFFFBB140)
val MoodLowEnergyPleasantStart  = Color(0xFF62F95D)
val MoodLowEnergyPleasantEnd    = Color(0xFF058C00)
val MoodLowEnergyUnpleasantStart  = Color(0xFF5D99F9)
val MoodLowEnergyUnpleasantEnd    = Color(0xFF2B7CFF)
val MoodHighEnergyUnpleasantStart = Color(0xFFF83700)
val MoodHighEnergyUnpleasantEnd   = Color(0xFFBF2A00)

// Shorthand accent per quadrant (used for bars, chips, icons)
val MoodHighEnergyPleasantAccent  = MoodHighEnergyPleasantStart
val MoodLowEnergyPleasantAccent   = MoodLowEnergyPleasantStart
val MoodLowEnergyUnpleasantAccent = MoodLowEnergyUnpleasantStart
val MoodHighEnergyUnpleasantAccent = MoodHighEnergyUnpleasantStart

fun moodAccentColor(modeType: String): Color = when (modeType) {
    "high_energy_pleasant"   -> MoodHighEnergyPleasantAccent
    "low_energy_pleasant"    -> MoodLowEnergyPleasantAccent
    "low_energy_unpleasant"  -> MoodLowEnergyUnpleasantAccent
    else                     -> MoodHighEnergyUnpleasantAccent
}