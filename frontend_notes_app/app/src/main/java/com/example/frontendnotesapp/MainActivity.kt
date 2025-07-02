package com.example.frontendnotesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * PUBLIC_INTERFACE
 * MainActivity is the app's launcher and entry point. It hosts Compose UI and handles all navigation
 * between the notes list, add/edit dialogs, and note details.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesApp()
        }
    }
}

/** Note data class for the app (simple in-memory storage). */
data class Note(
    val id: Int,
    var title: String,
    var content: String
)

@Composable
fun NotesApp() {
    NotesAppTheme {
        Surface(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            NotesScreen()
        }
    }
}

/**
 * PUBLIC_INTERFACE
 * The main notes screen with tabular list view, floating action button, inline and dialog edit support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen() {
    // In-memory note list & state for demonstration.
    var notes by remember {
        mutableStateOf(
            listOf(
                Note(1, "Welcome", "Tap a note to see details. Swipe left/right to delete or tap pencil to edit!")
            )
        )
    }
    var showDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editableNote by remember { mutableStateOf<Note?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var noteForDetails by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = AppColors.primary,
                contentColor = Color.White,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Note")
            }
        },
        topBar = {
            // Use TopAppBar which is stable in material3
            TopAppBar(
                title = { Text("Notes", color = AppColors.primary, fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No notes yet.\nTap + to add new!", color = AppColors.secondary, fontSize = 16.sp, fontWeight = FontWeight.Light)
                }
            } else {
                LazyColumn(Modifier.padding(8.dp)) {
                    items(notes, key = { it.id }) { note ->
                        NoteRow(
                            note = note,
                            onDelete = {
                                notes = notes.filter { n -> n.id != note.id }
                            },
                            onEdit = {
                                editableNote = note.copy()
                                showEditDialog = true
                            },
                            onClick = {
                                noteForDetails = note
                                showDetailsDialog = true
                            }
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }

        if (showDialog) {
            AddEditNoteDialog(
                title = "Add Note",
                initialTitle = "",
                initialContent = "",
                onDismiss = { showDialog = false },
                onConfirm = { title, content ->
                    notes = notes + Note(
                        (notes.maxByOrNull { it.id }?.id ?: 0) + 1,
                        title,
                        content
                    )
                    showDialog = false
                }
            )
        }
        if (showEditDialog && editableNote != null) {
            AddEditNoteDialog(
                title = "Edit Note",
                initialTitle = editableNote!!.title,
                initialContent = editableNote!!.content,
                onDismiss = {
                    showEditDialog = false
                    editableNote = null
                },
                onConfirm = { title, content ->
                    notes = notes.map {
                        if (it.id == editableNote!!.id) it.copy(title = title, content = content)
                        else it
                    }
                    showEditDialog = false
                    editableNote = null
                }
            )
        }
        if (showDetailsDialog && noteForDetails != null) {
            NoteDetailsDialog(
                note = noteForDetails!!,
                onClose = {
                    showDetailsDialog = false
                    noteForDetails = null
                }
            )
        }
    }
}

/**
 * A single row for a note in the list, offering inline delete and edit buttons, and triggers details view on click/tap.
 */
@Composable
fun NoteRow(note: Note, onDelete: () -> Unit, onEdit: () -> Unit, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.rowBackground)
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = note.title,
                    color = AppColors.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = note.content.take(48) + if (note.content.length > 48) "..." else "",
                    color = AppColors.secondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light
                )
            }
            // Edit and Delete icons inline
            IconButton(
                onClick = {
                    onEdit()
                },
                modifier = Modifier.padding(start = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit Note",
                    tint = AppColors.accent
                )
            }
            IconButton(
                onClick = {
                    onDelete()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Note",
                    tint = AppColors.primary
                )
            }
        }
    }
}

/**
 * Dialog to add or edit a note, with modern minimalistic styling and safe input.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteDialog(
    title: String,
    initialTitle: String,
    initialContent: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var t by remember { mutableStateOf(TextFieldValue(initialTitle)) }
    var c by remember { mutableStateOf(TextFieldValue(initialContent)) }
    val focusManager = LocalFocusManager.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (t.text.trim().isNotEmpty()) {
                        onConfirm(t.text.trim(), c.text.trim())
                    }
                },
                enabled = t.text.trim().isNotEmpty(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AppColors.primary
                )
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppColors.secondary)
            }
        },
        title = {
            Text(title, color = AppColors.primary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = t,
                    onValueChange = { t = it },
                    label = { Text("Title") },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = AppColors.primary,
                        unfocusedBorderColor = AppColors.secondary,
                        cursorColor = AppColors.primary
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = c,
                    onValueChange = { c = it },
                    label = { Text("Content") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = AppColors.primary,
                        unfocusedBorderColor = AppColors.secondary,
                        cursorColor = AppColors.primary
                    ),
                    maxLines = 6,
                    modifier = Modifier.height(110.dp)
                )
            }
        },
        containerColor = AppColors.dialogBackground,
        shape = RoundedCornerShape(18.dp)
    )
}

/**
 * Detail dialog view for a note.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailsDialog(note: Note, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = onClose) {
                Text("Close", fontWeight = FontWeight.Medium, color = AppColors.primary)
            }
        },
        title = {
            Text(note.title, color = AppColors.primary, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(note.content, color = Color.Black, fontSize = 16.sp)
        },
        containerColor = AppColors.dialogBackground,
        shape = RoundedCornerShape(18.dp)
    )
}

/**
 * AppColors object to keep all color references easy to update and consistent with the custom palette.
 */
object AppColors {
    val primary = Color(0xFF4A90E2)
    val secondary = Color(0xFF50E3C2)
    val accent = Color(0xFFF5A623)
    val rowBackground = Color(0xFFF7F9FA)
    val dialogBackground = Color.White
}

/**
 * PUBLIC_INTERFACE
 * Light minimalistic theme using the requested colors.
 */
@Composable
fun NotesAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AppColors.primary,
            secondary = AppColors.secondary,
            background = Color.White,
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = Color.Black,
            onSurface = Color.Black,
            tertiary = AppColors.accent
        ),
        typography = Typography(),
        content = content
    )
}
