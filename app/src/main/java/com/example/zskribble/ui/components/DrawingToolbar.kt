package com.example.zskribble.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.zskribble.model.DrawingTool

@Composable
fun DrawingToolbar(
    currentTool: DrawingTool,
    onToolChange: (DrawingTool) -> Unit,
    onClear: () -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Color picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Colors",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                ColorPicker(
                    selectedColor = currentTool.color,
                    onColorSelected = { color ->
                        onToolChange(currentTool.copy(color = color))
                    }
                )
            }
            
            Divider(color = Color.LightGray.copy(alpha = 0.3f))
            
            // Brush sizes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Brush Size",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                BrushSizePicker(
                    selectedSize = currentTool.strokeWidth,
                    onSizeSelected = { size ->
                        onToolChange(currentTool.copy(strokeWidth = size))
                    }
                )
            }
            
            Divider(color = Color.LightGray.copy(alpha = 0.3f))
            
            // Tools row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Eraser
                FilledTonalIconButton(
                    onClick = {
                        onToolChange(
                            currentTool.copy(
                                type = if (currentTool.type == DrawingTool.ToolType.BRUSH)
                                    DrawingTool.ToolType.ERASER
                                else
                                    DrawingTool.ToolType.BRUSH
                            )
                        )
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (currentTool.type == DrawingTool.ToolType.ERASER)
                            Color(0xFF6366F1).copy(alpha = 0.2f)
                        else
                            Color.LightGray.copy(alpha = 0.2f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eraser",
                        tint = if (currentTool.type == DrawingTool.ToolType.ERASER)
                            Color(0xFF6366F1)
                        else
                            Color.Gray
                    )
                }
                
                // Undo
                FilledTonalIconButton(
                    onClick = onUndo,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.LightGray.copy(alpha = 0.2f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Undo",
                        tint = Color.Gray
                    )
                }
                
                // Clear all
                Button(
                    onClick = onClear,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444).copy(alpha = 0.1f),
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Text("Clear All")
                }
            }
        }
    }
}

@Composable
fun ColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        Color.Black, Color.Red, Color.Blue, Color.Green,
        Color.Yellow, Color(0xFFFF6B35), Color.Cyan, Color(0xFF8B5CF6)
    )
    
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (selectedColor == color.toArgb().toString()) 3.dp else 0.dp,
                        color = Color(0xFF6366F1),
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color.toArgb().toString()) }
            )
        }
    }
}

@Composable
fun BrushSizePicker(
    selectedSize: Float,
    onSizeSelected: (Float) -> Unit
) {
    val sizes = listOf(3f, 6f, 10f)
    
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        sizes.forEach { size ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (selectedSize == size)
                            Color(0xFF6366F1).copy(alpha = 0.1f)
                        else
                            Color.LightGray.copy(alpha = 0.1f)
                    )
                    .border(
                        width = if (selectedSize == size) 2.dp else 1.dp,
                        color = if (selectedSize == size)
                            Color(0xFF6366F1)
                        else
                            Color.LightGray,
                        shape = CircleShape
                    )
                    .clickable { onSizeSelected(size) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size((size * 1.5f).dp)
                        .clip(CircleShape)
                        .background(
                            if (selectedSize == size)
                                Color(0xFF6366F1)
                            else
                                Color.Gray
                        )
                )
            }
        }
    }
}
