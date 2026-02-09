package com.example.horiz.componets

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.horiz.components.SubjectCard
import com.example.horiz.model.SubjectNode

@Composable
fun SubjectList(items: List<SubjectNode>, onEdit: (SubjectNode) -> Unit, onDelete: (SubjectNode) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 12.dp)) {
        items(items, key = { it.i }) { item ->
            SubjectCard(item, { onEdit(item) }, { onDelete(item) })
        }
    }
}
