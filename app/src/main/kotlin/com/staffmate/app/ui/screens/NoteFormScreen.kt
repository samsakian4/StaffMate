package com.staffmate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.staffmate.app.StaffMateApp
import com.staffmate.app.data.*
import com.staffmate.app.util.DateUtil
import kotlinx.coroutines.launch

private val NEGATIVE_STATUSES = listOf("تکرار نشده", "تکرار شده", "برطرف شده")
private val SEVERITIES = listOf("کم", "متوسط", "زیاد", "بسیار زیاد")
private val PERSONALITY_TYPES = listOf("مثبت", "منفی")

@Composable
fun NoteFormScreen(navController: NavHostController, type: String, employeeId: Long, noteId: Long?) {
    val context = LocalContext.current
    val repository = (context.applicationContext as StaffMateApp).repository
    val scope = rememberCoroutineScope()
    val isNew = noteId == null

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(DateUtil.format(DateUtil.today())) }
    var importance by remember { mutableStateOf("متوسط") }
    var status by remember { mutableStateOf(NEGATIVE_STATUSES[0]) }
    var personalityType by remember { mutableStateOf(PERSONALITY_TYPES[0]) }
    var violationType by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(SEVERITIES[0]) }
    var actionTaken by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }

    var importanceOptions by remember { mutableStateOf(listOf("کم", "متوسط", "زیاد")) }
    var violationOptions by remember { mutableStateOf(listOf<String>()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(isNew) }

    LaunchedEffect(Unit) {
        val impList = repository.getSetting("importance_list", "کم,متوسط,زیاد")
        importanceOptions = impList.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val violList = repository.getSetting("violation_types_list", "غیبت,تاخیر,عدم رعایت ایمنی,نافرمانی,سایر")
        violationOptions = violList.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (violationType.isEmpty() && violationOptions.isNotEmpty()) violationType = violationOptions.first()

        if (!isNew && noteId != null) {
            when (type) {
                "positive" -> repository.positives.getById(noteId)?.let { n ->
                    title = n.title; description = n.description; importance = n.importance
                    dateText = DateUtil.format(n.date)
                }
                "negative" -> repository.negatives.getById(noteId)?.let { n ->
                    title = n.title; description = n.description; importance = n.importance
                    status = n.status; dateText = DateUtil.format(n.date)
                }
                "personality" -> repository.personalities.getById(noteId)?.let { n ->
                    title = n.title; description = n.description; personalityType = n.type
                    dateText = DateUtil.format(n.date)
                }
                "disciplinary" -> repository.disciplinary.getById(noteId)?.let { n ->
                    violationType = n.violationType; description = n.description; severity = n.severity
                    actionTaken = n.actionTaken; additionalNotes = n.additionalNotes
                    dateText = DateUtil.format(n.date)
                }
            }
        }
        loaded = true
    }

    val titleLabel = when (type) {
        "positive" -> "افزودن نکته مثبت"
        "negative" -> "افزودن نکته منفی"
        "personality" -> "افزودن ویژگی شخصیتی"
        "disciplinary" -> "افزودن مورد انضباطی"
        else -> "افزودن"
    }

    Scaffold(topBar = { TopAppBar(title = { Text(if (isNew) titleLabel else "ویرایش مورد") }) }) { padding ->
        if (!loaded) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(dateText, { dateText = it }, label = { Text("تاریخ (yyyy/MM/dd) *") }, modifier = Modifier.fillMaxWidth())

            when (type) {
                "positive" -> {
                    OutlinedTextField(title, { title = it }, label = { Text("عنوان *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(description, { description = it }, label = { Text("توضیح") }, modifier = Modifier.fillMaxWidth())
                    DropdownField("میزان اهمیت", importanceOptions, importance) { importance = it }
                }
                "negative" -> {
                    OutlinedTextField(title, { title = it }, label = { Text("عنوان *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(description, { description = it }, label = { Text("توضیح") }, modifier = Modifier.fillMaxWidth())
                    DropdownField("میزان اهمیت", importanceOptions, importance) { importance = it }
                    DropdownField("وضعیت", NEGATIVE_STATUSES, status) { status = it }
                }
                "personality" -> {
                    DropdownField("نوع ویژگی", PERSONALITY_TYPES, personalityType) { personalityType = it }
                    OutlinedTextField(title, { title = it }, label = { Text("عنوان ویژگی *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(description, { description = it }, label = { Text("توضیح") }, modifier = Modifier.fillMaxWidth())
                }
                "disciplinary" -> {
                    DropdownField("نوع تخلف", violationOptions, violationType) { violationType = it }
                    OutlinedTextField(description, { description = it }, label = { Text("توضیح") }, modifier = Modifier.fillMaxWidth())
                    DropdownField("شدت", SEVERITIES, severity) { severity = it }
                    OutlinedTextField(actionTaken, { actionTaken = it }, label = { Text("اقدام انجام‌شده") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(additionalNotes, { additionalNotes = it }, label = { Text("توضیحات تکمیلی") }, modifier = Modifier.fillMaxWidth())
                }
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    val dateMillis = DateUtil.parse(dateText)
                    if (type != "disciplinary" && title.isBlank()) {
                        error = "عنوان الزامی است."
                        return@Button
                    }
                    if (type == "disciplinary" && violationType.isBlank()) {
                        error = "نوع تخلف الزامی است."
                        return@Button
                    }
                    scope.launch {
                        when (type) {
                            "positive" -> if (isNew) {
                                repository.positives.insert(PositiveNote(employeeId = employeeId, title = title.trim(), description = description.trim(), importance = importance, date = dateMillis))
                            } else {
                                repository.positives.update(PositiveNote(id = noteId!!, employeeId = employeeId, title = title.trim(), description = description.trim(), importance = importance, date = dateMillis))
                            }
                            "negative" -> if (isNew) {
                                repository.negatives.insert(NegativeNote(employeeId = employeeId, title = title.trim(), description = description.trim(), importance = importance, status = status, date = dateMillis))
                            } else {
                                repository.negatives.update(NegativeNote(id = noteId!!, employeeId = employeeId, title = title.trim(), description = description.trim(), importance = importance, status = status, date = dateMillis))
                            }
                            "personality" -> if (isNew) {
                                repository.personalities.insert(PersonalityNote(employeeId = employeeId, type = personalityType, title = title.trim(), description = description.trim(), date = dateMillis))
                            } else {
                                repository.personalities.update(PersonalityNote(id = noteId!!, employeeId = employeeId, type = personalityType, title = title.trim(), description = description.trim(), date = dateMillis))
                            }
                            "disciplinary" -> if (isNew) {
                                repository.disciplinary.insert(DisciplinaryRecord(employeeId = employeeId, date = dateMillis, violationType = violationType, description = description.trim(), severity = severity, actionTaken = actionTaken.trim(), additionalNotes = additionalNotes.trim()))
                            } else {
                                repository.disciplinary.update(DisciplinaryRecord(id = noteId!!, employeeId = employeeId, date = dateMillis, violationType = violationType, description = description.trim(), severity = severity, actionTaken = actionTaken.trim(), additionalNotes = additionalNotes.trim()))
                            }
                        }
                        navController.popBackStack()
                        Toast.makeText(context, if (isNew) "با موفقیت ثبت شد." else "تغییرات ذخیره شد.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("ذخیره") }

            if (!isNew && noteId != null) {
                var confirmDelete by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("حذف") }

                if (confirmDelete) {
                    AlertDialog(
                        onDismissRequest = { confirmDelete = false },
                        title = { Text("حذف مورد") },
                        text = { Text("آیا از حذف این مورد مطمئن هستید؟") },
                        confirmButton = {
                            TextButton(onClick = {
                                scope.launch {
                                    when (type) {
                                        "positive" -> repository.positives.getById(noteId)?.let { repository.positives.delete(it) }
                                        "negative" -> repository.negatives.getById(noteId)?.let { repository.negatives.delete(it) }
                                        "personality" -> repository.personalities.getById(noteId)?.let { repository.personalities.delete(it) }
                                        "disciplinary" -> repository.disciplinary.getById(noteId)?.let { repository.disciplinary.delete(it) }
                                    }
                                    navController.popBackStack()
                                    Toast.makeText(context, "مورد حذف شد.", Toast.LENGTH_SHORT).show()
                                }
                            }) { Text("حذف") }
                        },
                        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("انصراف") } }
                    )
                }
            }
        }
    }
}

@Composable
private fun DropdownField(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = { onSelect(opt); expanded = false })
            }
        }
    }
}
