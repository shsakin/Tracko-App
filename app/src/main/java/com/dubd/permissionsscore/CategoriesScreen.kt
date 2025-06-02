package com.dubd.permissionsscore

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dubd.permissionsscore.DataManager.getAllAndroidPermissions
import com.dubd.permissionsscore.DataManager.loadCategories
import com.dubd.permissionsscore.DataManager.saveCategories
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.navigation.NavController
import com.dubd.permissionsscore.viewmodel.AppViewModel

private val primaryColor = Color(0xFF5C6BC0)    // #FF0059
private val secondaryColor = Color(0xFF1A237E)  // #FF7DAB

private val permissionMap = mapOf(
    "Microphone" to "android.permission.RECORD_AUDIO",
    "Phone" to "android.permission.READ_PHONE_STATE",
    "Storage" to "android.permission.WRITE_EXTERNAL_STORAGE",
    "Contact" to "android.permission.READ_CONTACTS",
    "Camera" to "android.permission.CAMERA",
    "Call Logs" to "android.permission.READ_CALL_LOG",
    "Location" to "android.permission.ACCESS_FINE_LOCATION",
    "Nearby Devices" to "android.permission.BLUETOOTH",
    "Calendar" to "android.permission.READ_CALENDAR"
)

@Composable
fun EmptyStateView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Add a subtle animation
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutQuad),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "No Categories Yet",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Create categories to organize app permissions and improve security monitoring",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { /* This will trigger the same action as the + button */ },
                modifier = Modifier
                    .height(48.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5C6BC0)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Create First Category",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PermissionCheckboxes(
    permissions: List<String>,
    selectedPermissions: MutableMap<String, Boolean>
) {
    LazyColumn(
        modifier = Modifier.fillMaxHeight(0.7f),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(permissions) { permission ->
            val checked = selectedPermissions[permission] ?: false
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (checked) secondaryColor.copy(alpha = 0.1f) else Color.Transparent)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { selectedPermissions[permission] = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = primaryColor,
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.White
                    )
                )
                Spacer(Modifier.width(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (permissionMap.entries.find { it.value == permission }?.key) {
                            "Microphone" -> Icons.Default.Mic
                            "Phone" -> Icons.Default.Phone
                            "Storage" -> Icons.Default.Storage
                            "Contact" -> Icons.Default.Contacts
                            "Camera" -> Icons.Default.Camera
                            "Call Logs" -> Icons.Default.Call
                            "Location" -> Icons.Default.LocationOn
                            "Nearby Devices" -> Icons.Default.Bluetooth
                            "Calendar" -> Icons.Default.CalendarToday
                            else -> Icons.Default.Security
                        },
                        contentDescription = null,
                        tint = secondaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        permissionMap.entries.find { it.value == permission }?.key ?: permission,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoriesList(
    categories: List<Category>,
    paddingValues: PaddingValues,
    onEdit: (Category) -> Unit,
    onAddApps: (Category) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { category ->
            // Add enter animation for each item
            val itemVisibility = remember { Animatable(0f) }
            LaunchedEffect(category.name) {
                itemVisibility.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(500, easing = EaseOutQuart)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = itemVisibility.value
                        translationY = (1 - itemVisibility.value) * 50f
                    }
            ) {
                CategoryItem(
                    category = category,
                    onEdit = onEdit,
                    onAddApps = onAddApps
                )
            }
        }
    }
}

@Composable
fun CategoryDialog(
    allPermissions: List<String>,
    initialCategory: Category?,
    onSave: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    val categoryName = remember { mutableStateOf(initialCategory?.name ?: "") }
    val selectedPermissions = remember {
        mutableStateMapOf<String, Boolean>().apply {
            initialCategory?.permissions?.forEach { put(it.key, it.value) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryColor
                        )
                    }
                    Text(
                        if (initialCategory == null) "New Category" else "Edit Category",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            OutlinedTextField(
                value = categoryName.value,
                onValueChange = { categoryName.value = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Category, "Category", tint = secondaryColor)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = secondaryColor,
                    unfocusedBorderColor = primaryColor
                )
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Permissions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = primaryColor
            )

            PermissionCheckboxes(allPermissions, selectedPermissions)

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    onSave(Category(categoryName.value, selectedPermissions.toMap()))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = categoryName.value.isNotBlank() && selectedPermissions.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, "Save")
                Spacer(Modifier.width(8.dp))
                Text("Save Category")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    navController: NavController? = null,
    appViewModel: AppViewModel,
    allApps: List<AppInfo>
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("categories", Context.MODE_PRIVATE)
    val allPermissions = remember { getAllAndroidPermissions() }
    var categories by remember { mutableStateOf(loadCategories(context)) }
    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var showAppsDialog by remember { mutableStateOf<Category?>(null) }

    // Add screen entrance animation
    val screenAnimation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        screenAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = EaseOutQuart)
        )
    }

    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF3949AB), Color(0xFF5C6BC0))
                )
            ),
    ) {
        // Add decorative elements like in HomeScreen
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

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = screenAnimation.value
                    translationY = (1f - screenAnimation.value) * 100f
                },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (navController != null) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Categories",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                    },
                    actions = {
                        // Add button with animation
                        val rotation = remember { Animatable(0f) }

                        IconButton(
                            onClick = {
                                showDialog = true
//                                scope.launch {
//                                    rotation.animateTo(
//                                        targetValue = 90f,
//                                        animationSpec = tween(300, easing = EaseOutQuart)
//                                    )
//                                    rotation.snapTo(0f)
//                                }
                            },
                            modifier = Modifier
                                .padding(8.dp)
                                .graphicsLayer {
                                    rotationZ = rotation.value
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.3f),
                                                Color.White.copy(alpha = 0.1f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Category",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF3949AB)
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            if (categories.isEmpty()) {
                EmptyStateView(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                )
            } else {
                CategoriesList(
                    categories = categories,
                    paddingValues = paddingValues,
                    onEdit = { category ->
                        editingCategory = category
                        showDialog = true
                    },
                    onAddApps = { category ->
                        showAppsDialog = category
                    }
                )
            }
        }

        if (showAppsDialog != null) {
            // Animated background entrance
            val dialogAnimation = remember { Animatable(0f) }
            LaunchedEffect(showAppsDialog) {
                dialogAnimation.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(300, easing = EaseOutQuart)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = dialogAnimation.value }
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                AppsDialog(
                    category = showAppsDialog!!,
                    context = context,
                    onSave = { selectedPackageNames ->
                        allApps.forEach { app ->
                            val currentCategory =
                                DataManager.getAppCategory(context, app.packageName)
                            if (selectedPackageNames.contains(app.packageName)) {
                                DataManager.saveAppCategory(
                                    context,
                                    app.packageName,
                                    showAppsDialog!!.name
                                )
                            } else if (currentCategory == showAppsDialog!!.name) {
                                DataManager.clearAppCategory(context, app.packageName)
                            }
                        }
                        showAppsDialog = null
                    },
                    onDismiss = { showAppsDialog = null },
                    allApps= allApps
                )
            }
        }

        if (showDialog) {
            // Animated background entrance
            val dialogAnimation = remember { Animatable(0f) }
            LaunchedEffect(showDialog) {
                dialogAnimation.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(300, easing = EaseOutQuart)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = dialogAnimation.value }
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                CategoryDialog(
                    allPermissions = allPermissions,
                    initialCategory = editingCategory,
                    onSave = { newCategory ->
                        categories = if (editingCategory == null) {
                            categories + newCategory
                        } else {
                            categories.map { if (it.name == editingCategory!!.name) newCategory else it }
                        }
                        saveCategories(context, categories)
                        showDialog = false
                        editingCategory = null
                    },
                    onDismiss = { showDialog = false; editingCategory = null }
                )
            }
        }
    }
}

