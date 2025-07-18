package com.example.rkwthringenapp.data

data class RkwFormData(
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
        lastYear = FinancialYear(year = "", employees = 0, turnover = 0L, balanceSheetTotal = 0L),
        penultimateYear = FinancialYear(year = "", employees = 0, turnover = 0L, balanceSheetTotal = 0L)
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
    val attachedDocuments: List<String> = emptyList()
)

data class ContactPerson(val name: String, val email: String, val phone: String)
data class BeneficialOwner(val lastName: String, val firstName: String, val birthDate: String = "", val taxId: String = "")
data class BankDetails(val institute: String, val iban: String, val taxId: String)
data class FinancialYear(val year: String = "", val employees: Int, val turnover: Long, val balanceSheetTotal: Long)
data class SmeClassification(val lastYear: FinancialYear, val penultimateYear: FinancialYear)
data class ConsultationDetails(val focus: String, val scopeInDays: Int, val dailyRate: Double, val endDate: String, val initialSituation: String, val consultationContent: String)
data class Consultant(val firstName: String, val lastName: String, val accreditationId: String = "", val email: String)