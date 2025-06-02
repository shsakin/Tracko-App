package com.dubd.permissionsscore

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.*
import com.dubd.permissionsscore.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog

@Composable
fun HomeScreen(appViewModel: AppViewModel) {
    val context = LocalContext.current
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val biometricManager = androidx.biometric.BiometricManager.from(context)
    var thirdPartyAppCount by remember { mutableStateOf<Int?>(null) }

    // Use the ViewModel to get app data
    val apps by appViewModel.installedApps.collectAsStateWithLifecycle(initialValue = emptyList())
    var pointsData by remember { mutableStateOf<PointsData?>(null) }

    // Calculate points when apps are loaded
    LaunchedEffect(apps) {
        if (apps.isNotEmpty()) {
            pointsData = calculatePoints(context, apps, appViewModel)
        }
        thirdPartyAppCount = appViewModel.getThirdPartyAppsCount()
    }

    var showThirdPartyAppsDialog by remember { mutableStateOf(false) }
    var showSystemFeatureDialog by remember { mutableStateOf(false) }
    var systemFeatureDialogTitle by remember { mutableStateOf("") }
    var systemFeatureDialogIcon by remember { mutableStateOf(Icons.Filled.Lock) }

    val scrollState = rememberScrollState()

    val screenAnimation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        screenAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = EaseOutQuart)
        )
    }

    var showCreditsScreen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF3949AB), Color(0xFF5C6BC0))
                )
            )
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
                .graphicsLayer {
                    alpha = screenAnimation.value
                    translationY = (1f - screenAnimation.value) * 100f
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with title and credits button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Security Dashboard",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Credits icon button
                IconButton(
                    onClick = { showCreditsScreen = true },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Credits",
                        tint = Color.White
                    )
                }
            }

            ScoreCard(pointsData?.total ?: 0)

            Spacer(modifier = Modifier.height(20.dp))
//
            SectionTitle("Device Permissions")
//
            Spacer(modifier = Modifier.height(8.dp))

            val (showDialog, setShowDialog) = remember { mutableStateOf(false) }

//            assert(maxHighScore + maxModScore + maxLowScore == 100) { "Maximum scores must add up to 100" }

            SecurityScoreCard(
                title = "Security Assessment",
                description = "System security evaluation results",
                totalHighScore = pointsData?.totalHighScore ?: 0,
                maxHighScore = pointsData?.maxHighScore ?: 60,
                totalModScore = pointsData?.totalModScore ?: 0,
                maxModScore = pointsData?.maxModScore ?: 30,
                totalLowScore = pointsData?.totalLowScore ?: 0,
                maxLowScore = pointsData?.maxLowScore ?: 10,
                gradientColors = listOf(Color(0xFF1E88E5), Color(0xFF0D47A1)),
                icon = Icons.Default.Security,
                onClick = { setShowDialog(true) }
            )

            if (showDialog && pointsData != null) {
                SecurityScoreDetailsDialog(
                    onDismiss = { setShowDialog(false) },
                    title = "Security Assessment",
                    totalHighScore = pointsData?.totalHighScore ?: 0,
                    maxHighScore = pointsData?.maxHighScore ?: 0,
                    totalModScore = pointsData?.totalModScore ?: 0,
                    maxModScore = pointsData?.maxModScore ?: 0,
                    totalLowScore = pointsData?.totalLowScore ?: 0,
                    maxLowScore = pointsData?.maxLowScore ?: 0,
                    highErrors = pointsData?.highPermErrs ?: mutableMapOf<String, Int>(),
                    modErrors = pointsData?.modPermErrs ?: mutableMapOf<String, Int>(),
                    lowErrors = pointsData?.lowPermErrs ?: mutableMapOf<String, Int>(),
                )
            }

