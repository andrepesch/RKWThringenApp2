package com.example.rkwthringenapp.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rkwthringenapp.R
import com.example.rkwthringenapp.data.*
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigInteger
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar

class RkwFormViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("RkwFormPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _uiState = MutableStateFlow(RkwFormData())
    val uiState: StateFlow<RkwFormData> = _uiState.asStateFlow()

    private val _hasSavedData = MutableStateFlow(false)
    val hasSavedData: StateFlow<Boolean> = _hasSavedData.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var fullWzList: List<String> = emptyList()
    val wzList: List<String> get() = fullWzList

    private val _isPlzError = MutableStateFlow(false)
    val isPlzError: StateFlow<Boolean> = _isPlzError.asStateFlow()

    private val _isDateError = MutableStateFlow(false)
    val isDateError: StateFlow<Boolean> = _isDateError.asStateFlow()

    private val _taxIdErrors = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val taxIdErrors: StateFlow<Map<Int, Boolean>> = _taxIdErrors.asStateFlow()

    private val _beneficialOwnerDateErrors = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val beneficialOwnerDateErrors: StateFlow<Map<Int, Boolean>> = _beneficialOwnerDateErrors.asStateFlow()

    private val _isMainContactEmailError = MutableStateFlow(false)
    val isMainContactEmailError: StateFlow<Boolean> = _isMainContactEmailError.asStateFlow()

    private val _consultantEmailErrors = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val consultantEmailErrors: StateFlow<Map<Int, Boolean>> = _consultantEmailErrors.asStateFlow()

    private val _isIbanError = MutableStateFlow(false)
    val isIbanError: StateFlow<Boolean> = _isIbanError.asStateFlow()

    private val _isDailyRateError = MutableStateFlow(false)
    val isDailyRateError: StateFlow<Boolean> = _isDailyRateError.asStateFlow()

    private val _isEndDateError = MutableStateFlow(false)
    val isEndDateError: StateFlow<Boolean> = _isEndDateError.asStateFlow()

    val availableYears: List<String>

    init {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        availableYears = (0..3).map { (currentYear - 1 - it).toString() }

        loadInitialData()
        uiState
            .onEach { saveData() }
            .launchIn(viewModelScope)
    }

    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            val inputStream = getApplication<Application>().resources.openRawResource(R.raw.wz2008)
            val reader = BufferedReader(InputStreamReader(inputStream))
            fullWzList = reader.readLines()
            loadData()
            _isLoading.value = false
        }
    }

    private fun saveData() {
        val dataAsJson = gson.toJson(uiState.value)
        sharedPreferences.edit().putString("formData", dataAsJson).apply()
        _hasSavedData.value = true
    }

    private fun loadData() {
        val dataAsJson = sharedPreferences.getString("formData", null)
        if (dataAsJson != null) {
            try {
                _uiState.value = gson.fromJson(dataAsJson, RkwFormData::class.java)
                _hasSavedData.value = true
            } catch (e: JsonSyntaxException) {
                Log.e("RkwFormViewModel", "Fehler beim Laden der alten Daten. Setze zurück.", e)
                startNewForm()
            }
        } else {
            _hasSavedData.value = false
        }
    }

    fun startNewForm() {
        _uiState.value = RkwFormData()
        sharedPreferences.edit().remove("formData").apply()
        _hasSavedData.value = false
    }

    private fun isValidGermanTaxId(taxId: String): Boolean {
        if (taxId.length != 11 || !taxId.all { it.isDigit() } || taxId.startsWith("0")) { return false }
        val digitCounts = taxId.groupingBy { it }.eachCount()
        val hasDouble = digitCounts.values.contains(2)
        val hasTriple = digitCounts.values.contains(3)
        val distinctDigits = digitCounts.keys.size
        val isValidRule1 = hasDouble && !hasTriple && distinctDigits == 9
        val isValidRule2 = hasTriple && !hasDouble && distinctDigits == 8
        if (!isValidRule1 && !isValidRule2) { return false }
        if (hasTriple) {
            for (i in 0..taxId.length - 3) {
                if (taxId[i] == taxId[i + 1] && taxId[i] == taxId[i + 2]) { return false }
            }
        }
        return true
    }

    private fun isValidIban(iban: String): Boolean {
        if (iban.length < 5) return false
        val rearrangedIban = iban.substring(4) + iban.substring(0, 4)
        val numericIban = rearrangedIban.map { char -> if (char.isLetter()) (char.code - 'A'.code + 10).toString() else char.toString() }.joinToString("")
        return try {
            BigInteger(numericIban).mod(BigInteger("97")) == BigInteger.ONE
        } catch (e: NumberFormatException) { false }
    }

    fun updateCompanyName(name: String) { _uiState.update { it.copy(companyName = name) } }
    fun updateLegalForm(legalForm: String) {
        val legalFormsRequiringBeneficialOwners = listOf("GmbH", "GmbH & Co. KG", "UG (haftungsbeschränkt)", "Kommanditgesellschaft (KG)", "Offene Handelsgesellschaft (OHG)", "Aktiengesellschaft (AG)", "Limited (Ltd.)", "Ltd. & Co. KG", "Eingetragene Genossenschaft (eG)", "KG auf Aktien (KGaA)", "Partnerschaftsgesellschaft", "Societas Europaea (SE)", "Stiftung")
        _uiState.update {
            if (legalForm in legalFormsRequiringBeneficialOwners) it.copy(legalForm = legalForm)
            else it.copy(legalForm = legalForm, beneficialOwners = emptyList())
        }
    }
    fun updateFoundationDate(date: String) {
        val digitsOnly = date.filter { it.isDigit() }
        if (digitsOnly.length <= 8) {
            _uiState.update { it.copy(foundationDate = digitsOnly) }
            if (digitsOnly.length == 8) {
                try {
                    val formatter = DateTimeFormatter.ofPattern("ddMMyyyy")
                    val parsedDate = LocalDate.parse(digitsOnly, formatter)
                    _isDateError.value = parsedDate.isAfter(LocalDate.now())
                } catch (e: DateTimeParseException) { _isDateError.value = true }
            } else { _isDateError.value = false }
        }
    }
    fun updateStreetAndNumber(street: String) { _uiState.update { it.copy(streetAndNumber = street) } }
    fun updatePostalCode(code: String) {
        if (code.length <= 5 && code.all { it.isDigit() }) {
            _uiState.update { it.copy(postalCode = code) }
            if (code.length == 5) {
                val plzNum = code.toIntOrNull()
                _isPlzError.value = plzNum == null || plzNum !in 1067..99998
            } else { _isPlzError.value = false }
        }
    }
    fun updateCity(city: String) { _uiState.update { it.copy(city = city) } }
    fun updateIsVatDeductible(isDeductible: Boolean) { _uiState.update { it.copy(isVatDeductible = isDeductible) } }
    fun updateHasWebsite(hasWebsite: Boolean) { _uiState.update { it.copy(hasWebsite = hasWebsite) } }
    fun updateWebsiteUrl(url: String) { _uiState.update { it.copy(websiteUrl = url) } }
    fun onWzSelected(selectedWz: String) { _uiState.update { it.copy(industrySector = selectedWz) } }
    fun updateMainContactName(name: String) { _uiState.update { it.copy(mainContact = it.mainContact.copy(name = name)) } }
    fun updateMainContactEmail(email: String) {
        _isMainContactEmailError.value = email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()
        _uiState.update { it.copy(mainContact = it.mainContact.copy(email = email)) }
    }
    fun updateMainContactPhone(phone: String) { _uiState.update { it.copy(mainContact = it.mainContact.copy(phone = phone)) } }
    fun addBeneficialOwner() { _uiState.update { it.copy(beneficialOwners = it.beneficialOwners + BeneficialOwner("", "")) } }
    fun removeBeneficialOwner(index: Int) { _uiState.update { val list = it.beneficialOwners.toMutableList(); list.removeAt(index); it.copy(beneficialOwners = list) } }
    fun updateBeneficialOwner(index: Int, owner: BeneficialOwner) {
        val ownerDigitsOnly = owner.copy(birthDate = owner.birthDate.filter { it.isDigit() }, taxId = owner.taxId.filter { it.isDigit() })
        if (ownerDigitsOnly.birthDate.length == 8) {
            try {
                val formatter = DateTimeFormatter.ofPattern("ddMMyyyy")
                val parsedDate = LocalDate.parse(ownerDigitsOnly.birthDate, formatter)
                _beneficialOwnerDateErrors.update { it.toMutableMap().apply { this[index] = parsedDate.isAfter(LocalDate.now()) } }
            } catch (e: DateTimeParseException) { _beneficialOwnerDateErrors.update { it.toMutableMap().apply { this[index] = true } } }
        } else { _beneficialOwnerDateErrors.update { it.toMutableMap().apply { this[index] = false } } }
        if (ownerDigitsOnly.taxId.length == 11) {
            val isValid = isValidGermanTaxId(ownerDigitsOnly.taxId)
            _taxIdErrors.update { currentErrors -> currentErrors.toMutableMap().apply { this[index] = !isValid } }
        } else { _taxIdErrors.update { currentErrors -> currentErrors.toMutableMap().apply { this[index] = false } } }
        _uiState.update {
            val newList = it.beneficialOwners.toMutableList().apply { this[index] = ownerDigitsOnly }
            it.copy(beneficialOwners = newList)
        }
    }
    fun updateBankInstitute(institute: String) { _uiState.update { it.copy(bankDetails = it.bankDetails.copy(institute = institute)) } }
    fun updateIban(iban: String) {
        val sanitizedIban = iban.uppercase().filter { it.isLetterOrDigit() }
        _uiState.update { it.copy(bankDetails = it.bankDetails.copy(iban = sanitizedIban)) }
        if (sanitizedIban.isNotEmpty()) {
            _isIbanError.value = if (sanitizedIban.length == 22) !isValidIban(sanitizedIban) else false
        } else {
            _isIbanError.value = false
        }
    }
    fun updateTaxId(taxId: String) { _uiState.update { it.copy(bankDetails = it.bankDetails.copy(taxId = taxId)) } }
    fun updateFinancialYear(isLastYear: Boolean, year: FinancialYear) {
        _uiState.update {
            val sme = it.smeClassification
            var updatedSme = sme
            if (isLastYear) {
                updatedSme = sme.copy(lastYear = year)
                if (sme.penultimateYear.year.isEmpty() && year.year.isNotEmpty()) {
                    val lastYearInt = year.year.toInt()
                    updatedSme = updatedSme.copy(penultimateYear = sme.penultimateYear.copy(year = (lastYearInt - 1).toString()))
                }
            } else {
                updatedSme = sme.copy(penultimateYear = year)
            }
            it.copy(smeClassification = updatedSme)
        }
    }
    fun updateConsultationFocus(focus: String) { _uiState.update { it.copy(consultationDetails = it.consultationDetails.copy(focus = focus)) } }
    fun updateConsultationScope(scope: String) {
        val days = scope.filter { it.isDigit() }.toIntOrNull() ?: 0
        _uiState.update { it.copy(consultationDetails = it.consultationDetails.copy(scopeInDays = days)) }
    }
    fun updateConsultationRate(rate: String) {
        val rateDigits = rate.filter { it.isDigit() }
        val rateValue = rateDigits.toLongOrNull() ?: 0L
        _isDailyRateError.value = rateValue > 0 && rateValue < 600
        _uiState.update { it.copy(consultationDetails = it.consultationDetails.copy(dailyRate = rateDigits)) }
    }
    fun updateConsultationEndDate(date: String) {
        val digitsOnly = date.filter { it.isDigit() }
        if (digitsOnly.length <= 8) {
            _uiState.update { it.copy(consultationDetails = it.consultationDetails.copy(endDate = digitsOnly)) }
            if (digitsOnly.length == 8) {
                try {
                    val formatter = DateTimeFormatter.ofPattern("ddMMyyyy")
                    val parsedDate = LocalDate.parse(digitsOnly, formatter)
                    _isEndDateError.value = parsedDate.isBefore(LocalDate.now())
                } catch (e: DateTimeParseException) { _isEndDateError.value = true }
            } else { _isEndDateError.value = false }
        }
    }
    fun updateConsultationInitialSituation(text: String) { _uiState.update { it.copy(consultationDetails = it.consultationDetails.copy(initialSituation = text)) } }
    fun updateConsultationContent(text: String) { _uiState.update { it.copy(consultationDetails = it.consultationDetails.copy(consultationContent = text)) } }
    fun updateHasChosenConsultant(hasChosen: Boolean) { _uiState.update { it.copy(hasChosenConsultant = hasChosen) } }
    fun updateConsultingFirm(firmName: String) { _uiState.update { it.copy(consultingFirm = firmName) } }
    fun addConsultant() { _uiState.update { it.copy(consultants = it.consultants + Consultant("", "", "", "")) } }
    fun removeConsultant(index: Int) { _uiState.update { val list = it.consultants.toMutableList(); list.removeAt(index); it.copy(consultants = list) } }
    fun updateConsultant(index: Int, consultant: Consultant) {
        val email = consultant.email
        val isError = email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()
        _consultantEmailErrors.update { it.toMutableMap().apply { this[index] = isError } }
        _uiState.update { val list = it.consultants.toMutableList(); list[index] = consultant; it.copy(consultants = list) }
    }
    fun addDocument(uri: Uri) { _uiState.update { val list = it.attachedDocuments.toMutableList().apply { add(uri.toString()) }; it.copy(attachedDocuments = list) } }
    fun removeDocument(uriAsString: String) { _uiState.update { val list = it.attachedDocuments.toMutableList().apply { remove(uriAsString) }; it.copy(attachedDocuments = list) } }
    fun updateAcknowledgement(isAcknowledged: Boolean) { _uiState.update { it.copy(hasAcknowledgedPublicationObligations = isAcknowledged) } }
}