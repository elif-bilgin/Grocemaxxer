package com.grocemaxxer.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun App(viewModel: GroceryListViewModel = remember { GroceryListViewModel() }) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var inputText by remember { mutableStateOf("") }

            fun submit() {
                viewModel.addItems(inputText)
                inputText = ""
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Grocemaxxer", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Add what you need — we'll order it to match a low-backtrack walk through the store.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("e.g. milk, eggs, bread, apples") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        singleLine = true,
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { submit() }) { Text("Add") }
                }

                Spacer(Modifier.height(12.dp))

                if (viewModel.totalCount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${viewModel.checkedCount} of ${viewModel.totalCount} collected")
                        Row {
                            TextButton(onClick = viewModel::clearChecked) { Text("Clear checked") }
                            TextButton(onClick = viewModel::clearAll) { Text("Clear all") }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                if (viewModel.groups.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Your route-optimized checklist will show up here.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        viewModel.groups.forEach { group ->
                            item {
                                Text(
                                    text = group.section.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                                )
                            }
                            items(group.items, key = { it.id }) { groceryItem ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = groceryItem.isChecked,
                                        onCheckedChange = { viewModel.toggleChecked(groceryItem.id) },
                                    )
                                    Text(
                                        text = groceryItem.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textDecoration = if (groceryItem.isChecked) TextDecoration.LineThrough else null,
                                        modifier = Modifier.weight(1f),
                                    )
                                    IconButton(onClick = { viewModel.removeItem(groceryItem.id) }) {
                                        Text("✕")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
