package com.dubd.permissionsscore

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import com.dubd.permissionsscore.viewmodel.AppViewModel
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun AppsScreen(
    navController: NavController,
    appViewModel: AppViewModel,
    apps: List<AppInfo>,
    isLoading: Boolean
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF3949AB), Color(0xFF5C6BC0))
                )
            ),
        containerColor = Color.White,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF5C6BC0))
                    .padding(top = 8.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "All Apps",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Apps", color = Color.White) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = Color.White
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color.Transparent, shape = RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedLeadingIconColor = Color.White,
                        unfocusedLeadingIconColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A237E), Color(0xFF3949AB), Color(0xFF5C6BC0))
                    )
                )
                .padding(paddingValues),
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF5C6BC0),
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = apps.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.packageName.contains(searchQuery, ignoreCase = true)
                        },
                        key = { it.packageName } // Adding key for better performance
                    ) { app ->
                        AppItem(app, context.packageManager, context) {
                            navController.navigate("appDetail/${app.packageName}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppItem(app: AppInfo, pm: PackageManager, context: Context, onClick: () -> Unit) {
    // Get app icon from cache if available
    val appViewModel = (context as? ComponentActivity)?.viewModels<AppViewModel>()?.value
    val icon = remember {
        appViewModel?.getAppIcon(app.packageName)?.asImageBitmap()
            ?: pm.getApplicationIcon(app.packageName).toBitmap().asImageBitmap()
    }
    val categories = DataManager.loadCategories(context)
    val categoryName = DataManager.getAppCategory(context, app.packageName)
    val category = categories.find { it.name == categoryName }
    val expectedPermissions = category?.permissions?.filterValues { it == true }?.keys ?: emptySet()

    val grantedSpecificPermissions = app.permissions.filter { perm ->
        DataManager.getAllAndroidPermissions().contains(perm) &&
                pm.checkPermission(perm, app.packageName) == PackageManager.PERMISSION_GRANTED
    }

    val allGrantedPermissions = app.permissions.count { perm ->
        DataManager.getAllAndroidPermissions().contains(perm) &&
                pm.checkPermission(perm, app.packageName) == PackageManager.PERMISSION_GRANTED
    }

    val warnings = grantedSpecificPermissions.count { perm ->
        !expectedPermissions.contains(perm)
    }

    // Add animated hover and elevation effects
    var isHovered by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (isHovered) 12.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card elevation"
    )

    // Scale animation
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale animation"
    )

    // Define gradient colors based on security status
    val gradientColors = when {
        warnings > 2 -> listOf(Color(0xFFFF5722), Color(0xFFE64A19)) // Orange/red for many warnings
        warnings > 0 -> listOf(Color(0xFFFFA000), Color(0xFFFF8F00)) // Amber for some warnings
        else -> listOf(Color(0xFF66BB6A), Color(0xFF43A047))         // Green for safe
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation, RoundedCornerShape(16.dp))
            .clickable {
                isHovered = true
                onClick()
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Header with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .background(
                        Brush.horizontalGradient(gradientColors)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App icon with better styling
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.9f),
                                        Color.White.copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 1
                        )

                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                // Category section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = Color(0xFF3949AB),
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = category?.name ?: "Uncategorized",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF3949AB),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Divider(
                    color = Color(0xFFE0E0E0),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Permissions summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Total permissions indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8EAF6))
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3949AB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Text(
                            text = "$allGrantedPermissions Permissions",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3949AB),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Warning indicator (if warnings exist)
                    if (warnings > 0) {
                        // Pulsing animation for warnings
                        val pulseAnimation = rememberInfiniteTransition(label = "pulse")
                        val pulseAlpha by pulseAnimation.animateFloat(
                            initialValue = 0.7f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = EaseInOutQuad),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse animation"
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFF3E0))
                                .padding(8.dp)
                                .graphicsLayer { alpha = pulseAlpha }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5722)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Text(
                                text = "$warnings Warnings",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFF5722),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                // Action row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View Details",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF3949AB),
                        fontWeight = FontWeight.Medium
                    )

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "View Details",
                        tint = Color(0xFF3949AB),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(packageName: String, navController: NavController, appViewModel: AppViewModel) {
    val context = LocalContext.current
    val pm = context.packageManager

    // Use ViewModel to get app details
    var appInfo by remember { mutableStateOf<AppInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load app details using the ViewModel
    LaunchedEffect(packageName) {
        isLoading = true
        appInfo = appViewModel.getAppDetails(packageName)
        isLoading = false
    }

    // Get app icon from cache if available
    val icon = remember {
        appViewModel.getAppIcon(packageName)?.asImageBitmap()
            ?: pm.getApplicationIcon(packageName).toBitmap().asImageBitmap()
    }
    val prefs = context.getSharedPreferences("categories", Context.MODE_PRIVATE)
    val categories = remember { DataManager.loadCategories(context) }
    var selectedCategory by remember {
        mutableStateOf(categories.find {
            prefs.getString(packageName, "") == it.name
        })
    }
    var expanded by remember { mutableStateOf(false) }

    val allGrantedPermissions = appInfo?.permissions?.count { perm ->
        DataManager.getAllAndroidPermissions().contains(perm) &&
                pm.checkPermission(perm, packageName) == PackageManager.PERMISSION_GRANTED
    } ?: 0

    // Add entrance animation
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
        // Add decorative elements
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
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = (-50).dp, y = 650.dp)
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        shape = CircleShape
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = screenAnimation.value
                    translationY = (1f - screenAnimation.value) * 100f
                }
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        "App Details",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3949AB),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = android.net.Uri.parse("package:$packageName")
                        context.startActivity(intent)
                    }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "App Settings",
                            tint = Color.White
                        )
                    }
                }
            )

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF3949AB), Color(0xFF5C6BC0), Color(0xFF7986CB))
                        )
                    )
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // App header card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .shadow(8.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF3949AB).copy(alpha = 0.1f),
                                                    Color(0xFF5C6BC0).copy(alpha = 0.2f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = icon,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    appInfo?.name ?: "",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF3949AB)
                                )

                                Text(
                                    packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Permission counter
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF3949AB).copy(alpha = 0.1f))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF3949AB),
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        "$allGrantedPermissions Active Permissions",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = Color(0xFF3949AB)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Category selector
                                if (categories.isNotEmpty()) {
                                    Text(
                                        "App Category",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = !expanded },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = selectedCategory?.name ?: "Select Category",
                                            onValueChange = {},
                                            readOnly = true,
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Category,
                                                    contentDescription = null,
                                                    tint = Color(0xFF3949AB)
                                                )
                                            },
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(
                                                    expanded = expanded
                                                )
                                            },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF3949AB),
                                                unfocusedBorderColor = Color(0xFF5C6BC0)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            categories.forEach { category ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            category.name,
                                                            fontWeight = if (category.name == selectedCategory?.name)
                                                                FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    },
                                                    onClick = {
                                                        selectedCategory = category
                                                        DataManager.saveAppCategory(
                                                            context,
                                                            packageName,
                                                            category.name
                                                        )
                                                        expanded = false
                                                    },
                                                    leadingIcon = {
                                                        if (category.name == selectedCategory?.name) {
                                                            Icon(
                                                                Icons.Default.CheckCircle,
                                                                contentDescription = null,
                                                                tint = Color(0xFF3949AB)
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // System settings button
                                Button(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        intent.data = android.net.Uri.parse("package:$packageName")
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF3949AB)
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        "Manage App Permissions",
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Permission categories
                    item {
                        appInfo?.let {
                            EnhancedPermissionCategoryCard(
                                "Permissions",
                                listOf(
                                    Triple(
                                        "Microphone",
                                        "android.permission.RECORD_AUDIO",
                                        Icons.Default.Mic
                                    ),
                                    Triple(
                                        "Phone",
                                        "android.permission.READ_PHONE_STATE",
                                        Icons.Default.Phone
                                    ),
                                    Triple(
                                        "Storage",
                                        "android.permission.WRITE_EXTERNAL_STORAGE",
                                        Icons.Default.Storage
                                    ),
                                    Triple(
                                        "Contact",
                                        "android.permission.READ_CONTACTS",
                                        Icons.Default.Contacts
                                    ),
                                    Triple("Camera", "android.permission.CAMERA", Icons.Default.Camera),
                                    Triple(
                                        "Call Logs",
                                        "android.permission.READ_CALL_LOG",
                                        Icons.Default.Call
                                    ),
                                    Triple(
                                        "Location",
                                        "android.permission.ACCESS_FINE_LOCATION",
                                        Icons.Default.LocationOn
                                    ),
                                    Triple(
                                        "Calendar",
                                        "android.permission.READ_CALENDAR",
                                        Icons.Default.CalendarToday
                                    ),Triple(
                                        "Nearby Devices",
                                        "android.permission.BLUETOOTH",
                                        Icons.Default.NearMeDisabled
                                    ),
                                ),
                                it,
                                pm
                            )
                        }
                    }

                    item {
                        EnhancedPermissionCategoryCard(
                            "Additional Permissions",
                            listOf(
                                Triple(
                                    "Wifi",
                                    "android.permission.ACCESS_WIFI_STATE",
                                    Icons.Default.Wifi
                                ),
                                Triple(
                                    "Bluetooth",
                                    "android.permission.BLUETOOTH_CONNECT",
                                    Icons.Default.Bluetooth
                                ),
                                Triple("Internet", "android.permission.INTERNET", Icons.Default.Public),
                                Triple(
                                    "Keyboard",
                                    "android.permission.BIND_INPUT_METHOD",
                                    Icons.Default.Keyboard
                                )
                            ),
                            appInfo,
                            pm
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedPermissionCategoryCard(
    title: String,
    permissions: List<Triple<String, String, ImageVector>>,
    appInfo: AppInfo?,
    pm: PackageManager
) {
    val context = LocalContext.current
    val categoryName = DataManager.getAppCategory(context, appInfo?.packageName?:"")
    val categories = DataManager.loadCategories(context)
    val category = categories.find { it.name == categoryName }
    val expectedPermissions = category?.permissions?.filterValues { it == true }?.keys ?: emptySet()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF3949AB), Color(0xFF5C6BC0))
                        )
                    )
                    .padding(12.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permissions list
            permissions.forEach { (name, perm, icon) ->
                val isGranted = appInfo?.permissions?.contains(perm) == true &&
                        pm.checkPermission(perm, appInfo.packageName) == PackageManager.PERMISSION_GRANTED

                // Check if this permission is unexpected (would trigger a warning)
                val isWarning = isGranted && !expectedPermissions.contains(perm) &&
                        DataManager.getAllAndroidPermissions().contains(perm)

                var isExpanded by remember { mutableStateOf(false) }
                val animatedHeight by animateDpAsState(
                    targetValue = if (isExpanded) 72.dp else 48.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "expand animation"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(animatedHeight)
                        .padding(vertical = 4.dp)
                        .clickable { isExpanded = !isExpanded },
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isWarning -> Color(0xFFFFECB3)  // Light yellow for warning
                            isGranted -> Color(0xFFE8F5E9)  // Light green for granted
                            else -> Color(0xFFF5F5F5)      // Light gray for not granted
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon with background
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    color = when {
                                        isWarning -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                        isGranted -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                        else -> Color.Gray.copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = when {
                                    isWarning -> Color(0xFFFF9800)
                                    isGranted -> Color(0xFF4CAF50)
                                    else -> Color.Gray
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .weight(1f)
                        ) {
                            Text(
                                name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = when {
                                    isWarning -> Color(0xFFFF9800)
                                    isGranted -> Color(0xFF4CAF50)
                                    else -> Color.Gray
                                }
                            )

                            if (isExpanded) {
                                Text(
                                    text = when {
                                        isWarning -> "This permission is not expected for this app category"
                                        isGranted -> "Permission is granted and expected for this app"
                                        else -> "Permission is not granted"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = when {
                                        isWarning -> Color(0xFFFF9800)
                                        isGranted -> Color(0xFF4CAF50)
                                        else -> Color.Gray
                                    },
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        // Status icon
                        Icon(
                            imageVector = when {
                                isWarning -> Icons.Default.Warning
                                isGranted -> Icons.Default.CheckCircle
                                else -> Icons.Default.Cancel
                            },
                            contentDescription = null,
                            tint = when {
                                isWarning -> Color(0xFFFF9800)
                                isGranted -> Color(0xFF4CAF50)
                                else -> Color.Gray
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}