@Composable
fun CategoryItem(category: Category, onEdit: (Category) -> Unit, onAddApps: (Category) -> Unit) {
    // Add hover/press animation
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
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale animation"
    )

    // Define permission count for gradient colors
    val permissionCount = category.permissions.count { it.value }
    val gradientColors = when {
        permissionCount > 10 -> listOf(Color(0xFF9C27B0), Color(0xFF7B1FA2)) // Purple for many permissions
        permissionCount > 5 -> listOf(Color(0xFF3949AB), Color(0xFF303F9F))  // Indigo for moderate
        else -> listOf(Color(0xFF039BE5), Color(0xFF0288D1))                 // Blue for few
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation, RoundedCornerShape(16.dp))
            .clickable {
                isHovered = true
                // No action on general click - we have specific action buttons
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(gradientColors)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category icon with animation
                    val rotateAnimation = rememberInfiniteTransition(label = "rotate")
                    val iconRotation by rotateAnimation.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(20000, easing = EaseInOutQuad),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "icon rotation"
                    )

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.9f),
                                        Color.White.copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = gradientColors[0],
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer {
                                    rotationZ = iconRotation
                                }
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            category.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                "${category.permissions.count { it.value }} Permissions",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit button
                OutlinedButton(
                    onClick = { onEdit(category) },
                    modifier = Modifier
                        .height(40.dp)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = gradientColors[0]
                    ),
                    border = BorderStroke(1.dp, gradientColors[0]),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 14.sp)
                }

                // Add apps button
                Button(
                    onClick = { onAddApps(category) },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = gradientColors[0]
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        Icons.Default.Apps,
                        contentDescription = "Add Apps",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Assign Apps", fontSize = 14.sp)
                }
            }

            // Permissions preview section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    "Permission Types",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Display permission types in a flowing layout
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activePermissions = category.permissions
                        .filter { it.value }
                        .keys
                        .take(5) // Show only first 5 to avoid clutter

                    activePermissions.forEach { permission ->
                        val permName = permission.split(".").lastOrNull() ?: permission
                        val displayName = permName.replace("_", " ")
                            .lowercase()
                            .replaceFirstChar { it.uppercase() }
                            .take(15) + if (permName.length > 15) "..." else ""

                        Surface(
                            color = gradientColors[0].copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = getIconForPermissionType(permission),
                                    contentDescription = null,
                                    tint = gradientColors[0],
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = displayName,
                                    fontSize = 12.sp,
                                    color = gradientColors[0]
                                )
                            }
                        }
                    }

                    if (category.permissions.count { it.value } > 5) {
                        Surface(
                            color = gradientColors[0].copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "+${category.permissions.count { it.value } - 5} more",
                                    fontSize = 12.sp,
                                    color = gradientColors[0],
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getIconForPermissionType(permission: String): ImageVector {
    return when {
        permission.contains("CAMERA", ignoreCase = true) -> Icons.Default.Camera
        permission.contains("LOCATION", ignoreCase = true) -> Icons.Default.LocationOn
        permission.contains("RECORD_AUDIO", ignoreCase = true) -> Icons.Default.Mic
        permission.contains("READ_CONTACTS", ignoreCase = true) -> Icons.Default.Contacts
        permission.contains("CALL", ignoreCase = true) -> Icons.Default.Call
        permission.contains("STORAGE", ignoreCase = true) -> Icons.Default.Storage
        permission.contains("BLUETOOTH", ignoreCase = true) -> Icons.Default.Bluetooth
        permission.contains("CALENDAR", ignoreCase = true) -> Icons.Default.CalendarToday
        permission.contains("SMS", ignoreCase = true) -> Icons.Default.Sms
        permission.contains("PHONE", ignoreCase = true) -> Icons.Default.Phone
        else -> Icons.Default.Lock
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val horizontalGapPx = 0
        val verticalGapPx = 0

        val rows = mutableListOf<MutableList<androidx.compose.ui.layout.Placeable>>()
        val rowWidths = mutableListOf<Int>()

        var rowHeight = 0
        var rowWidth = 0
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()

        measurables.forEach { measurable ->
            val placeable = measurable.measure(
                constraints.copy(
                    minWidth = 0,
                    minHeight = 0
                )
            )

            if (rowWidth + placeable.width + horizontalGapPx > constraints.maxWidth) {
                rows.add(currentRow)
                rowWidths.add(rowWidth - horizontalGapPx)
                rowWidth = 0
                currentRow = mutableListOf()
            }

            currentRow.add(placeable)
            rowWidth += placeable.width + horizontalGapPx
            rowHeight = maxOf(rowHeight, placeable.height)
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(rowWidth - horizontalGapPx)
        }

        val width = rowWidths.maxOrNull() ?: 0
        val height = rowHeight * rows.size + (verticalGapPx * (rows.size - 1))

        val placeablesY = mutableListOf<Int>()
        for (i in rows.indices) {
            placeablesY.add(i * (rowHeight + verticalGapPx))
        }

        layout(
            width = constraints.constrainWidth(width),
            height = constraints.constrainHeight(height)
        ) {
            rows.forEachIndexed { rowIndex, row ->
                var rowX = 0

                row.forEach { placeable ->
                    placeable.place(
                        x = rowX,
                        y = placeablesY[rowIndex]
                    )
                    rowX += placeable.width + horizontalGapPx
                }
            }
        }
    }
}

@Composable
fun AppsDialog(
    category: Category,
    context: Context,
    onSave: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
    allApps: List<AppInfo>
) {
    val selectedApps = remember {
        mutableStateMapOf<String, Boolean>().apply {
            allApps.forEach { app ->
                // Check if this app is already in the current category
                val isInCategory =
                    DataManager.getAppCategory(context, app.packageName) == category.name
                put(app.packageName, isInCategory)
            }
        }
    }

    // Add search state
    var searchQuery by remember { mutableStateOf("") }

    // Filter apps based on search query
    val filteredApps = remember(searchQuery, allApps) {
        if (searchQuery.isEmpty()) {
            allApps
        } else {
            allApps.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.Gray)
                    }
                    Text(
                        "Add Apps to ${category.name}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            // Add search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Search apps...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = primaryColor
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = secondaryColor,
                    unfocusedBorderColor = primaryColor.copy(alpha = 0.5f),
                    cursorColor = primaryColor
                )
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                items(filteredApps) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedApps[app.packageName] == true)
                                    secondaryColor.copy(alpha = 0.1f)
                                else Color.Transparent
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedApps[app.packageName] ?: false,
                            onCheckedChange = { selectedApps[app.packageName] = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = primaryColor,
                                uncheckedColor = Color.Gray,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(Modifier.width(12.dp))

                        // App icon
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { ctx ->
                                android.widget.ImageView(ctx).apply {
                                    try {
                                        setImageDrawable(ctx.packageManager.getApplicationIcon(app.packageName))
                                    } catch (e: Exception) {
                                        // Fallback to default icon if app icon can't be loaded
                                        setImageResource(android.R.drawable.sym_def_app_icon)
                                    }
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                app.name,
                                color = Color(0xFF1A1A1A),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                app.packageName,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val selectedPackageNames = selectedApps.filterValues { it }.keys
                    onSave(selectedPackageNames)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, "Save")
                Spacer(Modifier.width(8.dp))
                Text("Save Apps")
            }
        }
    }
}
