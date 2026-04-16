package com.example.zskribble.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.zskribble.model.DrawingTool
import com.example.zskribble.model.Point
import com.example.zskribble.model.Stroke as StrokeModel

@Composable
fun DrawingCanvas(
    strokes: List<StrokeModel>,
    isDrawingEnabled: Boolean,
    currentTool: DrawingTool,
    onStrokeComplete: (StrokeModel) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPath by remember { mutableStateOf<List<Point>>(emptyList()) }
    
    Canvas(
        modifier = modifier
            .pointerInput(isDrawingEnabled, currentTool) {
                if (isDrawingEnabled) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = listOf(Point(offset.x, offset.y))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPath = currentPath + Point(change.position.x, change.position.y)
                        },
                        onDragEnd = {
                            if (currentPath.isNotEmpty()) {
                                onStrokeComplete(
                                    StrokeModel.create(
                                        points = currentPath,
                                        tool = currentTool
                                    )
                                )
                                currentPath = emptyList()
                            }
                        }
                    )
                }
            }
    ) {
        // Draw completed strokes
        strokes.forEach { stroke ->
            drawStroke(stroke)
        }
        
        // Draw current stroke being drawn
        if (currentPath.isNotEmpty()) {
            val tempStroke = StrokeModel.create(points = currentPath, tool = currentTool)
            drawStroke(tempStroke)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStroke(stroke: StrokeModel) {
    if (stroke.points.size < 2) return
    
    val path = Path()
    val firstPoint = stroke.points.first()
    path.moveTo(firstPoint.x, firstPoint.y)
    
    for (i in 1 until stroke.points.size) {
        val point = stroke.points[i]
        path.lineTo(point.x, point.y)
    }
    
    // Use white color for eraser, otherwise use the tool color
    val color = if (stroke.tool.type == DrawingTool.ToolType.ERASER) {
        Color.White
    } else {
        try {
            Color(stroke.tool.color.toLong())
        } catch (e: Exception) {
            Color.Black
        }
    }
    
    drawPath(
        path = path,
        color = color.copy(alpha = stroke.tool.opacity),
        style = Stroke(
            width = if (stroke.tool.type == DrawingTool.ToolType.ERASER) 
                stroke.tool.strokeWidth * 2 // Make eraser wider
            else 
                stroke.tool.strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
