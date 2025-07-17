package com.example.rkwthringenapp.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rkwthringenapp.R
import com.example.rkwthringenapp.data.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class RkwFormViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("RkwFormPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _uiState = MutableStateFlow(RkwFormData())
    val uiState: StateFlow<RkwFormData> = _uiState.asStateFlow()

    private val _hasSavedData = MutableStateFlow(false)
    val hasSavedData: StateFlow<Boolean> = _hasSavedData.asStateFlow()

    private val _wzSearchText = MutableStateFlow("")
    val wzSearchText: StateFlow<String> = _wzSearchText.asStateFlow()

    private var fullWzList: List<String> = emptyList()
    private val _filteredWzList = MutableStateFlow<List<String>>(emptyList())
    val filteredWzList: StateFlow<List<String>> = _filteredWzList.asStateFlow()

    private val _isPlzError = MutableStateFlow(false)
    val isPlzError: StateFlow<Boolean> = _isPlzError.asStateFlow()

    init {
        loadWzData()
        loadData()
        uiState
            .onEach { saveData() }
            .launchIn(viewModelScope)
    }

    private fun loadWzData() {
        viewModelScope.launch(Dispatchers.IO) {
            val inputStream = getApplication<Application>().resources.openRawResource(R.raw.wz2008)
            val reader = BufferedReader(InputStreamReader(inputStream))
            fullWzList = reader.readLines()
            _filteredWzList.value = fullWzList
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
            _uiState.value = gson.fromJson(dataAsJson, RkwFormData::class.java)
            _wzSearchText.value = _uiState.value.industrySector
            _hasSavedData.value = true
        } else {
            _hasSavedData.value = false
        }
    }

    fun startNewForm() {
        _uiState.value = RkwFormData()
        _filteredWzList.value = fullWzList
        _wzSearchText.value = ""
        sharedPreferences.edit().remove("formData").apply()
        _hasSavedData.value = false
    }

    fun onWzSearchChanged(newQuery: String) {
        _wzSearchText.value = newQuery
        _filteredWzList.value = if (newQuery.isBlank()) {
            fullWzList
        } else {
            fullWzList.filter { it.contains(newQuery, ignoreCase = true) }
        }
    }

    fun onWzSelected(selectedWz: String) {
        _uiState.update { it.copy(industrySector = selectedWz) }
        _wzSearchText.value = selectedWz
    }

    // Update-Funktionen
    fun updateCompanyName(name: String) { _uiState.update { it.copy(companyName = name) } }
    fun updateLegalForm(legalForm: String) { _uiState.update { it.copy(legalForm = legalForm) } }
    fun updateFoundationDate(date: String) {
        val digitsOnly = date.filter { it.isDigit() }
        _uiState.update { it.copy(foundationDate = digitsOnly) }
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
    fun updateMainContactName(name: String) { _uiState.update { it.copy(mainContact = it.mainContact.copy(name = name)) } }
    fun updateMainContactEmail(email: String) { _uiState.update { it.copy(mainContact = it.mainContact.copy(email = email)) } }
    fun updateMainContactPhone(phone: String) { _uiState.update { it.copy(mainContact = it.mainContact.copy(phone = phone)) } }
    fun addBeneficialOwner() { _uiState.update { it.copy(beneficialOwners = it.beneficialOwners + BeneficialOwner("", "")) } }
    fun removeBeneficialOwner(index: Int) { _uiState.update { val list = it.beneficialOwners.toMutableList(); list.removeAt(index); it.copy(beneficialOwners = list) } }
    fun updateBeneficialOwner(index: Int, owner: BeneficialOwner) { _uiState.update { val list = it.beneficialOwners.toMutableList(); list[index] = owner; it.copy(beneficialOwners = list) } }
    fun updateBankInstitute(institute: String) { _uiState.update { it.copy(bankDetails = it.bankDetails.copy(institute = institute)) } }
    fun updateIban(iban: String) { _uiState.update { it.copy(bankDetails = it.bankDetails.copy(iban = iban)) } }
    fun updateTaxId(taxId: String) { _uiState.update { it.copy(bankDetails = it.bankDetails.copy(taxId = taxId)) } }
    fun updateFinancialYear(isLastYear: Boolean, updatedYear: FinancialYear) {
        val sme = uiState.value.smeClassification
        _uiState.update { if (isLastYear) it.copy(smeClassification = sme.copy(lastYear = updatedYear)) else it.copy(smeClassification = sme.copy(penultimateYear = updatedYear)) }
    }
    fun updateConsultationFocus(focus: String) { _uiState.update { it.copy(consultationDetails = it.consultationDetails.copy(focus = focus)) } }
    fun updateConsultationScope(days: String) { _uiState.update { it.copy(consultationDetails = it.consultationDetails.copy(scopeInDays = days.toIntOrNull() ?: 0)) } }
    fun updateConsultationRate(rate: String) { _uiState.update { it.copy(consultationDetails = it.consultationDetails.copy(dailyRate = rate.toDoubleOrNull() ?: 0.0)) } }
    fun updateConsultationEndDate(date: String) { _uiState.update { it.copy(consultationDetails = it.consultationDetails.copy(endDate = date)) } }
    fun updateConsultationInitialSituation(text: String) { _uiState.update { it.copy(consultationDetails = it.consultationDetails.copy(initialSituation = text)) } }
    fun updateConsultationContent(text: String) { _uiState.update { it.copy(consultationDetails = it.consultationDetails.copy(consultationContent = text)) } }
    fun updateHasChosenConsultant(hasChosen: Boolean) { _uiState.update { it.copy(hasChosenConsultant = hasChosen) } }
    fun updateConsultingFirm(firmName: String) { _uiState.update { it.copy(consultingFirm = firmName) } }
    fun addConsultant() { _uiState.update { it.copy(consultants = it.consultants + Consultant("", "", "", "")) } }
    fun removeConsultant(index: Int) { _uiState.update { val list = it.consultants.toMutableList(); list.removeAt(index); it.copy(consultants = list) } }
    fun updateConsultant(index: Int, consultant: Consultant) { _uiState.update { val list = it.consultants.toMutableList(); list[index] = consultant; it.copy(consultants = list) } }
    fun addDocument(uri: Uri) { _uiState.update { val list = it.attachedDocuments.toMutableList().apply { add(uri.toString()) }; it.copy(attachedDocuments = list) } }
    fun removeDocument(uriAsString: String) { _uiState.update { val list = it.attachedDocuments.toMutableList().apply { remove(uriAsString) }; it.copy(attachedDocuments = list) } }
    fun updateAcknowledgement(isAcknowledged: Boolean) { _uiState.update { it.copy(hasAcknowledgedPublicationObligations = isAcknowledged) } }
}