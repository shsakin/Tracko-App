package com.dubd.permissionsscore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsSensitivityScreen(navController: NavController? = null) {
    val context = LocalContext.current

    // Initialize permissions on first run
    LaunchedEffect(Unit) {
        PermissionsPreferences.initializePermissionsCategories(context)
    }

    // Load permissions from SharedPreferences
    var highPermissions by remember {
        mutableStateOf(
            PermissionsPreferences.getHighPermissions(
                context
            )
        )
    }
    var moderatePermissions by remember {
        mutableStateOf(
            PermissionsPreferences.getModeratePermissions(
                context
            )
        )
    }
    var lowPermissions by remember { mutableStateOf(PermissionsPreferences.getLowPermissions(context)) }

    // Drag-and-drop state with explicit type
    val dragAndDropState = rememberDragAndDropState<DraggablePermission>()
    val scope = rememberCoroutineScope()
    var targetCategory by remember { mutableStateOf<String?>(null) }

    // Animation states
    val screenAnimation = remember { Animatable(0f) }
    val pulseAnimation = remember { Animatable(1f) }
    val rotationAnimation = remember { Animatable(0f) }

    // Animated floating particles
    val random = Random(System.currentTimeMillis())
    val particles = remember {
        List(15) {
            mutableStateOf(
                Particle(
                    x = random.nextInt(-100, 500).toFloat(),
                    y = random.nextInt(-100, 900).toFloat(),
                    size = random.nextInt(5, 15).toFloat(),
                    speed = 0.2f + random.nextFloat() * 0.6f,
                    alpha = 0.05f + random.nextFloat() * 0.1f
                )
            )
        }
    }

    // Launch animations
    LaunchedEffect(Unit) {
        screenAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = EaseOutQuart)
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            pulseAnimation.animateTo(
                targetValue = 1.05f,
                animationSpec = tween(2000, easing = EaseInOutQuad)
            )
            pulseAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(2000, easing = EaseInOutQuad)
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            rotationAnimation.animateTo(
                targetValue = 360f,
                animationSpec = tween(30000, easing = LinearEasing)
            )
            rotationAnimation.snapTo(0f)
        }
    }

    // Animate particles
    LaunchedEffect(Unit) {
        while (true) {
            particles.forEach { particleState ->
                val particle = particleState.value
                particleState.value = particle.copy(
                    y = if (particle.y > 1000) -50f else particle.y + particle.speed
                )
            }
            kotlinx.coroutines.delay(16) // ~60fps
        }
    }

    // Enhanced background gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            DarkPurple,
            DeepPurple.copy(alpha = 0.95f),
            Color(0xFF5C6BC0).copy(alpha = 0.9f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Animated background elements with Box instead of Canvas
        Box(modifier = Modifier.fillMaxSize()) {
            // First rotating gradient circle
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = rotationAnimation.value
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(size = 600.dp)
                        .offset(x = 100.dp, y = (-200).dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF3F51B5).copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }

            // Second rotating gradient circle
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = -rotationAnimation.value / 2
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(size = 500.dp)
                        .offset(x = (-150).dp, y = 300.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF7986CB).copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }

            // Floating particles
            particles.forEach { particleState ->
                val particle = particleState.value
                Box(
                    modifier = Modifier
                        .size(particle.size.dp * pulseAnimation.value)
                        .offset(x = particle.x.dp, y = particle.y.dp)
                        .background(
                            color = Color.White.copy(alpha = particle.alpha),
                            shape = CircleShape
                        )
                )
            }
        }

        // Decorative elements with animation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = screenAnimation.value
                    scaleX = pulseAnimation.value
                    scaleY = pulseAnimation.value
                }
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .offset(x = 150.dp, y = (-50).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.02f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = (-50).dp, y = 650.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.02f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        DragAndDropContainer(
            state = dragAndDropState,
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Security,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(end = 8.dp)
                                )
                                Text(
                                    "Permissions Sensitivity",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { navController?.popBackStack() },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    Icons.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
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
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Category,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Permission Categories",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DragIndicator,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Drag and drop permissions to reclassify their sensitivity level",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    item {
                        // High Permissions Category
                        PermissionCategoryCard(
                            category = PermissionCategory.HIGH,
                            permissions = highPermissions,
                            dragAndDropState = dragAndDropState,
                            isDraggingOver = targetCategory == "high",
                            onDrop = { permission ->
                                if (permission != null && permission.category != "high") {
                                    scope.launch {
                                        PermissionsPreferences.movePermission(
                                            context,
                                            permission.permission,
                                            permission.category,
                                            "high"
                                        )
                                        highPermissions =
                                            PermissionsPreferences.getHighPermissions(context)
                                        moderatePermissions =
                                            PermissionsPreferences.getModeratePermissions(context)
                                        lowPermissions =
                                            PermissionsPreferences.getLowPermissions(context)
                                    }
                                }
                                targetCategory = null
                            },
                            onDragOver = { isOver ->
                                targetCategory = if (isOver) "high" else null
                            }
                        )
                    }

                    item {
                        // Moderate Permissions Category
                        PermissionCategoryCard(
                            category = PermissionCategory.MODERATE,
                            permissions = moderatePermissions,
                            dragAndDropState = dragAndDropState,
                            isDraggingOver = targetCategory == "moderate",
                            onDrop = { permission ->
                                if (permission != null && permission.category != "moderate") {
                                    scope.launch {
                                        PermissionsPreferences.movePermission(
                                            context,
                                            permission.permission,
                                            permission.category,
                                            "moderate"
                                        )
                                        highPermissions =
                                            PermissionsPreferences.getHighPermissions(context)
                                        moderatePermissions =
                                            PermissionsPreferences.getModeratePermissions(context)
                                        lowPermissions =
                                            PermissionsPreferences.getLowPermissions(context)
                                    }
                                }
                                targetCategory = null
                            },
                            onDragOver = { isOver ->
                                targetCategory = if (isOver) "moderate" else null
                            }
                        )
                    }

                    item {
                        // Low Permissions Category
                        PermissionCategoryCard(
                            category = PermissionCategory.LOW,
                            permissions = lowPermissions,
                            dragAndDropState = dragAndDropState,
                            isDraggingOver = targetCategory == "low",
                            onDrop = { permission ->
                                if (permission != null && permission.category != "low") {
                                    scope.launch {
                                        PermissionsPreferences.movePermission(
                                            context,
                                            permission.permission,
                                            permission.category,
                                            "low"
                                        )
                                        highPermissions =
                                            PermissionsPreferences.getHighPermissions(context)
                                        moderatePermissions =
                                            PermissionsPreferences.getModeratePermissions(context)
                                        lowPermissions =
                                            PermissionsPreferences.getLowPermissions(context)
                                    }
                                }
                                targetCategory = null
                            },
                            onDragOver = { isOver ->
                                targetCategory = if (isOver) "low" else null
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionCategoryCard(
    category: PermissionCategory,
    permissions: List<String>,
    dragAndDropState: DragAndDropState<DraggablePermission>,
    isDraggingOver: Boolean,
    onDrop: (DraggablePermission?) -> Unit,
    onDragOver: (Boolean) -> Unit
) {
    // Animation states
    val borderColor = remember { Animatable(1f) }
    val scaleAnimation = remember { Animatable(1f) }
    val glowAnimation = remember { Animatable(0f) }

    // Category-specific animations
    LaunchedEffect(isDraggingOver) {
        borderColor.animateTo(
            targetValue = if (isDraggingOver) .5f else 1f,
            animationSpec = tween(300)
        )

        scaleAnimation.animateTo(
            targetValue = if (isDraggingOver) 1.03f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        glowAnimation.animateTo(
            targetValue = if (isDraggingOver) 1f else 0f,
            animationSpec = tween(300)
        )
    }

    // Category-specific gradient
    val gradientColors = when (category) {
        PermissionCategory.HIGH -> listOf(
            ErrorRed,
            ErrorRed.copy(alpha = 0.8f),
            Color(0xFFD32F2F)
        )
        PermissionCategory.MODERATE -> listOf(
            WarningOrange,
            WarningOrange.copy(alpha = 0.8f),
            Color(0xFFF57C00)
        )
        PermissionCategory.LOW -> listOf(
            SuccessGreen,
            SuccessGreen.copy(alpha = 0.8f),
            Color(0xFF388E3C)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .graphicsLayer {
                scaleX = scaleAnimation.value
                scaleY = scaleAnimation.value
                shadowElevation = if (isDraggingOver) 8f else 4f
            }
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = if (isDraggingOver) {
                        listOf(
                            Color.White.copy(alpha = 0.8f),
                            category.color.copy(alpha = 0.8f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(
                elevation = if (isDraggingOver) 8.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = category.color.copy(alpha = glowAnimation.value * 0.5f)
            )
            .dropTarget(
                state = dragAndDropState,
                key = category.name.lowercase(),
                onDrop = { state ->
                    onDrop(state.data)
                    true
                },
                onDragEnter = { onDragOver(true) },
                onDragExit = { onDragOver(false) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Enhanced Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = gradientColors,
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        )
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Enhanced Category icon with glow effect
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                spotColor = category.color
                            )
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White,
                                        category.color.copy(alpha = 0.2f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val iconSize = 24.dp
                        when (category) {
                            PermissionCategory.HIGH -> {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(iconSize)
                                )
                            }
                            PermissionCategory.MODERATE -> {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = WarningOrange,
                                    modifier = Modifier.size(iconSize)
                                )
                            }
                            PermissionCategory.LOW -> {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(iconSize)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${category.displayName} Sensitivity",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (category) {
                                PermissionCategory.HIGH -> "Potentially risky permissions"
                                PermissionCategory.MODERATE -> "Medium risk permissions"
                                PermissionCategory.LOW -> "Low risk permissions"
                            },
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Enhanced Permissions grid with better styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                contentAlignment = if (permissions.isEmpty()) Alignment.Center else Alignment.TopStart
            ) {
                if (permissions.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NoSim,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No permissions in this category",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = "${permissions.size} permission${if (permissions.size != 1) "s" else ""}",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (permissions.size > 3) 140.dp else 70.dp)
                        ) {
                            items(permissions.size) { index ->
                                DraggableItem(
                                    state = dragAndDropState,
                                    key = permissions[index],
                                    data = DraggablePermission(
                                        permissions[index],
                                        category.name.lowercase()
                                    )
                                ) {
                                    PermissionChip(
                                        permission = permissions[index],
                                        category = category
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val permissionMap = mapOf(
    "Microphone" to "android.permission.RECORD_AUDIO",
    "Phone" to "android.permission.READ_PHONE_STATE",
    "Storage" to "android.permission.WRITE_EXTERNAL_STORAGE",
    "Contact" to "android.permission.READ_CONTACTS",
    "Camera" to "android.permission.CAMERA",
    "Call Logs" to "android.permission.READ_CALL_LOG",
    "Location" to "android.permission.ACCESS_FINE_LOCATION",
    "Nearby" to "android.permission.BLUETOOTH",
    "Calendar" to "android.permission.READ_CALENDAR"
)

@Composable
fun PermissionChip(
    permission: String,
    category: PermissionCategory
) {
    // Get friendly name and icon for the permission
    val permissionName = permissionMap.entries.find { it.value == permission }?.key ?: "Unknown"
    val permissionIcon = when {
        permission.contains("CAMERA", ignoreCase = true) -> Icons.Rounded.CameraAlt
        permission.contains("RECORD_AUDIO", ignoreCase = true) -> Icons.Rounded.Mic
        permission.contains("WRITE_EXTERNAL_STORAGE", ignoreCase = true) -> Icons.Rounded.Storage
        permission.contains("ACCESS_FINE_LOCATION", ignoreCase = true) -> Icons.Rounded.LocationOn
        permission.contains("READ_PHONE_STATE", ignoreCase = true) -> Icons.Rounded.Call
        permission.contains("READ_CONTACTS", ignoreCase = true) -> Icons.Rounded.Contacts
        permission.contains("READ_CALL_LOG", ignoreCase = true) -> Icons.Rounded.ContactPhone
        permission.contains("BLUETOOTH", ignoreCase = true) -> Icons.Rounded.Bluetooth
        permission.contains("READ_CALENDAR", ignoreCase = true) -> Icons.Rounded.CalendarToday
        else -> Icons.Rounded.Security
    }

    // Hover state
    var isHovered by remember { mutableStateOf(false) }

    // Animation properties
    val animatedElevation by animateDpAsState(
        targetValue = if (isHovered) 4.dp else 0.dp,
        animationSpec = tween(200),
        label = "elevation"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isHovered) 1.03f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )

    // Create dynamic background gradients
    val mainGradient = Brush.linearGradient(
        colors = listOf(
            category.color.copy(alpha = 0.9f),
            category.color.copy(alpha = 0.7f)
        ),
        start = Offset(0f, 0f),
        end = Offset(60f, 60f)
    )

    val overlayGradient = Brush.radialGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.4f),
            Color.Transparent
        ),
        center = Offset(20f, 15f),
        radius = 40f
    )

    val iconGradient = Brush.radialGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.95f),
            Color.White.copy(alpha = 0.85f)
        )
    )

    Box(
        modifier = Modifier
            .size(width = 65.dp, height = 55.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .shadow(
                elevation = animatedElevation,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { isHovered = !isHovered }
            .clip(RoundedCornerShape(12.dp))
            .background(mainGradient)
            .then(
                Modifier.drawBehind {
                    // Create subtle dot pattern overlay
                    val dotSize = 1.5f
                    val spacing = 10f
                    val rows = (size.height / spacing).toInt()
                    val cols = (size.width / spacing).toInt()

                    for (row in 0..rows) {
                        for (col in 0..cols) {
                            // Stagger every other row
                            val offsetX = if (row % 2 == 0) 0f else spacing / 2

                            drawCircle(
                                color = Color.White.copy(alpha = 0.2f),
                                radius = dotSize,
                                center = Offset(
                                    x = col * spacing + offsetX,
                                    y = row * spacing
                                )
                            )
                        }
                    }
                }
            )
            .background(overlayGradient)
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Icon circle with inner shadow effect
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .shadow(2.dp, CircleShape)
                    .background(iconGradient, CircleShape)
                    .border(
                        width = 0.5.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.9f),
                                Color.White.copy(alpha = 0.3f)
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = permissionIcon,
                    contentDescription = permissionName,
                    tint = category.color.copy(alpha = 0.9f),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Text label with contrast for better readability
            Text(
                text = permissionName,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = Offset(0f, 1f),
                        blurRadius = 2f
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Data class for animated particles
data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)