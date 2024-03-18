package com.akapps.instantapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akapps.instantapp.ui.theme.DailyNoteTheme
import com.google.android.gms.instantapps.InstantApps
import kotlin.math.roundToInt

data class Note(val content: String, val initialWidth: Dp = 50.dp, val initialHeight: Dp = 50.dp)

private val notesList = listOf(
    Note(content = "First note", initialWidth = 160.dp, initialHeight = 90.dp),
    Note(content = "Second note", initialWidth = 90.dp, initialHeight = 160.dp),
    Note(content = "Third note", initialWidth = 50.dp, initialHeight = 80.dp),
    Note(content = "Fourth note", initialWidth = 300.dp, initialHeight = 400.dp),
    Note(content = "Fifth note", initialWidth = 160.dp, initialHeight = 90.dp),
    Note(content = "Sixth note", initialWidth = 500.dp, initialHeight = 500.dp),
    Note(content = "Seventh note", initialWidth = 90.dp, initialHeight = 160.dp),
    Note(content = "Eighth note", initialWidth = 500.dp, initialHeight = 500.dp),
    Note(content = "Ninth note", initialWidth = 50.dp, initialHeight = 80.dp),
    Note(content = "Tenth note", initialWidth = 300.dp, initialHeight = 400.dp),
    Note(content = "Eleventh note", initialWidth = 600.dp, initialHeight = 900.dp),
    Note(content = "Twelfth note", initialWidth = 500.dp, initialHeight = 500.dp),
    Note(content = "Thirteenth note", initialWidth = 900.dp, initialHeight = 600.dp),
    Note(content = "Fourteenth note", initialWidth = 500.dp, initialHeight = 500.dp),
    Note(content = "Fifteenth note", initialWidth = 300.dp, initialHeight = 400.dp),
    Note(content = "Sixteenth note", initialWidth = 50.dp, initialHeight = 80.dp),
    Note(content = "Seventeenth note", initialWidth = 500.dp, initialHeight = 500.dp),
    Note(content = "Eighteenth note", initialWidth = 1600.dp, initialHeight = 900.dp),
    Note(content = "Nineteenth note", initialWidth = 50.dp, initialHeight = 80.dp),
    Note(content = "Twentieth note", initialWidth = 50.dp, initialHeight = 80.dp)
)

class InstantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DailyNoteTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    InstantMessage()
                    //ResizeableLazyGrid(notesList)
                    //StaggeredList()
                }
            }
        }
    }

    @Composable
    fun ResizableListItem(
        modifier: Modifier = Modifier,
        initialWidth: Int = 100,
        initialHeight: Int = 100,
        content: @Composable () -> Unit
    ) {
        var width by remember { mutableStateOf(initialWidth.dp) }
        var height by remember { mutableStateOf(initialHeight.dp) }

        Surface(
            modifier = modifier
                .widthIn(min = 50.dp, max = 300.dp)
                .heightIn(min = 50.dp, max = 300.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        width += dragAmount.x.dp
                        height += dragAmount.y.dp
                    }
                }
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Box {
                content()
            }
        }
    }

    @Composable
    fun StaggeredList() {
        val itemsWidths = remember { mutableStateListOf(150.dp, 150.dp, 150.dp, 150.dp) }
        val itemsHeights = remember { mutableStateListOf(150.dp, 150.dp, 150.dp, 150.dp) }

        Layout(
            content = {
                itemsWidths.forEachIndexed { index, width ->
                    ResizableListItem(
                        modifier = Modifier
                            .width(width)
                            .height(itemsHeights[index])
                            .padding(8.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = "Testing the size"
                        )
                    }
                }
            }
        ) { measurables, constraints ->
            layout(constraints.maxWidth, constraints.maxHeight) {
                var yPosition = 0
                var xPosition = 0
                measurables.forEachIndexed { index, measurable ->
                    val placeable = measurable.measure(
                        constraints.copy(
                            minWidth = 0,
                            minHeight = 0,
                            maxWidth = itemsWidths[index].roundToPx(),
                            maxHeight = itemsHeights[index].roundToPx()
                        )
                    )
                    placeable.placeRelative(x = xPosition, y = yPosition)
                    xPosition += placeable.width
                    if (xPosition > constraints.maxWidth - placeable.width) {
                        xPosition = 0
                        yPosition += placeable.height
                    }
                }
            }
        }
    }

//    @Composable
//    fun ResizableListItem(
//        modifier: Modifier = Modifier,
//        initialWidth: Int = 100,
//        initialHeight: Int = 100,
//        content: @Composable () -> Unit
//    ) {
//        var width by remember { mutableStateOf(initialWidth.dp) }
//        var height by remember { mutableStateOf(initialHeight.dp) }
//
//        Surface(
//            modifier = modifier
//                .size(width, height)
//                .pointerInput(Unit) {
//                    detectDragGestures { change, dragAmount ->
//                        width += dragAmount.x.dp / 2
//                        height += dragAmount.y.dp / 2
//                    }
//                }
//                .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
//        ) {
//            Box {
//                content()
//            }
//        }
//    }

//    @Composable
//    fun ResizableList() {
//        Column(
//            modifier = Modifier
//                .background(Color.Blue)
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            ResizableListItem {
//                Text(
//                    modifier = Modifier.padding(16.dp),
//                    text = "Testing the size"
//                )
//            }
//        }
//    }

//    @Composable
//    fun StaggeredList() {
//        LazyVerticalGrid(
//            columns = GridCells.Fixed(2),
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            items(10) { index ->
//                ResizableListItem {
//                    Text(
//                    modifier = Modifier.padding(16.dp),
//                    text = "Testing the size"
//                )
//                }
//            }
//        }
//    }

    @Composable
    private fun ResizeableLazyGrid(noteList: List<Note>) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 200.dp),
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = {
                items(noteList) { note ->
                    var offsetX by remember { mutableFloatStateOf(0f) }
                    var offsetY by remember { mutableFloatStateOf(0f) }
                    var widthOffset by remember { mutableFloatStateOf(note.initialWidth.value) }
                    var heightOffset by remember { mutableFloatStateOf(note.initialHeight.value) }
                    Log.d("Here", "width offset is $widthOffset")
                    Log.d("Here", "height offset is $heightOffset")
                    Card(
                        modifier = Modifier
                            .width(widthOffset.dp)
                            .height(heightOffset.dp)
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(6.dp),
                        content = {
                            Text(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .wrapContentSize()
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            offsetX += dragAmount.x
                                            offsetY += dragAmount.y
                                            widthOffset -= dragAmount.x
                                            //heightOffset += dragAmount.y
                                        }
                                    },
                                text = "${note.content}\n${note.initialWidth.value.roundToInt()} x ${note.initialHeight.value.roundToInt()}"
                            )
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    @Composable
    private fun InstantMessage() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.app_info),
                        textAlign = TextAlign.Center
                    )

                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp),
                        onClick = { showInstallPrompt() }
                    ) {
                        Text(stringResource(R.string.button_download))
                    }
                }
            }
        }
    }

    private fun showInstallPrompt() {
        val postInstall = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_DEFAULT)
            .setPackage(packageName)

        InstantApps.showInstallPrompt(this, postInstall, 0, null)
    }

}