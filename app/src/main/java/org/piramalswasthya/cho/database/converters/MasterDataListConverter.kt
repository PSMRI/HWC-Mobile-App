package org.piramalswasthya.cho.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.piramalswasthya.cho.moddel.OccupationMaster
import org.piramalswasthya.cho.model.AgeUnit
import org.piramalswasthya.cho.model.AlcoholDropdown
import org.piramalswasthya.cho.model.AllergicReactionDropdown
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.ChiefComplaintMaster
import org.piramalswasthya.cho.model.CommunityMaster
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.DoseType
import org.piramalswasthya.cho.model.FamilyMemberDropdown
import org.piramalswasthya.cho.model.GenderMaster
import org.piramalswasthya.cho.model.GovIdEntityMaster
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.model.IncomeMaster
import org.piramalswasthya.cho.model.Language
import org.piramalswasthya.cho.model.LiteracyStatus
import org.piramalswasthya.cho.model.MaritalStatusMaster
import org.piramalswasthya.cho.model.OtherGovIdEntityMaster
import org.piramalswasthya.cho.model.QualificationMaster
import org.piramalswasthya.cho.model.RelationshipMaster
import org.piramalswasthya.cho.model.ReligionMaster
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.model.SurgeryDropdown
import org.piramalswasthya.cho.model.TobaccoDropdown
import org.piramalswasthya.cho.model.VaccineType
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.model.VisitCategory
import org.piramalswasthya.cho.model.VisitReason

object MasterDataListConverter {

    @TypeConverter
    fun toStatesMasterList(value: String?): List<StateMaster> {
        val listType = object : TypeToken<List<StateMaster?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toDistrictMasterList(value: String?): List<DistrictMaster> {
        val listType = object : TypeToken<List<DistrictMaster?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toBlockMasterList(value: String?): List<BlockMaster> {
        val listType = object : TypeToken<List<BlockMaster?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toVillageMasterList(value: String?): List<VillageMaster> {
        val listType = object : TypeToken<List<VillageMaster?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toLanguageList(value: String?): List<Language> {
        val listType = object : TypeToken<List<Language?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toVisitReasonsList(value: String?): List<VisitReason> {
        val listType = object : TypeToken<List<VisitReason?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toVisitCategoryList(value: String?): List<VisitCategory> {
        val listType = object : TypeToken<List<VisitCategory?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toGenderList(value: String?): List<GenderMaster> {
        val listType = object : TypeToken<List<GenderMaster?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toAgeUnitList(value: String?): List<AgeUnit> {
        val listType = object : TypeToken<List<AgeUnit?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toIncomeStatusList(value: String?): List<IncomeMaster> {
        val listType = object : TypeToken<List<IncomeMaster?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toLiteracyStatusList(value: String?): List<LiteracyStatus> {
        val listType = object : TypeToken<List<LiteracyStatus?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toCommunityMasterList(value: String?): List<CommunityMaster> {
        val listType = object : TypeToken<List<CommunityMaster?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toGovIdEntityList(value: String?): List<GovIdEntityMaster> {
        val listType = object : TypeToken<List<GovIdEntityMaster?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toOtherGovIdEntityList(value: String?): List<OtherGovIdEntityMaster> {
        val listType = object : TypeToken<List<OtherGovIdEntityMaster?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toMaritalStatusList(value: String?): List<MaritalStatusMaster> {
        val listType = object : TypeToken<List<MaritalStatusMaster?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toQualificationMasterList(value: String?): List<QualificationMaster> {
        val listType = object : TypeToken<List<QualificationMaster?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toReligionMasterList(value: String?): List<ReligionMaster> {
        val listType = object : TypeToken<List<ReligionMaster?>?>() {}.type
        return Gson().fromJson(value, listType)
    }
    @TypeConverter
    fun toOccupationMasterList(value: String?): List<OccupationMaster> {
        val listType = object : TypeToken<List<OccupationMaster?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toRelationshipMasterList(value: String?): List<RelationshipMaster> {
        val listType = object : TypeToken<List<RelationshipMaster?>?>() {}.type
        return Gson().fromJson(value, listType)
    }
    @TypeConverter
    fun toVaccineTypeList(value: String): List<VaccineType> {
        val listType = object : TypeToken<List<VaccineType?>?>() {}.type
        return Gson().fromJson(value, listType)
    }
    @TypeConverter
    fun toDoseTypeList(value: String): List<DoseType> {
        val listType = object : TypeToken<List<DoseType?>?>() {}.type
        return Gson().fromJson(value, listType)
    }
    @TypeConverter
    fun toSubCatVisitList(value: String?): List<SubVisitCategory>{
        val listType = object : TypeToken<List<SubVisitCategory?>?>(){}.type
        return Gson().fromJson(value,listType)
    }
    @TypeConverter
    fun toChiefMasterComplaintList(value: String?): List<ChiefComplaintMaster>{
        val listType = object : TypeToken<List<ChiefComplaintMaster?>?>(){}.type
        return Gson().fromJson(value,listType)
    }
    @TypeConverter
    fun toIllnessList(value: String?):List<IllnessDropdown>{
        val listType = object :TypeToken<List<IllnessDropdown?>?>(){}.type
        return Gson().fromJson(value,listType)
    }
    @TypeConverter
    fun toAlcoholList(value: String?):List<AlcoholDropdown>{
        val listType = object :TypeToken<List<AlcoholDropdown?>?>(){}.type
        return Gson().fromJson(value,listType)
    }
    @TypeConverter
    fun toAllergyList(value: String?):List<AllergicReactionDropdown>{
        val listType = object :TypeToken<List<AllergicReactionDropdown?>?>(){}.type
        return Gson().fromJson(value,listType)
    }
    @TypeConverter
    fun toFamilyMemberList(value: String?):List<FamilyMemberDropdown>{
        val listType = object :TypeToken<List<FamilyMemberDropdown?>?>(){}.type
        return Gson().fromJson(value,listType)
    }
    @TypeConverter
    fun toSurgeryList(value: String?):List<SurgeryDropdown>{
        val listType = object :TypeToken<List<SurgeryDropdown?>?>(){}.type
        return Gson().fromJson(value,listType)
    }
    @TypeConverter
    fun toTobaccoList(value: String?):List<TobaccoDropdown>{
        val listType = object :TypeToken<List<TobaccoDropdown?>?>(){}.type
        return Gson().fromJson(value,listType)
    }

}