package fr.isen.proto.androideresraurant

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import fr.isen.proto.androideresraurant.ui.theme.AndroidEResraurantTheme
import org.json.JSONObject
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar



class CategoryActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val category = intent.getStringExtra("category") ?: ""
        setContent {
            AndroidEResraurantTheme {
                sharedPreferences = getSharedPreferences("cart", Context.MODE_PRIVATE)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        ToolBar(
                            onCartClick = {  },
                            cartItemCount = getCartItemCount()
                        )
                        MenuContent(category)
                    }
                }
            }
        }
    }

    private fun getCartItemCount(): Int = sharedPreferences.getInt("cartItemCount", 0)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ToolBar(onCartClick: () -> Unit, cartItemCount: Int) {
        TopAppBar(
            title = { Text("ProtoFood", color = MaterialTheme.colorScheme.onPrimary) },
            actions = {
                if (cartItemCount > 0) {
                    BadgedBox(badge = { Badge { Text("$cartItemCount") } }) {
                        IconButton(onClick = onCartClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_shopping_cart),
                                contentDescription = "Cart",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                } else {
                    IconButton(onClick = onCartClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shopping_cart),
                            contentDescription = "Cart",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        )
    }



    @Composable
    fun MenuContent(selectedCategory: String) {
        var menuData by remember { mutableStateOf<MenuData?>(null) }
        var loading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            val queue = Volley.newRequestQueue(this@CategoryActivity)
            val url = "http://test.api.catering.bluecodegames.com/menu"
            val params = JSONObject().apply { put("id_shop", "1") }

            val request = JsonObjectRequest(Request.Method.POST, url, params, { response ->
                menuData = Gson().fromJson(response.toString(), MenuData::class.java)
                loading = false
            }, { error ->
                loading = false
                error.printStackTrace()
            })

            queue.add(request)
        }

        if (loading) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            menuData?.data?.find { it.name_fr == selectedCategory }?.let { category ->
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(category.items) { item ->
                        MenuItem(item)
                    }
                }
            }
        }
    }

    @Composable
    fun MenuItem(item: MonMenuItem) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = item.name_fr,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow {
                    items(item.images) { imageUrl ->
                        Image(
                            painter = rememberImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .padding(end = 8.dp)
                        )
                    }
                }
                // autres infos
            }
        }
    }
}


data class MenuData(
    val data: List<CategoryItem>
)

data class CategoryItem(
    val name_fr: String,
    val items: List<MonMenuItem>
)

data class MonMenuItem(
    val itemId: String,
    val name_fr: String,
    val images: List<String>,
    val prices: List<Price>,
    val ingredients: List<Ingredient>
)

data class Price(
    val price: Double
)

data class Ingredient(
    val name_fr: String
)