//
//            SecurityInfoCard(
//                title = "Permission Warnings",
//                description = "Apps that are using serious permissions related to your device",
//                status = "Unavailable",
//                points = 40,
//                earned = pointsData.permissionsTotalScore > 0,
//                icon = Icons.Default.DeveloperMode,
//                gradientColors = listOf(Color(0xFF00C853), Color(0xFF00E676)),
//                onClick = {
//                    systemFeatureDialogTitle = "Permission Warnings"
//                    systemFeatureDialogIcon = Icons.Default.DeveloperMode
//                    showSystemFeatureDialog = true
//                }
//            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionTitle("Device Security")

            Spacer(modifier = Modifier.height(8.dp))

            SecurityInfoCard(
                title = "Pin/Password Lock",
                description = "Secure your device with a PIN or password",
                status = if (keyguardManager.isDeviceSecure) "Enabled" else "Disabled",
                points = 20,
                earned = (pointsData?.pinPoints ?: 0) > 0,
                icon = Icons.Default.Lock,
                gradientColors = listOf(Color(0xFF03DAC5), Color(0xFF018786)),
                onClick = null
            )

            SecurityInfoCard(
                title = "Biometric Scan",
                description = "Fingerprint or face recognition",
                status = checkBiometricStatus(biometricManager),
                points = 20,
                earned = (pointsData?.biometricPoints ?: 0) > 0,
                icon = Icons.Default.Fingerprint,
                gradientColors = listOf(Color(0xFFBB86FC), Color(0xFF6200EE)),
                onClick = null
            )

            // Section Title
            SectionTitle("App Security")

            Spacer(modifier = Modifier.height(8.dp))

            SecurityInfoCard(
                title = "Third Party Apps",
                description = "Non-Play Store apps can pose security risks",
                status = "${thirdPartyAppCount} Detected",
                points = 20,
                earned = (pointsData?.thirdPartyPoints ?: 0) > 0,
                icon = Icons.Default.Apps,
                gradientColors = listOf(Color(0xFFFF5722), Color(0xFFE64A19)),
                onClick = { showThirdPartyAppsDialog = true }
            )

            // Section Title
//            SectionTitle("Advanced Security")

            //            Spacer(modifier = Modifier.height(8.dp))
            //
            //            SecurityInfoCard(
            //                title = "Password Age",
            //                description = "How long since you changed your password",
            //                status = "Unavailable",
            //                points = 0,
            //                earned = pointsData.passwordAgePoints > 0,
            //                icon = Icons.Default.Update,
            //                gradientColors = listOf(Color(0xFF00C853), Color(0xFF00E676)),
            //                onClick = {
            //                    systemFeatureDialogTitle = "Password Age"
            //                    systemFeatureDialogIcon = Icons.Default.Update
            //                    showSystemFeatureDialog = true
            //                }
            //            )

