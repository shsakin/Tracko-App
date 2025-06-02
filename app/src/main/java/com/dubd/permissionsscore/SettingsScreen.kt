package com.dubd.permissionsscore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dubd.permissionsscore.ui.theme.*
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import kotlinx.coroutines.launch

// Data class to represent a draggable permission item
data class DraggablePermission(
    val permission: String,
    val category: String
)

// Enum for permission categories
enum class PermissionCategory(val displayName: String, val color: Color) {
    HIGH("High", ErrorRed),
    MODERATE("Moderate", WarningOrange),
    LOW("Low", SuccessGreen)
}

// Data class for settings items
data class SettingsItem(
    val title: String,
    val description: String = "",
    val icon: ImageVector,
    val iconTint: Color = Color.White,
    val gradientColors: List<Color>,
    val onClick: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController? = null) {
    val context = LocalContext.current

    // Initialize permissions on first run
    LaunchedEffect(Unit) {
        PermissionsPreferences.initializePermissionsCategories(context)
    }

    // Screen entrance animation
    val screenAnimation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        screenAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = EaseOutQuart)
        )
    }

    // Floating animation for decorative elements
    val floatingAnimation = rememberInfiniteTransition(label = "floating")
    val yOffset = floatingAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yOffset"
    )

    // Enhanced background gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            DarkPurple,
            DeepPurple.copy(alpha = 0.9f),
            DeepPurple.copy(alpha = 0.8f)
        )
    )

    // Define settings groups
    val permissionSettings = listOf(
        SettingsItem(
            title = "Category Settings",
            description = "Manage permission categories and sensitivity levels",
            icon = Icons.Default.Category,
            gradientColors = listOf(PrimaryPink, PrimaryPink.copy(alpha = 0.7f)),
            onClick = { navController?.navigate("categories") }
        ),
        SettingsItem(
            title = "Permissions Sensitivity",
            description = "Customize how permissions affect your security score",
            icon = Icons.Default.Security,
            gradientColors = listOf(WarningOrange, WarningOrange.copy(alpha = 0.7f)),
            onClick = { navController?.navigate("permissions_sensitivity") }
        )
    )

    val appSettings = listOf(
        SettingsItem(
            title = "Notification Settings",
            description = "Manage app notifications and alerts",
            icon = Icons.Default.Notifications,
            gradientColors = listOf(SuccessGreen, SuccessGreen.copy(alpha = 0.7f)),
            onClick = { /* Handle notification settings */ }
        ),
        SettingsItem(
            title = "Privacy Settings",
            description = "Control data usage and privacy options",
            icon = Icons.Default.PrivacyTip,
            gradientColors = listOf(Color(0xFF9C27B0), Color(0xFF7B1FA2)),
            onClick = { /* Handle privacy settings */ }
        )
    )

    val accountSettings = listOf(
        SettingsItem(
            title = "App Theme",
            description = "Customize app appearance",
            icon = Icons.Default.Palette,
            gradientColors = listOf(Color(0xFF2196F3), Color(0xFF1976D2)),
            onClick = { /* Handle theme settings */ }
        ),
        SettingsItem(
            title = "About",
            description = "App information and version",
            icon = Icons.Default.Info,
            gradientColors = listOf(Color(0xFF607D8B), Color(0xFF455A64)),
            onClick = { /* Handle about screen */ }
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Enhanced background decorative elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = screenAnimation.value
                }
        ) {
            // Top right circle
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = 150.dp, y = (-50).dp)
                    .graphicsLayer {
                        translationY = yOffset.value
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.White.copy(alpha = 0.05f),
                                Color.White.copy(alpha = 0.01f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Bottom left circle
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = (-50).dp, y = 650.dp)
                    .graphicsLayer {
                        translationY = -yOffset.value
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.White.copy(alpha = 0.05f),
                                Color.White.copy(alpha = 0.01f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Middle decorative element
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 50.dp, y = 300.dp)
                    .graphicsLayer {
                        translationY = yOffset.value * 0.7f
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.07f),
                                Color.White.copy(alpha = 0.03f),
                                Color.White.copy(alpha = 0.01f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Settings",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = screenAnimation.value
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Permissions Settings Section
                item {
                    SettingsSectionHeader(title = "Permissions", icon = Icons.Default.Security)
                }

                items(permissionSettings.size) { index ->
                    SettingsButton(item = permissionSettings[index])
                }

                // App Settings Section
//                item {
//                    Spacer(modifier = Modifier.height(8.dp))
//                    SettingsSectionHeader(title = "App Settings", icon = Icons.Default.Settings)
//                }
//
//                items(appSettings.size) { index ->
//                    SettingsButton(item = appSettings[index])
//                }
//
//                // Account Settings Section
//                item {
//                    Spacer(modifier = Modifier.height(8.dp))
//                    SettingsSectionHeader(title = "Preferences", icon = Icons.Default.Person)
//                }
//
//                items(accountSettings.size) { index ->
//                    SettingsButton(item = accountSettings[index])
//                }
//
//                // Bottom spacer
//                item {
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.3f))
        )
    }
}

@Composable
fun SettingsButton(item: SettingsItem) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animation for pressed state
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(
            durationMillis = 100,
            easing = EaseOutQuart
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 2.dp else 4.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true, color = Color.White),
                onClick = item.onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = item.gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon with circular background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = item.iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Text content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (item.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Arrow icon
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}