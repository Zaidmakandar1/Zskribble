package com.example.zskribble.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zskribble.ui.components.DrawingCanvas
import com.example.zskribble.ui.components.DrawingToolbar
import com.example.zskribble.viewmodel.GameViewModel

@Composable
fun DrawingTestScreen(
    viewModel: GameViewModel = viewModel()
) {
    val strokes by viewModel.strokes.collectAsState()
    val currentTool by viewModel.currentTool.collectAsState()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Drawing canvas
            DrawingCanvas(
                strokes = strokes,
                isDrawingEnabled = true,
                currentTool = currentTool,
                onStrokeComplete = viewModel::onStrokeComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
            )
            
            // Toolbar
            DrawingToolbar(
                currentTool = currentTool,
                onToolChange = viewModel::onToolChange,
                onClear = viewModel::onClear,
                onUndo = viewModel::onUndo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}