//            SecurityInfoCard(
//                title = "Realtime Tracking",
//                description = "This system will monitor apps that are actively using sensitive permissions such as location, microphone, and camera.",
//                status = "Unavailable",
//                points = 0,
//                earned = false,
//                icon = Icons.Default.Security,
//                gradientColors = listOf(Color(0xFF2196F3), Color(0xFF1976D2)),
//                onClick = {
//                    systemFeatureDialogTitle = "Realtime Tracking"
//                    systemFeatureDialogIcon = Icons.Default.Security
//                    showSystemFeatureDialog = true
//                }
//            )

            Spacer(modifier = Modifier.height(24.dp))

            // Security Tips Section
            SecurityTipsSection()

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Third Party Apps Dialog
        if (showThirdPartyAppsDialog) {
            ThirdPartyAppsDialog(
                context = context,
                appViewModel = appViewModel,
                onDismiss = { showThirdPartyAppsDialog = false }
            )
        }

        // System Feature Dialog
        if (showSystemFeatureDialog) {
            SystemFeatureDialog(
                title = systemFeatureDialogTitle,
                icon = systemFeatureDialogIcon,
                onDismiss = { showSystemFeatureDialog = false }
            )
        }

        if (showCreditsScreen) {
            CreditsScreen(onBackPressed = { showCreditsScreen = false })
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Divider(
            color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Divider(
            color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SecurityTipsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        Text(
            text = "Security Tips",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SecurityTip(
            icon = Icons.Outlined.VpnKey,
            title = "Use strong passwords",
            description = "Mix letters, numbers, and symbols"
        )

        SecurityTip(
            icon = Icons.Outlined.Update,
            title = "Update regularly",
            description = "Keep your device and apps up to date"
        )

        SecurityTip(
            icon = Icons.Outlined.VerifiedUser,
            title = "Install from trusted sources",
            description = "Only download apps from the Play Store"
        )
    }
}

@Composable
fun SecurityTip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ScoreCard(score: Int) {
    // Clamp score to valid range
    val validScore = score.coerceIn(0, 100)

    // Define score categories
    val categories = listOf(
        ScoreCategory(
            range = 0..30,
            name = "Danger",
            description = "Extremely low security — immediate action needed.",
            color = Color(0xFFE53935), // Red
            icon = Icons.Default.Warning
        ),
        ScoreCategory(
            range = 31..50,
            name = "Weak",
            description = "Insufficient protection — needs work.",
            color = Color(0xFFFF9800), // Orange
            icon = Icons.Default.Error
        ),
        ScoreCategory(
            range = 51..70,
            name = "Moderate",
            description = "Basic protection — could be better.",
            color = Color(0xFFFFC107), // Amber
            icon = Icons.Default.Info
        ),
        ScoreCategory(
            range = 71..90,
            name = "Safe",
            description = "Generally protected — small issues exist.",
            color = Color(0xFF4CAF50), // Green
            icon = Icons.Default.CheckCircle
        ),
        ScoreCategory(
            range = 91..100,
            name = "Protected",
            description = "Excellent security — well protected.",
            color = Color(0xFF2196F3), // Blue
            icon = Icons.Default.Security
        )
    )

    // Find current category
    val currentCategory = categories.first { validScore in it.range }

    // Determine current step (0-based index)
    val currentStep = categories.indexOfFirst { validScore in it.range }

    // Animation values
    val animatedScore = remember { Animatable(0f) }
    val scale = remember { Animatable(0.9f) }

    LaunchedEffect(validScore) {
        // Animate score count
        animatedScore.animateTo(
            targetValue = validScore.toFloat(),
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )

        // Scale card for attention
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    // Infinite animations
    val infiniteTransition = rememberInfiniteTransition(label = "card_effects")

    // Glow animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_effect"
    )

    // Subtle pulse animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_effect"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .padding(12.dp)
            .graphicsLayer {
                this.scaleX = scale.value
                this.scaleY = scale.value
            }
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = currentCategory.color.copy(alpha = 0.5f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            currentCategory.color.copy(alpha = 0.95f),
                            currentCategory.color.copy(alpha = 0.7f),
                            currentCategory.color.darken(0.2f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Background decorative elements
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background patterns
                for (i in 0 until 5) {
                    val radius = size.minDimension * (0.2f + i * 0.15f)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.03f + (i * 0.01f)),
                        radius = radius,
                        center = Offset(size.width * 0.2f, size.height * 0.3f)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Security Score",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.25f),
                                offset = Offset(1f, 1f),
                                blurRadius = 2f
                            )
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Animated Score Display
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.7f),
                                    Color.White.copy(alpha = 0.2f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Score text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${animatedScore.value.toInt()}",
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.25f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )
                        Text(
                            text = "/ 100",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        )
                    }

                    // Outer glow
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .graphicsLayer {
                                alpha = glowAlpha * 0.3f
                            }
                            .border(
                                width = 4.dp,
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.7f),
                                        Color.White.copy(alpha = 0.0f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Status Category
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.Center,
//                    modifier = Modifier.padding(horizontal = 8.dp)
//                ) {
//                    // Status icon with subtle animation
//                    Icon(
//                        imageVector = currentCategory.icon,
//                        contentDescription = null,
//                        tint = Color.White,
//                        modifier = Modifier
//                            .size(28.dp)
//                            .graphicsLayer {
//                                scaleX = pulseScale
//                                scaleY = pulseScale
//                            }
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = currentCategory.name,
//                        color = Color.White,
//                        fontSize = 22.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }

//                // Description
//                Text(
//                    text = currentCategory.description,
//                    color = Color.White.copy(alpha = 0.9f),
//                    fontSize = 16.sp,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.padding(horizontal = 16.dp)
//                )

                Spacer(modifier = Modifier.height(20.dp))
                StepProgressIndicator(
                    categories = categories,
                    currentIndex = currentStep,
                    score = validScore
                )
            }
        }
    }
}


