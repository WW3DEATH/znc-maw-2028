package com.example.ui.redemption

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.model.RedemptionItem
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedemptionScreen(
    currentUser: UserProfile
) {
    val items by DataRepository.redemptionItems.collectAsState()
    val userRedemptions by DataRepository.userRedemptions.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showEditPriceDialog by remember { mutableStateOf<RedemptionItem?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val isAdmin = currentUser.role == UserRole.ADMIN

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SP Redemption Center",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Zahira Science Points Store",
                            fontSize = 12.sp,
                            color = ZahiraGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZahiraMaroon
                ),
                actions = {
                    if (isAdmin) {
                        IconButton(
                            onClick = { showAddItemDialog = true },
                            modifier = Modifier.testTag("btn_admin_add_item")
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add Item", tint = ZahiraGold)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    onClick = { showAddItemDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Catalog Item") },
                    containerColor = ZahiraGold,
                    contentColor = Color(0xFF451A03),
                    modifier = Modifier.testTag("fab_add_redemption_item")
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SurfaceLight)
        ) {
            // User SP Balance Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(ZahiraMaroon, ZahiraMaroonDark)
                            )
                        )
                        .border(1.dp, ZahiraGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ZahiraGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFF451A03),
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AVAILABLE BALANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ZahiraGold
                            )
                            Text(
                                text = "${currentUser.spPoints} SP Points",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Earn more by ranking Top 10 in Wednesday A/L Quizzes",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Snackbar notification
            snackbarMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = msg, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Items List
            Text(
                text = "ACADEMIC REWARD CATALOG",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (items.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(ZahiraGold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CardGiftcard,
                                        contentDescription = null,
                                        tint = ZahiraMaroon,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Redemption Center is Empty",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = ZahiraMaroon
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isAdmin) {
                                        "All catalog items have been cleared. Tap '+ Add Item' at the top to list new rewards and set their SP point costs."
                                    } else {
                                        "There are currently no items in the reward catalog. New study materials and awards will be posted by the Administrator soon!"
                                    },
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                items(items) { item ->
                    RedemptionItemCard(
                        item = item,
                        userSp = currentUser.spPoints,
                        isAdmin = isAdmin,
                        onRedeem = {
                            val res = DataRepository.redeemItem(item)
                            if (res.isSuccess) {
                                snackbarMessage = res.getOrNull()
                            } else {
                                snackbarMessage = res.exceptionOrNull()?.message
                            }
                        },
                        onEditPrice = {
                            showEditPriceDialog = item
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Admin Add Item Dialog
    if (showAddItemDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newDesc by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("Books") }
        var newPriceStr by remember { mutableStateOf("100") }
        var newStockStr by remember { mutableStateOf("20") }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = {
                Text("Add New Redemption Item", fontWeight = FontWeight.Bold, color = ZahiraMaroon)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Only administrators can add items and define prices.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Item Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newPriceStr,
                            onValueChange = { newPriceStr = it },
                            label = { Text("SP Price") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newStockStr,
                            onValueChange = { newStockStr = it },
                            label = { Text("Initial Stock") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = newPriceStr.toIntOrNull() ?: 100
                        val stock = newStockStr.toIntOrNull() ?: 20
                        if (newTitle.isNotBlank()) {
                            DataRepository.adminAddRedemptionItem(
                                RedemptionItem(
                                    id = "item_${System.currentTimeMillis()}",
                                    title = newTitle.trim(),
                                    description = newDesc.trim(),
                                    category = newCategory,
                                    spPrice = price,
                                    stock = stock
                                )
                            )
                            snackbarMessage = "Added item '$newTitle' successfully!"
                            showAddItemDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZahiraMaroon)
                ) {
                    Text("Add Item")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Admin Edit Price Dialog
    showEditPriceDialog?.let { item ->
        var editPriceStr by remember { mutableStateOf(item.spPrice.toString()) }

        AlertDialog(
            onDismissRequest = { showEditPriceDialog = null },
            title = {
                Text("Set Price for ${item.title}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text("Modify the required SP points for this academic reward.", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editPriceStr,
                        onValueChange = { editPriceStr = it },
                        label = { Text("SP Price") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newPrice = editPriceStr.toIntOrNull() ?: item.spPrice
                        DataRepository.adminUpdateRedemptionPrice(item.id, newPrice)
                        snackbarMessage = "Updated price to $newPrice SP"
                        showEditPriceDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZahiraMaroon)
                ) {
                    Text("Update Price")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPriceDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RedemptionItemCard(
    item: RedemptionItem,
    userSp: Int,
    isAdmin: Boolean,
    onRedeem: () -> Unit,
    onEditPrice: () -> Unit
) {
    val canAfford = userSp >= item.spPrice
    val inStock = item.stock > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Item Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ZahiraMaroon.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (item.category) {
                            "Books" -> Icons.AutoMirrored.Filled.MenuBook
                            "Equipment" -> Icons.Default.Calculate
                            "Merchandise" -> Icons.Default.MilitaryTech
                            else -> Icons.Default.CardGiftcard
                        },
                        contentDescription = null,
                        tint = ZahiraMaroon,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price Tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "${item.spPrice} SP",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Stock: ${item.stock}",
                        fontSize = 11.sp,
                        color = if (inStock) TextSecondary else Color(0xFFEF4444)
                    )
                }

                // Actions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isAdmin) {
                        OutlinedButton(
                            onClick = onEditPrice,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_edit_price_${item.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Price", fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = onRedeem,
                        enabled = canAfford && inStock,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZahiraMaroon
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_redeem_${item.id}")
                    ) {
                        Text(
                            text = if (!inStock) "Out of Stock" else if (canAfford) "Redeem" else "Need ${item.spPrice - userSp} SP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
