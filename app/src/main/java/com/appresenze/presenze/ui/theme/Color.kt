package com.appresenze.presenze.ui.theme

import androidx.compose.ui.graphics.Color

// Design tokens straight from App Presenze.dc.html
val Accent = Color(0xFF3B5BFE)          // #3b5bfe
val AccentPillBg = Color(0x1F3B5BFE)    // rgba(59,91,254,0.12)
val AccentSoftBg = Color(0xFFEEF1FF)    // #eef1ff (avatar / badge backgrounds)
val ClockedInGreen = Color(0xFF1F9D55)  // #1f9d55

val ScreenBackground = Color(0xFFFAFAFA) // #fafafa
val TextPrimary = Color(0xFF161616)      // #161616

val TextSecondary55 = Color(0x8C161616) // rgba(22,22,22,0.55)
val TextSecondary50 = Color(0x80161616) // rgba(22,22,22,0.5)
val TextSecondary45 = Color(0x73161616) // rgba(22,22,22,0.45)
val TextSecondary40 = Color(0x66161616) // rgba(22,22,22,0.4)  -- inactive nav icon/gray
val TextSecondary60 = Color(0x99161616) // rgba(22,22,22,0.6)
val CardBorder = Color(0x14161616)       // rgba(22,22,22,0.08)
val DashedBorder = Color(0x26161616)     // rgba(22,22,22,0.15)
val ScrimColor = Color(0x59161616)       // rgba(22,22,22,0.35)

val NotifBadgeRed = Color(0xFFE5484D)
val SecondaryDotGray = Color(0xFF9AA0A6)

val NavBarBg = Color(0xADFFFFFF)         // rgba(255,255,255,0.68) -- semi-transparent pill nav

// Contract-hours highlight (Riepilogo week cards): soft tints + the existing
// ClockedInGreen / NotifBadgeRed as border/accent colors.
val SuccessSoftBg = Color(0xFFE7F7ED)
val DangerSoftBg = Color(0xFFFCEBEA)

// Smart-working badge, kept visually distinct from the green/red contract-hours colors above.
val SmartWorkingAccent = Color(0xFF8B5CF6)
val SmartWorkingSoftBg = Color(0xFFF3EEFF)

// Vacation ("Ferie") badge + the orange "below target because of vacation" week highlight.
val VacationAccent = Color(0xFFF59E0B)
val VacationSoftBg = Color(0xFFFFF4E0)