@Composable
fun StepProgressIndicator(
    categories: List<ScoreCategory>,
    currentIndex: Int,
    score: Int
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedProgress.animateTo(
            targetValue = score / 100f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Draw step indicators with connecting lines
        categories.forEachIndexed { index, category ->
            val isActive = index <= currentIndex
            val isCurrentCategory = index == currentIndex

            // Calculate if this step should have partial progress
            val stepWidth = 1f / (categories.size - 1)
            val stepStart = index * stepWidth
            val stepEnd = (index + 1) * stepWidth
            val hasPartialProgress =
                animatedProgress.value > stepStart && animatedProgress.value < stepEnd && index < categories.size - 1

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (index < categories.size) {
                    val lineProgress = if (hasPartialProgress) {
                        (animatedProgress.value - stepStart) / stepWidth
                    } else if (isActive) {
                        1f
                    } else {
                        0f
                    }

                    Box(modifier = Modifier.padding(bottom = 20.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(Color.LightGray.copy(alpha = 0.5f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(lineProgress)
                                    .fillMaxHeight()
                                    .background(category.color)
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(if (isCurrentCategory) 40.dp else 36.dp)
                            .graphicsLayer {
                                scaleX = if (isCurrentCategory) 1.1f else 1f
                                scaleY = if (isCurrentCategory) 1.1f else 1f
                            }
                            .background(
                                color = if (isActive) category.color else Color.LightGray.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .border(
                                width = if (isCurrentCategory) 2.dp else 0.dp,
                                color = if (isActive) category.color.copy(alpha = 0.5f) else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.name,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )            // Step indicator

                    }
                    Text(
                        text = "${category.name}",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class ScoreCategory(
    val range: IntRange,
    val name: String,
    val description: String,
    val color: Color,
    val icon: ImageVector
)

private fun Color.darken(factor: Float): Color {
    return Color(
        red = this.red * (1 - factor),
        green = this.green * (1 - factor),
        blue = this.blue * (1 - factor),
        alpha = this.alpha
    )
}

@Composable
fun SecurityInfoCard(
    title: String,
    description: String,
    status: String,
    points: Int,
    earned: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    onClick: (() -> Unit)? = null
) {
    val cardElevation = animateDpAsState(
        targetValue = if (earned) 8.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card elevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = cardElevation.value,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Header with gradient
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(gradientColors))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon with circular background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }

                // Points indicator
                if (points > 0) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (earned) Color(0xFF00E676) else Color.Gray,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+$points",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Status row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status:",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = status,
                        color = if (earned) Color(0xFF00C853) else Color(0xFFFF5722),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Icon(
                        imageVector = if (earned) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (earned) Color(0xFF00C853) else Color(0xFFFF5722),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // "More info" indicator for clickable cards
            if (onClick != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap for more details",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SystemFeatureDialog(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onDismiss: () -> Unit
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url("https://assets3.lottiefiles.com/packages/lf20_jhlaooji.json")
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF6200EE),
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lottie animation
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(bottom = 16.dp)
                ) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress }
                    )
                }

                Text(
                    "This feature is only available to system apps with special permissions.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "System apps have privileged access to device information that regular apps cannot access for security and privacy reasons.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                )
            ) {
                Text("Understood")
            }
        }
    )
}

@Composable
fun ThirdPartyAppsDialog(context: Context, appViewModel: AppViewModel, onDismiss: () -> Unit) {
    val packageManager = context.packageManager

    // Use ViewModel to get third-party apps
    var thirdPartyApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load third-party apps using the ViewModel
    LaunchedEffect(Unit) {
        isLoading = true
        thirdPartyApps = appViewModel.getThirdPartyApps()
//            .filter { app ->
//                app.packageName != currentAppPackageName &&
//                        !app.packageName.startsWith("com.android.") &&
//                        !app.packageName.startsWith("com.google.") &&
//                        !isSystemApp(app.packageName, packageManager)
//            }
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = Color(0xFFFF5722),
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    "Third Party Apps",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    "Apps not installed from the Google Play Store may pose security risks.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF6200EE),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Loading apps...",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else if (thirdPartyApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00C853),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No third party apps detected",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(thirdPartyApps) { app ->
                            val grantedSpecificPermissions = app.permissions.filter { perm ->
                                DataManager.getAllAndroidPermissions().contains(perm) &&
                                        packageManager.checkPermission(
                                            perm,
                                            app.packageName
                                        ) == PackageManager.PERMISSION_GRANTED
                            }.size
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF5F5F5)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AndroidView(
                                                    factory = { ctx ->
                                                        android.widget.ImageView(ctx).apply {
                                                            try {
                                                                setImageDrawable(
                                                                    packageManager.getApplicationIcon(
                                                                        app.packageName
                                                                    )
                                                                )
                                                            } catch (e: Exception) {
                                                                setImageResource(android.R.drawable.sym_def_app_icon)
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(40.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(16.dp))

                                            Text(
                                                text = app.name,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(5.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Block,
                                                contentDescription = null,
                                                tint = Color(0xFFFF5722),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Not from Play Store",
                                                fontSize = 12.sp,
                                                color = Color(0xFFFF5722)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${grantedSpecificPermissions}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (grantedSpecificPermissions > 5) Color(
                                                    0xFFFF5722
                                                ) else Color(
                                                    0xFF00C853
                                                )
                                            )
                                            Text(
                                                text = " permissions",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5722)
                )
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SecurityScoreCard(
    title: String,
    description: String,
    totalHighScore: Int,
    maxHighScore: Int,
    totalModScore: Int,
    maxModScore: Int,
    totalLowScore: Int,
    maxLowScore: Int,
    gradientColors: List<Color>,
    icon: ImageVector,
    onClick: (() -> Unit)? = null
) {
    // Calculate total score percentage
    val totalScore = totalHighScore + totalModScore + totalLowScore
    val maxTotalScore = maxHighScore + maxModScore + maxLowScore
    val scorePercentage = if (maxTotalScore > 0) (totalScore * 100) / maxTotalScore else 0

    // Calculate final score out of 40
    val finalScore = if (maxTotalScore > 0) (totalScore.toFloat() / maxTotalScore) * 40 else 0f
    val finalScoreRounded = finalScore.toInt()

    // Determine status based on score percentage
    val status = when {
        scorePercentage >= 80 -> "Excellent"
        scorePercentage >= 60 -> "Good"
        scorePercentage >= 40 -> "Average"
        else -> "Needs Improvement"
    }

    // Animation for card elevation based on score
    val cardElevation = animateDpAsState(
        targetValue = when {
            scorePercentage >= 70 -> 8.dp
            scorePercentage >= 40 -> 6.dp
            else -> 4.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card elevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = cardElevation.value,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Header with gradient
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(gradientColors))
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
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }

            // Final Score Display - Main focus
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp)
                            .border(
                                width = 4.dp,
                                color = when {
                                    scorePercentage >= 80 -> Color(0xFF00E676)
                                    scorePercentage >= 60 -> Color(0xFF64DD17)
                                    scorePercentage >= 40 -> Color(0xFFFFD600)
                                    else -> Color(0xFFFF5722)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$finalScoreRounded",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    scorePercentage >= 80 -> Color(0xFF00C853)
                                    scorePercentage >= 60 -> Color(0xFF64DD17)
                                    scorePercentage >= 40 -> Color(0xFFFFD600)
                                    else -> Color(0xFFFF5722)
                                }
                            )
                            Text(
                                text = "out of 40",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = status,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            scorePercentage >= 80 -> Color(0xFF00C853)
                            scorePercentage >= 60 -> Color(0xFF64DD17)
                            scorePercentage >= 40 -> Color(0xFFFFD600)
                            else -> Color(0xFFFF5722)
                        }
                    )

                    Text(
                        text = "Raw Score: $totalScore/$maxTotalScore",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // "More info" indicator for clickable cards
            if (onClick != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap for detailed breakdown",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(
    label: String,
    score: Int,
    maxScore: Int,
    color: Color
) {
    val progress = if (maxScore > 0) score.toFloat() / maxScore else 0f

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "$score/$maxScore",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun SecurityScoreDetailsDialog(
    onDismiss: () -> Unit,
    title: String,
    totalHighScore: Int,
    maxHighScore: Int,
    totalModScore: Int,
    maxModScore: Int,
    totalLowScore: Int,
    maxLowScore: Int,
    highErrors: MutableMap<String, Int>,
    modErrors: MutableMap<String, Int>,
    lowErrors: MutableMap<String, Int>
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Text(
                    text = "$title Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Calculate scores for display
                val totalScore = totalHighScore + totalModScore + totalLowScore
                val maxTotalScore = maxHighScore + maxModScore + maxLowScore
                val scorePercentage =
                    if (maxTotalScore > 0) (totalScore * 100) / maxTotalScore else 0
                val finalScore =
                    if (maxTotalScore > 0) (totalScore.toFloat() / maxTotalScore) * 40 else 0f
                val finalScoreRounded = finalScore.toInt()

                // Score summary section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Raw score display
                            Column {
                                Text(
                                    text = "Raw Score",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )

                                Text(
                                    text = "$totalScore/$maxTotalScore",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = when {
                                        scorePercentage >= 80 -> Color(0xFF00C853)
                                        scorePercentage >= 60 -> Color(0xFF64DD17)
                                        scorePercentage >= 40 -> Color(0xFFFFD600)
                                        else -> Color(0xFFFF5722)
                                    }
                                )
//
//                                Text(
//                                    text = "$scorePercentage%",
//                                    fontSize = 14.sp,
//                                    color = Color.Gray
//                                )
                            }

                            // Final score display
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Final Score",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )

                                Text(
                                    text = "$finalScoreRounded/40",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = when {
                                        scorePercentage >= 80 -> Color(0xFF00C853)
                                        scorePercentage >= 60 -> Color(0xFF64DD17)
                                        scorePercentage >= 40 -> Color(0xFFFFD600)
                                        else -> Color(0xFFFF5722)
                                    }
                                )

//                                Text(
//                                    text = "(${(finalScore / 40 * 100).toInt()}%)",
//                                    fontSize = 14.sp,
//                                    color = Color.Gray
//                                )
                            }
                        }

//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        // Score conversion explanation
//                        Text(
//                            text = "The final score is calculated as (raw score / 100) × 40, " +
//                                    "converting the total points to a scale of 40.",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = Color.Gray,
//                            textAlign = TextAlign.Center
//                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Score breakdown section
                Text(
                    text = "Score Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Score breakdown bars
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // High Priority Score
                    ScoreRow(
                        label = "High Sensitivity:",
                        score = totalHighScore,
                        maxScore = maxHighScore,
                        color = Color(0xFFE53935)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Moderate Priority Score
                    ScoreRow(
                        label = "Moderate Sensitivity:",
                        score = totalModScore,
                        maxScore = maxModScore,
                        color = Color(0xFFFFB300)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Low Priority Score
                    ScoreRow(
                        label = "Low Sensitivity:",
                        score = totalLowScore,
                        maxScore = maxLowScore,
                        color = Color(0xFF43A047)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Score categories explanation
                Text(
                    text = "Category Descriptions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // High priority description
                    ScoreDetailItem(
                        title = "High Sensitivity ($totalHighScore/$maxHighScore)",
                        errors = highErrors,
                        color = Color(0xFFE53935)
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color(0xFFEEEEEE)
                    )

                    // Moderate priority description
                    ScoreDetailItem(
                        title = "Moderate Sensitivity ($totalModScore/$maxModScore)",
                        errors = modErrors,
                        color = Color(0xFFFFB300)
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color(0xFFEEEEEE)
                    )

                    // Low priority description
                    ScoreDetailItem(
                        title = "Low Sensitivity ($totalLowScore/$maxLowScore)",
                        errors = lowErrors,
                        color = Color(0xFF43A047)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Calculation note
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFEAF6FF),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier
                                .padding(top = 2.dp, end = 12.dp)
                                .size(16.dp)
                        )

                        Column {
                            Text(
                                text = "Score Calculation",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2),
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Maximum possible raw score is 100, divided across high, moderate, and low priority categories. " +
                                        "The final score of 40 is calculated as (Raw Score / 100) × 40.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1976D2).copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScoreDetailItem(
    title: String,
    errors: MutableMap<String,Int>,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.05f))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

//            Spacer(modifier = Modifier.weight(1f))

//            Text(
//                text = "${errors.size}",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//            )
        }

        if (errors.isEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "No errors",
                    tint = Color.Green,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "No issues detected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        } else {
            FlowRow(
                maxItemsInEachRow = 3,
                modifier = Modifier.fillMaxWidth()
            ) {
                errors.keys.forEach { key->
                    ErrorChip(
                        error = "${permissionMap[key]}(${errors[key]} Apps)",
                        color = color,
                        onClick = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorChip(
    error: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier
            .padding(end = 8.dp, bottom = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = "Warning",
                tint = color,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private val permissionMap = mapOf(
    "android.permission.RECORD_AUDIO" to "Microphone",
    "android.permission.READ_PHONE_STATE" to "Phone",
    "android.permission.WRITE_EXTERNAL_STORAGE" to "Storage",
    "android.permission.READ_CONTACTS" to "Contact",
    "android.permission.CAMERA" to "Camera",
    "android.permission.READ_CALL_LOG" to "Call Logs",
    "android.permission.ACCESS_FINE_LOCATION" to "Location",
    "android.permission.BLUETOOTH" to "Nearby Devices",
    "android.permission.READ_CALENDAR" to "Calendar"
)

data class PointsData(
    val total: Int,
    val pinPoints: Int,
    val biometricPoints: Int,
    val thirdPartyPoints: Int,
    val permissionsTotalScore: Int,
    val totalHighScore: Int,
    val maxHighScore: Int,
    val totalModScore: Int,
    val maxModScore: Int,
    val totalLowScore: Int,
    val maxLowScore: Int,
    val highPermErrs: MutableMap<String, Int>,
    val modPermErrs: MutableMap<String, Int>,
    val lowPermErrs: MutableMap<String, Int>,
)

private fun calculatePoints(
    context: Context,
    installedApps: List<AppInfo>,
    appViewModel: AppViewModel
): PointsData {

    val highPermissions = PermissionsPreferences.getHighPermissions(context)
    val moderatePermissions = PermissionsPreferences.getModeratePermissions(context)
    val lowPermissions = PermissionsPreferences.getLowPermissions(context)

    var highPermissionsScore = 60
    var moderatePermissionsScore = 30
    var lowPermissionsScore = 10

    val isHighEmpty = highPermissions.isEmpty()
    val isModerateEmpty = moderatePermissions.isEmpty()
    val isLowEmpty = lowPermissions.isEmpty()

    if (isHighEmpty && isModerateEmpty) {
        // Only low is non-empty
        lowPermissionsScore += (highPermissionsScore + moderatePermissionsScore)
        highPermissionsScore = 0
        moderatePermissionsScore = 0
    } else if (isHighEmpty && isLowEmpty) {
        // Only moderate is non-empty
        moderatePermissionsScore += (highPermissionsScore + lowPermissionsScore)
        highPermissionsScore = 0
        lowPermissionsScore = 0
    } else if (isModerateEmpty && isLowEmpty) {
        // Only high is non-empty
        highPermissionsScore += (moderatePermissionsScore + lowPermissionsScore)
        moderatePermissionsScore = 0
        lowPermissionsScore = 0
    } else if (isHighEmpty) {
        // High is empty, split its score between moderate and low
        val split = highPermissionsScore / 2
        moderatePermissionsScore += split
        lowPermissionsScore += (highPermissionsScore - split) // Handle odd numbers
        highPermissionsScore = 0
    } else if (isModerateEmpty) {
        // Moderate is empty, split its score between high and low
        val split = moderatePermissionsScore / 2
        highPermissionsScore += split
        lowPermissionsScore += (moderatePermissionsScore - split)
        moderatePermissionsScore = 0
    } else if (isLowEmpty) {
        // Low is empty, split its score between high and moderate
        val split = lowPermissionsScore / 2
        highPermissionsScore += split
        moderatePermissionsScore += (lowPermissionsScore - split)
        lowPermissionsScore = 0
    }

    val permissionsToCheck = listOf(
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.READ_PHONE_STATE",
        "android.permission.READ_CONTACTS",
        "android.permission.READ_CALL_LOG",
        "android.permission.BLUETOOTH",
        "android.permission.READ_CALENDAR"
    )

    val permissionsMap = permissionsToCheck.associateWith { false }.toMutableMap()
    val permissionsMapCount = permissionsToCheck.associateWith { 0 }.toMutableMap()

    val packageManager = context.packageManager

    val highPermissionsPerScore =
        if (highPermissions.isEmpty()) 0 else highPermissionsScore / highPermissions.size
    val lowPermissionsPerScore =
        if (lowPermissions.isEmpty()) 0 else lowPermissionsScore / lowPermissions.size
    val moderatePermissionsPerScore =
        if (moderatePermissions.isEmpty()) 0 else moderatePermissionsScore / moderatePermissions.size

    for (app in installedApps) {
        val packageName = app.packageName

        try {
            val packageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS
            )

            val requestedPermissions = packageInfo.requestedPermissions
            val requestedPermissionsFlags = packageInfo.requestedPermissionsFlags

            if (requestedPermissions != null && requestedPermissionsFlags != null) {
                for (i in requestedPermissions.indices) {
                    val permission = requestedPermissions[i]
                    if (permission in permissionsMap.keys) {
                        val isGranted =
                            (requestedPermissionsFlags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
                        if (isGranted) {
                            permissionsMap[permission] = true
                            if (permissionsMapCount[permission] == null) {
                                permissionsMapCount[permission] = 0
                            }
                            permissionsMapCount[permission] = permissionsMapCount[permission]!! + 1
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var totalHighScore = 0
    var totalModScore = 0
    var totalLowScore = 0

    var highPermErrs = mutableMapOf<String,Int>()
    var modPermErrs = mutableMapOf<String,Int>()
    var lowPermErrs = mutableMapOf<String,Int>()

    for (perm in highPermissions) {
        if (permissionsMap[perm] == false) {
            totalHighScore += highPermissionsPerScore
        } else {
            highPermErrs[perm] = permissionsMapCount[perm]!!
        }
    }
    for (perm in moderatePermissions) {
        if (permissionsMap[perm] == false) {
            totalModScore += moderatePermissionsPerScore
        } else {
            modPermErrs[perm] = permissionsMapCount[perm]!!
        }
    }
    for (perm in lowPermissions) {
        if (permissionsMap[perm] == false) {
            totalLowScore += lowPermissionsPerScore
        } else {
            lowPermErrs[perm] = permissionsMapCount[perm]!!
        }
    }

    val totalScore = totalHighScore + totalModScore + totalLowScore
    val convertedTotalScore = (totalScore.toDouble() / 100) * 40

    var pinPoints = 0
    var biometricPoints = 0
    var thirdPartyPoints = 0

    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val biometricManager = androidx.biometric.BiometricManager.from(context)

    if (keyguardManager.isDeviceSecure) pinPoints += 20

    if (biometricManager.canAuthenticate(
            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) ==
        androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
    ) {
        biometricPoints += 20
    }

    if (checkThirdPartyApps(context, packageManager) == 0) thirdPartyPoints += 20

    val total = pinPoints + biometricPoints + thirdPartyPoints + convertedTotalScore.toInt()

    val points = mapOf(
        "totalScore" to total,
        "pinPassword" to pinPoints,
        "biometric" to biometricPoints,
        "thirdPartyPoints" to thirdPartyPoints,
        "sensitivityConvertedScore" to convertedTotalScore.toInt(),
        "sensitivityRawScore" to totalScore.toInt(),
    )

    UploadHelper.maybeUploadPoints(context, points, appViewModel)

    return PointsData(
        total,
        pinPoints,
        biometricPoints,
        thirdPartyPoints,
        convertedTotalScore.toInt(),
        totalHighScore,
        highPermissionsScore,
        totalModScore,
        moderatePermissionsScore,
        totalLowScore,
        lowPermissionsScore,
        highPermErrs,
        modPermErrs,
        lowPermErrs
    )
}

private fun checkBiometricStatus(biometricManager: androidx.biometric.BiometricManager): String {
    return when (biometricManager.canAuthenticate(
        androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
    )) {
        androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> "Enabled"
        androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Not Available"
        androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Hardware Unavailable"
        androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Not Enrolled"
        else -> "Disabled"
    }
}

private val AUTHORIZED_APPS = listOf(
    "Weather",
    "Card Package",
    "Notes",
    "Recorder",
    "com.oneplus.gamespace.black.overlay",
    "Community",
    "OnePlus Widget",
    "com.oneplus.filemanager.white.overlay",
    "com.oneplus.filemanager.black.overlay",
    "OnePlus Store",
    "Wireless Earphones",
    "com.oneplus.gamespace.white.overlay"
)

private fun checkThirdPartyApps(context: Context, pm: PackageManager): Int {
    val currentAppPackageName = context.packageName
    val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

    return packages.count { pkg ->
        val isNonSystem = (pkg.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        val isNotCurrentApp = pkg.packageName != currentAppPackageName
        val isNotGoogleApp = !pkg.packageName.startsWith("com.google.")
        val appName = pm.getApplicationLabel(pkg).toString()
        val isAuthorizedApp =
            AUTHORIZED_APPS.any { item -> item.trim().lowercase() == appName.lowercase() }

        val installer = try {
            pm.getInstallerPackageName(pkg.packageName)
        } catch (e: Exception) {
            null
        }
        isNonSystem && isNotCurrentApp && isNotGoogleApp && installer != "com.android.vending" && !isAuthorizedApp
    }
}

@Composable
fun CreditsScreen(onBackPressed: () -> Unit) {
    val screenAnimation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        screenAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = EaseOutQuart)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF3949AB), Color(0xFF5C6BC0))
                )
            )
    ) {
        // Background decorative elements
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

        // Top app bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .graphicsLayer {
                    alpha = screenAnimation.value
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Credits",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                .verticalScroll(rememberScrollState())
                .graphicsLayer {
                    alpha = screenAnimation.value
                    translationY = (1f - screenAnimation.value) * 100f
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App logo/icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF6200EE), Color(0xFF9C27B0))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App description
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "This application is a part of a Thesis Project Test App developed as part of academic research.",
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Team section
            Text(
                text = "Development Team",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Main lead
            TeamMemberCard(
                name = "Nafiz Ahmed Rhythm",
                role = "Main Lead",
                icon = Icons.Default.Star,
                gradientColors = listOf(Color(0xFFBB86FC), Color(0xFF6200EE))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Team members
            TeamMemberCard(
                name = "MD Rezaul Karim",
                role = "Team Member",
                icon = Icons.Default.Person,
                gradientColors = listOf(Color(0xFF03DAC5), Color(0xFF018786))
            )

            TeamMemberCard(
                name = "MD. Shamsul Haque Sakin",
                role = "Team Member",
                icon = Icons.Default.Person,
                gradientColors = listOf(Color(0xFF03DAC5), Color(0xFF018786))
            )

            TeamMemberCard(
                name = "Fatema Tuz Zohora Panna",
                role = "Team Member",
                icon = Icons.Default.Person,
                gradientColors = listOf(Color(0xFF03DAC5), Color(0xFF018786))
            )

            TeamMemberCard(
                name = "Asif Imtiaz Chowdhury",
                role = "Team Member",
                icon = Icons.Default.Person,
                gradientColors = listOf(Color(0xFF03DAC5), Color(0xFF018786))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Thank you note
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE).copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFF80AB),
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "We thank everyone who supported and contributed to the development of this project.",
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TeamMemberCard(
    name: String,
    role: String,
    icon: ImageVector,
    gradientColors: List<Color>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(gradientColors)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with circular background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = role,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}