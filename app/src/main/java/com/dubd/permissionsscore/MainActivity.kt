package com.dubd.permissionsscore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dubd.permissionsscore.ui.theme.*
import com.dubd.permissionsscore.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    // Initialize the ViewModel at the activity level
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataManager.initializeData(this)
        PermissionsPreferences.initializePermissionsCategories(this)

        // Load apps when the app starts
        appViewModel.loadInstalledApps()

        setContent {
            SecurityAppTheme {
                MainScreen(appViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh app list when returning to the app
        appViewModel.loadInstalledApps()
    }
}

@Composable
fun MainScreen(appViewModel: AppViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Collect app list state
    val apps by appViewModel.installedApps.collectAsStateWithLifecycle(initialValue = emptyList())
    val isLoading by appViewModel.isLoading.collectAsStateWithLifecycle(initialValue = true)

    val screenAnimation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        screenAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = EaseOutQuart)
        )
    }

    // Navigation items with their routes and icons
    val items = listOf(
        "home" to R.drawable.ic_security,
        "apps" to R.drawable.ic_apps,
        "notifications" to R.drawable.ic_notifications,
        "settings" to R.drawable.ic_categories
    )

    // Background gradient that matches HomeScreen
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            DarkPurple,
            DeepPurple
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = screenAnimation.value
                }
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = 150.dp, y = (-50).dp)
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = (-50).dp, y = 650.dp)
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                AnimatedVisibility(
                    visible = currentDestination?.route?.startsWith("appDetail") != true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .shadow(16.dp, shape = RoundedCornerShape(24.dp)),
                        color = Color(0xFF6200EE).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Purple80.copy(alpha = 0.2f),
                                            SecondaryPink.copy(alpha = 0.2f)
                                        )
                                    )
                                )
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            ) {
                                items.forEachIndexed { index, item ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == item.first } == true
                                    val iconTint = if (selected) Color.White.copy(alpha = 1f) else Color.White.copy(alpha = 0.8f)
                                    val backgroundAlpha = if (selected) 0.5f else 0f

                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                ImageVector.vectorResource(item.second),
                                                contentDescription = item.first,
                                                tint = iconTint,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                item.first.replaceFirstChar { it.uppercase() },
                                                color = iconTint,
                                                fontSize = 10.sp
                                            )
                                        },
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(item.first) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = Color.Transparent,
                                            selectedIconColor = Purple80,
                                            selectedTextColor = Purple80,
                                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                                        ),
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp, vertical = 8.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                color = Purple80.copy(alpha = backgroundAlpha),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .padding(innerPadding)
                    .graphicsLayer {
                        alpha = screenAnimation.value
                    }
            ) {
                composable("home") {
                    HomeScreen(appViewModel = appViewModel)
                }
                composable("apps") {
                    AppsScreen(
                        navController = navController,
                        appViewModel = appViewModel,
                        apps = apps,
                        isLoading = isLoading
                    )
                }
                composable("notifications") {
                    NotificationsScreen(
                        navController = navController,
                        appViewModel = appViewModel
                    )
                }
                composable("settings") {
                    SettingsScreen(navController)
                }
                composable("categories") {
                    CategoriesScreen(appViewModel = appViewModel, allApps = apps)
                }
                composable("permissions_sensitivity") {
                    PermissionsSensitivityScreen(navController)
                }
                composable("appDetail/{packageName}") { backStackEntry ->
                    AppDetailScreen(
                        packageName = backStackEntry.arguments?.getString("packageName") ?: "",
                        navController = navController,
                        appViewModel = appViewModel
                    )
                }
            }
        }
    }
}