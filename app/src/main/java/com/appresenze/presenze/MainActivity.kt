package com.appresenze.presenze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appresenze.presenze.data.AttendanceViewModel
import com.appresenze.presenze.data.EventType
import com.appresenze.presenze.data.Tab
import com.appresenze.presenze.ui.components.BottomNavBar
import com.appresenze.presenze.ui.components.ConfirmSheet
import com.appresenze.presenze.ui.screens.HomeScreen
import com.appresenze.presenze.ui.screens.NotificheScreen
import com.appresenze.presenze.ui.screens.RiepilogoScreen
import com.appresenze.presenze.ui.screens.StoricoScreen
import com.appresenze.presenze.ui.theme.ClockInTheme
import com.appresenze.presenze.ui.theme.ScreenBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClockInTheme {
                PresenzeApp()
            }
        }
    }
}

@Composable
fun PresenzeApp(vm: AttendanceViewModel = viewModel()) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        AnimatedContent(
            targetState = vm.activeTab,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                val direction = if (forward) {
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Right
                }
                (slideIntoContainer(direction, animationSpec = tween(360)) + fadeIn(tween(360)))
                    .togetherWith(slideOutOfContainer(direction, animationSpec = tween(360)) + fadeOut(tween(220)))
            },
            label = "tabContent",
        ) { tab ->
            when (tab) {
                Tab.HOME -> HomeScreen(vm, Modifier.fillMaxSize())
                Tab.STORICO -> StoricoScreen(vm, Modifier.fillMaxSize())
                Tab.RIEPILOGO -> RiepilogoScreen(vm, Modifier.fillMaxSize())
                Tab.NOTIFICHE -> NotificheScreen(vm, Modifier.fillMaxSize())
            }
        }

        BottomNavBar(
            activeTab = vm.activeTab,
            showBadge = vm.notifications.isNotEmpty(),
            onTabSelected = { vm.setTab(it) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(999.dp), ambientColor = Color.Black.copy(alpha = 0.14f)),
        )

        vm.pendingType?.let { pending ->
            ConfirmSheet(
                actionLabel = if (pending == EventType.IN) "entrata" else "uscita",
                onConfirm = vm::confirmYes,
                onCancel = vm::confirmNo,
            )
        }
    }
}
