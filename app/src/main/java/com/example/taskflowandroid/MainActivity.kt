package com.example.taskflowandroid

import android.os.Bundle
import android.content.Context
import android.app.DatePickerDialog
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.taskflowandroid.ui.theme.TaskFlowAndroidTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

data class Task(
    val id: Long,
    val title: String,
    val category: String,
    val completed: Boolean = false
)

data class Shift(
    val date: String,
    val startTime: String,
    val endTime: String
)
data class StudyRecord(
    val studyMinutes: Int,
    val restMinutes: Int,
    val category: String,
    val monthKey: String,
    val dayOfMonth: Int
)

data class FinanceEntry(
    val type: String,
    val amount: Double,
    val monthKey: String,
    val dayOfMonth: Int,
    val paymentMethod: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskFlowAndroidTheme {
                MeudiaMedApp()
            }
        }
    }
}

@Composable
fun MeudiaMedApp() {
    val context = LocalContext.current
    val preferences = remember {
        context.getSharedPreferences("meudiamed_preferences", Context.MODE_PRIVATE)
    }
    var selectedTab by rememberSaveable { mutableStateOf("Hoje") }
    var newTask by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Estudos") }
    var categoryMenuOpen by remember { mutableStateOf(false) }
    var showTaskForm by rememberSaveable { mutableStateOf(false) }
    var studyInput by rememberSaveable { mutableStateOf("") }
    var restInput by rememberSaveable { mutableStateOf("") }
    var studyCategory by rememberSaveable { mutableStateOf("Residência") }
    var userEmail by rememberSaveable {
        mutableStateOf(preferences.getString("user_email", "") ?: "")
    }
    var professionalRole by rememberSaveable {
        mutableStateOf(preferences.getString("professional_role", "") ?: "")
    }
    var specialization by rememberSaveable {
        mutableStateOf(preferences.getString("specialization", "") ?: "")
    }
    var workplaceInput by rememberSaveable { mutableStateOf("") }
    var financeAmountInput by rememberSaveable { mutableStateOf("") }
    var financeType by rememberSaveable { mutableStateOf("Plantões") }
    var paymentMethod by rememberSaveable { mutableStateOf("Pix") }

    val categories = listOf("Estudos", "Trabalho", "Lazer", "Plantão anterior")
    val tasks = remember {
        mutableStateListOf(
            Task(1, "Revisar consultas do plantão anterior", "Plantão anterior"),
            Task(2, "Estudar clínica médica", "Estudos"),
            Task(3, "Organizar agenda", "Trabalho")
        )
    }
    val studyRecords = remember {
        mutableStateListOf<StudyRecord>().apply {
            addAll(loadStudyRecords(preferences))
        }
    }
    val shifts = remember { mutableStateListOf<Shift>() }
    val workplaces = remember { mutableStateListOf<String>() }
    val financeEntries = remember {
        mutableStateListOf<FinanceEntry>().apply {
            addAll(loadFinanceEntries(preferences))
        }
    }
    val currentMonthTotal = financeEntries
        .filter { it.monthKey == currentMonthKey() }
        .sumOf { it.amount }
    val previousMonthTotal = financeEntries
        .filter {
            it.monthKey == previousMonthKey() &&
                it.dayOfMonth <= currentDayOfMonth()
        }
        .sumOf { it.amount }
    val currentMonthStudyMinutes = studyRecords
        .filter { it.monthKey == currentMonthKey() }
        .sumOf { it.studyMinutes }
    val previousMonthStudyMinutes = studyRecords
        .filter {
            it.monthKey == previousMonthKey() &&
                it.dayOfMonth <= currentDayOfMonth()
        }
        .sumOf { it.studyMinutes }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "Hoje",
                    onClick = { selectedTab = "Hoje" },
                    icon = { Text("⌂") },
                    label = { Text("Hoje") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Estudos",
                    onClick = { selectedTab = "Estudos" },
                    icon = { Text("◷") },
                    label = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Estudos")
                            Text(
                                formatHoursForNavigation(currentMonthStudyMinutes),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == "Trabalho",
                    onClick = { selectedTab = "Trabalho" },
                    icon = { Text("▣") },
                    label = { Text("Trabalho") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Financeiro",
                    onClick = { selectedTab = "Financeiro" },
                    icon = { Text("R$", style = MaterialTheme.typography.labelSmall) },
                    label = { Text("Financeiro") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                "Hoje" -> TodayScreen(
                    shifts = shifts,
                    currentMonthTotal = currentMonthTotal,
                    previousMonthTotal = previousMonthTotal,
                    currentMonthStudyMinutes = currentMonthStudyMinutes,
                    previousMonthStudyMinutes = previousMonthStudyMinutes,
                    professionalRole = professionalRole,
                    specialization = specialization,
                    onSettingsClick = { selectedTab = "Perfil" }
                )
                "Estudos" -> StudyScreen(
                    studyInput = studyInput,
                    restInput = restInput,
                    selectedCategory = studyCategory,
                    onStudyInputChange = { studyInput = it.filter(Char::isDigit) },
                    onRestInputChange = { restInput = it.filter(Char::isDigit) },
                    onCategoryChange = { studyCategory = it },
                    records = studyRecords,
                    onDeleteRecord = { record ->
                        studyRecords.remove(record)
                        saveStudyRecords(preferences, studyRecords)
                    },
                    onClearCurrentMonth = {
                        studyRecords.removeAll {
                            it.category == studyCategory && it.monthKey == currentMonthKey()
                        }
                        saveStudyRecords(preferences, studyRecords)
                    },
                    onRegister = {
                        val studyMinutes = studyInput.toIntOrNull() ?: 0
                        val restMinutes = restInput.toIntOrNull() ?: 0
                        if (studyMinutes > 0 || restMinutes > 0) {
                            studyRecords.add(
                                StudyRecord(
                                    studyMinutes,
                                    restMinutes,
                                    studyCategory,
                                    currentMonthKey(),
                                    currentDayOfMonth()
                                )
                            )
                            saveStudyRecords(preferences, studyRecords)
                            studyInput = ""
                            restInput = ""
                        }
                    }
                )
                "Financeiro" -> FinancialScreen(
                    amountInput = financeAmountInput,
                    selectedType = financeType,
                    selectedPaymentMethod = paymentMethod,
                    entries = financeEntries,
                    onAmountChange = { financeAmountInput = it },
                    onTypeChange = { financeType = it },
                    onPaymentMethodChange = { paymentMethod = it },
                    onDeleteEntry = { entry ->
                        financeEntries.remove(entry)
                        saveFinanceEntries(preferences, financeEntries)
                    },
                    onClearCurrentMonth = {
                        financeEntries.removeAll { it.monthKey == currentMonthKey() }
                        saveFinanceEntries(preferences, financeEntries)
                    },
                    onAddEntry = {
                        val amount = financeAmountInput.replace(",", ".").toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            financeEntries.add(
                                FinanceEntry(
                                    type = financeType,
                                    amount = amount,
                                    monthKey = currentMonthKey(),
                                    dayOfMonth = currentDayOfMonth(),
                                    paymentMethod = paymentMethod
                                )
                            )
                            saveFinanceEntries(preferences, financeEntries)
                            financeAmountInput = ""
                            true
                        } else {
                            false
                        }
                    }
                )
                "Trabalho" -> WorkAgenda(shifts = shifts)
                "Perfil" -> SettingsScreen(
                    onBack = { selectedTab = "Hoje" },
                    email = userEmail,
                    onEmailChange = { userEmail = it },
                    onSaveEmail = {
                        preferences.edit().putString("user_email", userEmail.trim()).apply()
                    },
                    professionalRole = professionalRole,
                    onProfessionalRoleChange = { professionalRole = it },
                    specialization = specialization,
                    onSpecializationChange = { specialization = it },
                    onSaveProfessionalInfo = {
                        preferences.edit()
                            .putString("professional_role", professionalRole.trim())
                            .putString("specialization", specialization.trim())
                            .apply()
                    },
                    workplaceInput = workplaceInput,
                    onWorkplaceInputChange = { workplaceInput = it },
                    workplaces = workplaces,
                    onAddWorkplace = {
                        if (workplaceInput.isNotBlank()) {
                            workplaces.add(workplaceInput.trim())
                            workplaceInput = ""
                        }
                    },
                    onRemoveWorkplace = { workplace -> workplaces.remove(workplace) },
                    completedTasks = tasks.count { it.completed },
                    pendingTasks = tasks.count { !it.completed }
                )
            }
        }
    }
}

