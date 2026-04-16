package com.example.zskribble.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.util.UUID

data class Point(
    val x: Float = 0f,
    val y: Float = 0f,
    val pressure: Float = 1.0f
)

data class DrawingTool(
    val type: ToolType = ToolType.BRUSH,
    val color: String = Color.Black.toArgb().toString(),
    val strokeWidth: Float = 5f,
    val opacity: Float = 1.0f
) {
    enum class ToolType {
        BRUSH, ERASER
    }
    
    companion object {
        fun default() = DrawingTool(
            type = ToolType.BRUSH,
            color = Color.Black.toArgb().toString(),
            strokeWidth = 5f
        )
    }
}

data class Stroke(
    val id: String = "",
    val points: List<Point> = emptyList(),
    val tool: DrawingTool = DrawingTool.default(),
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun create(points: List<Point>, tool: DrawingTool) = Stroke(
            id = UUID.randomUUID().toString(),
            points = points,
            tool = tool,
            timestamp = System.currentTimeMillis()
        )
    }
}
