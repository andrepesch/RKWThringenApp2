package com.example.rkwthringenapp.data

// HINWEIS: Wir importieren Uri hier nicht mehr, da wir Strings verwenden
// import android.net.Uri

data class RkwFormData(
    // ... alle anderen Felder bleiben unverändert
    val companyName: String = "",
    val legalForm: String = "",
    val foundationDate: String = "",
    val streetAndNumber: String = "",
    val postalCode: String = "",
    val city: String = "",
    val isVatDeductible: Boolean = false,
    val industrySector: String = "",
    val hasWebsite: Boolean = false,
    val websiteUrl: String = "",
    val mainContact: ContactPerson = ContactPerson(name = "", email = "", phone = ""),
    val beneficialOwners: List<BeneficialOwner> = emptyList(),
    val bankDetails: BankDetails = BankDetails(institute = "", iban = "", taxId = ""),
    val smeClassification: SmeClassification = SmeClassification(
        lastYear = FinancialYear(employees = 0, turnover = 0.0, balanceSheetTotal = 0.0),
        penultimateYear = FinancialYear(employees = 0, turnover = 0.0, balanceSheetTotal = 0.0)
    ),
    val consultationDetails: ConsultationDetails = ConsultationDetails(
        focus = "",
        scopeInDays = 0,
        dailyRate = 0.0,
        endDate = "",
        initialSituation = "",
        consultationContent = ""
    ),
    val hasChosenConsultant: Boolean = false,
    val consultingFirm: String = "",
    val consultants: List<Consultant> = emptyList(),
    val hasAcknowledgedPublicationObligations: Boolean = true,
    // ÄNDERUNG: Von List<Uri> zu List<String>
    val attachedDocuments: List<String> = emptyList()
)

// Die Hilfs-Datenklassen bleiben unverändert
data class ContactPerson(val name: String, val email: String, val phone: String)
data class BeneficialOwner(val lastName: String, val firstName: String, val birthDate: String = "", val taxId: String = "")
data class BankDetails(val institute: String, val iban: String, val taxId: String)
data class FinancialYear(val employees: Int, val turnover: Double, val balanceSheetTotal: Double)
data class SmeClassification(val lastYear: FinancialYear, val penultimateYear: FinancialYear)
data class ConsultationDetails(val focus: String, val scopeInDays: Int, val dailyRate: Double, val endDate: String, val initialSituation: String, val consultationContent: String)
data class Consultant(val firstName: String, val lastName: String, val accreditationId: String = "", val email: String)