@Composable
fun FinancialScreen(
    amountInput: String,
    selectedType: String,
    selectedPaymentMethod: String,
    entries: List<FinanceEntry>,
    onAmountChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onDeleteEntry: (FinanceEntry) -> Unit,
    onClearCurrentMonth: () -> Unit,
    onAddEntry: () -> Boolean
) {
    var registrationMessage by rememberSaveable { mutableStateOf("") }
    var paymentFilter by rememberSaveable { mutableStateOf("Todos") }
    var showClearConfirmation by rememberSaveable { mutableStateOf(false) }
    val currentMonthEntries = entries.filter { it.monthKey == currentMonthKey() }
    val filteredEntries = if (paymentFilter == "Todos") {
        currentMonthEntries
    } else {
        currentMonthEntries.filter { it.paymentMethod == paymentFilter }
    }
    val shiftTotal = currentMonthEntries.filter { it.type == "Plantões" }.sumOf { it.amount }
    val fixedTotal = currentMonthEntries.filter { it.type == "Fixo" }.sumOf { it.amount }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Zerar lançamentos do mês?") },
            text = { Text("Os lançamentos deste mês serão apagados. O histórico dos meses anteriores será mantido.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearCurrentMonth()
                    showClearConfirmation = false
                }) {
                    Text("Zerar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
    item {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
    Text("Financeiro", style = MaterialTheme.typography.displaySmall)
    Text("Cadastre os ganhos dos plantões e da renda fixa.")

    OutlinedTextField(
        value = amountInput,
        onValueChange = onAmountChange,
        label = { Text("Valor recebido") },
        placeholder = { Text("Ex.: 1200,00") },
        prefix = { Text("R$ ") },
        modifier = Modifier.fillMaxWidth()
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selectedType == "Plantões",
            onClick = { onTypeChange("Plantões") },
            label = { Text("Plantões") }
        )
        FilterChip(
            selected = selectedType == "Fixo",
            onClick = { onTypeChange("Fixo") },
            label = { Text("Fixo") }
        )
    }
    Text("Forma de pagamento", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selectedPaymentMethod == "Pix",
            onClick = { onPaymentMethodChange("Pix") },
            label = { Text("Pix") }
        )
        FilterChip(
            selected = selectedPaymentMethod == "Transferência",
            onClick = { onPaymentMethodChange("Transferência") },
            label = { Text("Transferência") }
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selectedPaymentMethod == "Cartão",
            onClick = { onPaymentMethodChange("Cartão") },
            label = { Text("Cartão") }
        )
        FilterChip(
            selected = selectedPaymentMethod == "Dinheiro",
            onClick = { onPaymentMethodChange("Dinheiro") },
            label = { Text("Dinheiro") }
        )
    }
    Button(
        onClick = {
            registrationMessage = if (onAddEntry()) {
                "Valor cadastrado com sucesso."
            } else {
                "Digite um valor maior que zero para cadastrar."
            }
        }
    ) {
        Text("Cadastrar valor")
    }
    if (registrationMessage.isNotBlank()) {
        Text(registrationMessage)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Resumo financeiro", style = MaterialTheme.typography.titleLarge)
            Text("Plantões: " + formatCurrency(shiftTotal))
            Text("Fixo: " + formatCurrency(fixedTotal))
            Text("Total: " + formatCurrency(shiftTotal + fixedTotal))
        }
    }

    Text("Filtrar lançamentos", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = paymentFilter == "Todos",
            onClick = { paymentFilter = "Todos" },
            label = { Text("Todos") }
        )
        FilterChip(
            selected = paymentFilter == "Pix",
            onClick = { paymentFilter = "Pix" },
            label = { Text("Pix") }
        )
        FilterChip(
            selected = paymentFilter == "Cartão",
            onClick = { paymentFilter = "Cartão" },
            label = { Text("Cartão") }
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = paymentFilter == "Transferência",
            onClick = { paymentFilter = "Transferência" },
            label = { Text("Transferência") }
        )
        FilterChip(
            selected = paymentFilter == "Dinheiro",
            onClick = { paymentFilter = "Dinheiro" },
            label = { Text("Dinheiro") }
        )
    }
    TextButton(onClick = { showClearConfirmation = true }) {
        Text("Zerar lançamentos deste mês")
    }

    if (filteredEntries.isEmpty()) {
        Text("Nenhum lançamento encontrado para este filtro.")
    } else {
        Text("Lançamentos", style = MaterialTheme.typography.titleLarge)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filteredEntries.forEach { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            entry.type + " • " + entry.paymentMethod,
                            modifier = Modifier.weight(1f)
                        )
                        Text(formatCurrency(entry.amount))
                        IconButton(
                            onClick = { onDeleteEntry(entry) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Text("×", style = MaterialTheme.typography.titleLarge)
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

@Composable
fun TodayScreen(
    shifts: List<Shift>,
    currentMonthTotal: Double,
    previousMonthTotal: Double,
    currentMonthStudyMinutes: Int,
    previousMonthStudyMinutes: Int,
    professionalRole: String,
    specialization: String,
    onSettingsClick: () -> Unit
) {
    val dateText = remember {
        SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("pt", "BR"))
            .format(Date())
            .replaceFirstChar { it.titlecase(Locale("pt", "BR")) }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("MeudiaMED", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onSettingsClick) {
            Text("⚙", style = MaterialTheme.typography.headlineMedium)
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.meudiamed_logo),
            contentDescription = "Logo do MeudiaMED",
            modifier = Modifier.size(52.dp)
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text("Olá, Fernando", style = MaterialTheme.typography.titleMedium)
            if (professionalRole.isNotBlank() || specialization.isNotBlank()) {
                Text(
                    listOf(professionalRole, specialization)
                        .filter { it.isNotBlank() }
                        .joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(dateText, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text("Ganhos do mês", style = MaterialTheme.typography.bodySmall)
            Text(formatCurrency(currentMonthTotal), style = MaterialTheme.typography.titleMedium)
            val difference = currentMonthTotal - previousMonthTotal
            val comparisonText = when {
                difference > 0 -> "▲ Acima do mês anterior: " + formatCurrency(difference)
                difference < 0 -> "▼ Abaixo do mês anterior: " + formatCurrency(-difference)
                else -> "— Igual ao mês anterior"
            }
            val comparisonColor = when {
                difference > 0 -> Color(0xFF2E7D32)
                difference < 0 -> Color(0xFFC62828)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Text(
                comparisonText,
                style = MaterialTheme.typography.bodySmall,
                color = comparisonColor
            )
        }
    }

    val studyDifference = currentMonthStudyMinutes - previousMonthStudyMinutes
    val studyComparisonText = when {
        studyDifference > 0 -> "▲ Acima do mês anterior: " + formatMinutes(studyDifference)
        studyDifference < 0 -> "▼ Abaixo do mês anterior: " + formatMinutes(-studyDifference)
        else -> "— Igual ao mês anterior"
    }
    val studyComparisonColor = when {
        studyDifference > 0 -> Color(0xFF2E7D32)
        studyDifference < 0 -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Tempo estudado no mês", style = MaterialTheme.typography.titleMedium)
            Text(
                formatMinutes(currentMonthStudyMinutes),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                studyComparisonText,
                style = MaterialTheme.typography.bodySmall,
                color = studyComparisonColor
            )
        }
    }

    val tomorrowShifts = shifts.filter { it.date == tomorrowDate() }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Avisos importantes", style = MaterialTheme.typography.titleMedium)
            if (tomorrowShifts.isEmpty()) {
                Text("Nenhum plantão agendado para amanhã.")
            } else {
                tomorrowShifts.forEach { shift ->
                    Text("Plantão amanhã: " + shift.startTime + " às " + shift.endTime)
                }
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Para hoje", style = MaterialTheme.typography.titleMedium)
            Text(dailyOrganizationMessage())
        }
    }

}

@Composable
fun WorkAgenda(shifts: MutableList<Shift>) {
    var shiftDate by rememberSaveable { mutableStateOf("") }
    var startTime by rememberSaveable { mutableStateOf("") }
    var endTime by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                shiftDate = String.format(
                    Locale("pt", "BR"),
                    "%02d/%02d/%04d",
                    dayOfMonth,
                    month + 1,
                    year
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Text("Agenda de plantões", style = MaterialTheme.typography.titleLarge)

    Text("Data do plantão", style = MaterialTheme.typography.titleMedium)
    OutlinedButton(
        onClick = { datePickerDialog.show() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (shiftDate.isBlank()) "Selecionar data" else shiftDate)
    }
    OutlinedTextField(
        value = startTime,
        onValueChange = { startTime = it },
        label = { Text("Horário de início") },
        placeholder = { Text("Ex.: 07:00") },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = endTime,
        onValueChange = { endTime = it },
        label = { Text("Horário de término") },
        placeholder = { Text("Ex.: 19:00") },
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = {
            if (shiftDate.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                shifts.add(Shift(shiftDate, startTime, endTime))
                shiftDate = ""
                startTime = ""
                endTime = ""
            }
        }
    ) {
        Text("Salvar plantão")
    }

    if (shifts.isEmpty()) {
        Text("Nenhum plantão cadastrado.")
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            shifts.forEach { shift ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Plantão — " + shift.date)
                        Text(shift.startTime + " às " + shift.endTime)
                    }
                }
            }
        }
    }
}
@Composable
fun StudyScreen(
    studyInput: String,
    restInput: String,
    selectedCategory: String,
    onStudyInputChange: (String) -> Unit,
    onRestInputChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    records: List<StudyRecord>,
    onDeleteRecord: (StudyRecord) -> Unit,
    onClearCurrentMonth: () -> Unit,
    onRegister: () -> Unit
) {
    var showClearConfirmation by rememberSaveable { mutableStateOf(false) }
    val selectedRecords = records.filter {
        it.category == selectedCategory && it.monthKey == currentMonthKey()
    }
    val studyTotal = selectedRecords.sumOf { it.studyMinutes }
    val restTotal = selectedRecords.sumOf { it.restMinutes }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Zerar estudos deste mês?") },
            text = { Text("Os registros de " + selectedCategory + " deste mês serão apagados. Os meses anteriores serão mantidos.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearCurrentMonth()
                    showClearConfirmation = false
                }) {
                    Text("Zerar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Text("Estudos", style = MaterialTheme.typography.displaySmall)
    Text("Registre os minutos estudados e os minutos de descanso.")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selectedCategory == "Residência",
            onClick = { onCategoryChange("Residência") },
            label = { Text("Residência") }
        )
        FilterChip(
            selected = selectedCategory == "Provas",
            onClick = { onCategoryChange("Provas") },
            label = { Text("Provas") }
        )
    }

    OutlinedTextField(
        value = studyInput,
        onValueChange = onStudyInputChange,
        label = { Text("Estudo (minutos)") },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = restInput,
        onValueChange = onRestInputChange,
        label = { Text("Descanso (minutos)") },
        modifier = Modifier.fillMaxWidth()
    )
    Button(onClick = onRegister) {
        Text("Registrar tempo")
    }

    Text("Resumo: " + selectedCategory, style = MaterialTheme.typography.titleLarge)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Tempo de estudo: " + formatMinutes(studyTotal))
            Text("Tempo de descanso: " + formatMinutes(restTotal))
        }
    }
    TextButton(onClick = { showClearConfirmation = true }) {
        Text("Zerar registros deste mês")
    }

    if (selectedRecords.isNotEmpty()) {
        Text("Registros deste mês", style = MaterialTheme.typography.titleLarge)
        selectedRecords.forEach { record ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        formatMinutes(record.studyMinutes) + " de estudo • " +
                            formatMinutes(record.restMinutes) + " de descanso",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onDeleteRecord(record) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text("×", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    onSaveEmail: () -> Unit,
    professionalRole: String,
    onProfessionalRoleChange: (String) -> Unit,
    specialization: String,
    onSpecializationChange: (String) -> Unit,
    onSaveProfessionalInfo: () -> Unit,
    workplaceInput: String,
    onWorkplaceInputChange: (String) -> Unit,
    workplaces: List<String>,
    onAddWorkplace: () -> Unit,
    onRemoveWorkplace: (String) -> Unit,
    completedTasks: Int,
    pendingTasks: Int
) {
    var emailSaved by rememberSaveable { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Configurações", style = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onBack) {
            Text("Voltar")
        }
    }
    Text("Seus dados", style = MaterialTheme.typography.titleLarge)

    OutlinedTextField(
        value = email,
        onValueChange = {
            onEmailChange(it)
            emailSaved = false
        },
        label = { Text("E-mail") },
        placeholder = { Text("exemplo@email.com") },
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = {
            onSaveEmail()
            emailSaved = true
        },
        enabled = email.contains("@") && email.contains(".")
    ) {
        Text("Salvar e-mail")
    }
    if (emailSaved) {
        Text("E-mail salvo neste aparelho.")
    }

    Text("Atuação profissional", style = MaterialTheme.typography.titleLarge)
    OutlinedTextField(
        value = professionalRole,
        onValueChange = onProfessionalRoleChange,
        label = { Text("Cargo") },
        placeholder = { Text("Ex.: Médica residente") },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = specialization,
        onValueChange = onSpecializationChange,
        label = { Text("Especialização") },
        placeholder = { Text("Ex.: Clínica Médica") },
        modifier = Modifier.fillMaxWidth()
    )
    Button(onClick = onSaveProfessionalInfo) {
        Text("Salvar atuação")
    }

    Text("Locais de trabalho", style = MaterialTheme.typography.titleLarge)
    OutlinedTextField(
        value = workplaceInput,
        onValueChange = onWorkplaceInputChange,
        label = { Text("Novo local") },
        placeholder = { Text("Ex.: Hospital Central") },
        modifier = Modifier.fillMaxWidth()
    )
    Button(onClick = onAddWorkplace) {
        Text("Adicionar local")
    }

    if (workplaces.isEmpty()) {
        Text("Nenhum local de trabalho cadastrado.")
    } else {
        workplaces.forEach { workplace ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(workplace, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onRemoveWorkplace(workplace) }) {
                        Text("Remover")
                    }
                }
            }
        }
    }

    Text("Resumo", style = MaterialTheme.typography.titleLarge)
    Text("Concluídas: " + completedTasks)
    Text("Pendentes: " + pendingTasks)
}

