package com.example.feature_add_recipe

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.db.dto.Recipe
import com.example.feature_recipe.RecipeViewModel
import kotlinx.coroutines.launch
import com.example.core.ImageHelper
import com.example.core.ImageHelper.Companion.copyImageToInternalStorage

@Composable
fun CreateRoute(
    nameScreen: String,
    // coroutineScope: CoroutineScope = rememberCoroutineScope(),
    viewModel: com.example.feature_recipe.RecipeViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var ingredients by remember { mutableStateOf(mutableListOf<Pair<String, String>>()) }
    var preparation by remember { mutableStateOf(TextFieldValue("")) }
    var photo by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var newIngredientName by remember { mutableStateOf(TextFieldValue("")) }
    var newIngredientQuantity by remember { mutableStateOf(TextFieldValue("")) }

    val context = LocalContext.current

    // Лаунчер для выбора изображения
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            selectedImageUri = uri // Сохраняем URI выбранного изображения
        }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF6200EE))
            .padding(16.dp)
    ) {
        Text(
            text = nameScreen,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp) // Паддинг для отступов слева и справа
            .padding(top = 80.dp)
        .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {


        // Название
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Название") },
            modifier = Modifier.fillMaxWidth()
        )

        // Таблица ингредиентов
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Ингредиенты",
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ingredients.forEachIndexed { index, (name, quantity) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { newName -> ingredients[index] = newName to quantity },
                        label = { Text("Ингредиент") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { newQuantity -> ingredients[index] = name to newQuantity },
                        label = { Text("Количество") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Поля для добавления нового ингредиента
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newIngredientName,
                    onValueChange = { newIngredientName = it },
                    label = { Text("Ингредиент") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = newIngredientQuantity,
                    onValueChange = { newIngredientQuantity = it },
                    label = { Text("Количество") },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    if (newIngredientName.text.isNotBlank() && newIngredientQuantity.text.isNotBlank()) {
                        ingredients.add(newIngredientName.text to newIngredientQuantity.text)
                        newIngredientName = TextFieldValue("") // Очистка поля после добавления
                        newIngredientQuantity = TextFieldValue("") // Очистка поля после добавления
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Добавить ингредиент")
            }
        }
        //}

        // Способ приготовления
        OutlinedTextField(
            value = preparation,
            onValueChange = { preparation = it },
            label = { Text("Способ приготовления") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 5
        )

        // Фото
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Фото рецепта", fontSize = 18.sp, color = Color.Black)
            selectedImageUri?.let{ uri -> Box(
                modifier = Modifier
                    .size(150.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                //if (photo != null) {
               // selectedImageUri?.let { uri ->
                    Image(
                        //painter = painterResource(id = photo!!),
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Фото рецепта",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                //} //else {
                    ?:
                    Text(
                        text = "Нет изображения",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
            Button(onClick = { launcher.launch("image/*") },) {
                Text("Добавить фото")
            }
        }

        // Кнопка сохранения
        Button(
            onClick = {
                scope.launch {
                    try {
                        val savedImagePath = selectedImageUri?.let { uri ->
                            copyImageToInternalStorage(context, uri)
                        }
                        viewModel.addRecipe(
                            Recipe(
                                name = title.text,
                                ingredients = ingredients.joinToString("\n") { "${it.first}: ${it.second}" },
                                instructions = preparation.text,
                                imageUrl = savedImagePath
                            )
                        )
                        Toast.makeText(context, "Рецепт добавлен!", Toast.LENGTH_SHORT).show()
                        // Очистка полей
                        title = TextFieldValue("")
                        ingredients.clear()
                        preparation = TextFieldValue("")
                        selectedImageUri = null
                        newIngredientName = TextFieldValue("")
                        newIngredientQuantity = TextFieldValue("")
                    } catch (e: Exception) {
                        Toast.makeText(context, "Ошибка сохранения: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Сохранить рецепт")
        }
    }
}