@Composable
fun TaskList(tasks: List<Task>, allTasks: MutableList<Task>) {
    if (tasks.isEmpty()) {
        Text("Nenhuma atividade pendente.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tasks, key = { it.id }) { task ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.completed,
                        onCheckedChange = { checked ->
                            val index = allTasks.indexOfFirst { it.id == task.id }
                            allTasks[index] = task.copy(completed = checked)
                        }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(task.title, style = MaterialTheme.typography.bodyLarge)
                        Text(task.category, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return hours.toString() + "h " + remainingMinutes.toString() + "min"
}

fun formatHoursForNavigation(minutes: Int): String {
    return (minutes / 60).toString() + "h"
}

fun formatCurrency(amount: Double): String {
    return String.format(Locale("pt", "BR"), "R$ %,.2f", amount)
}

fun currentMonthKey(): String {
    return SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
}

fun currentDayOfMonth(): Int {
    return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
}

fun previousMonthKey(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, -1)
    return SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)
}

fun tomorrowDate(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    return SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(calendar.time)
}

fun dailyOrganizationMessage(): String {
    val messages = listOf(
        "Hoje, priorize 2h de revisão.",
        "Comece pelo assunto mais importante do dia.",
        "Reserve um intervalo de descanso entre os blocos de estudo.",
        "Uma pequena revisão hoje facilita muito o plantão de amanhã.",
        "Defina uma meta possível e conclua uma etapa por vez.",
        "Organize primeiro o tempo, depois as tarefas.",
        "Cuide do descanso: ele também faz parte do rendimento."
    )
    val dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % messages.size
    return messages[dayIndex]
}

fun saveFinanceEntries(
    preferences: android.content.SharedPreferences,
    entries: List<FinanceEntry>
) {
    val jsonArray = JSONArray()
    entries.forEach { entry ->
        jsonArray.put(
            JSONObject().apply {
                put("type", entry.type)
                put("amount", entry.amount)
                put("monthKey", entry.monthKey)
                put("dayOfMonth", entry.dayOfMonth)
                put("paymentMethod", entry.paymentMethod)
            }
        )
    }
    preferences.edit().putString("finance_entries", jsonArray.toString()).apply()
}

fun loadFinanceEntries(
    preferences: android.content.SharedPreferences
): List<FinanceEntry> {
    val savedJson = preferences.getString("finance_entries", null) ?: return emptyList()
    return runCatching {
        val jsonArray = JSONArray(savedJson)
        List(jsonArray.length()) { index ->
            val item = jsonArray.getJSONObject(index)
            FinanceEntry(
                type = item.getString("type"),
                amount = item.getDouble("amount"),
                monthKey = item.getString("monthKey"),
                dayOfMonth = item.getInt("dayOfMonth"),
                paymentMethod = item.optString("paymentMethod", "Não informado")
            )
        }
    }.getOrDefault(emptyList())
}

fun saveStudyRecords(
    preferences: android.content.SharedPreferences,
    records: List<StudyRecord>
) {
    val jsonArray = JSONArray()
    records.forEach { record ->
        jsonArray.put(
            JSONObject().apply {
                put("studyMinutes", record.studyMinutes)
                put("restMinutes", record.restMinutes)
                put("category", record.category)
                put("monthKey", record.monthKey)
                put("dayOfMonth", record.dayOfMonth)
            }
        )
    }
    preferences.edit().putString("study_records", jsonArray.toString()).apply()
}

fun loadStudyRecords(
    preferences: android.content.SharedPreferences
): List<StudyRecord> {
    val savedJson = preferences.getString("study_records", null) ?: return emptyList()
    return runCatching {
        val jsonArray = JSONArray(savedJson)
        List(jsonArray.length()) { index ->
            val item = jsonArray.getJSONObject(index)
            StudyRecord(
                studyMinutes = item.getInt("studyMinutes"),
                restMinutes = item.getInt("restMinutes"),
                category = item.getString("category"),
                monthKey = item.getString("monthKey"),
                dayOfMonth = item.getInt("dayOfMonth")
            )
        }
    }.getOrDefault(emptyList())
}
