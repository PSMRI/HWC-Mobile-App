package org.piramalswasthya.cho.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.piramalswasthya.cho.moddel.OccupationMaster
import org.piramalswasthya.cho.model.AgeUnit
import org.piramalswasthya.cho.model.CommunityMaster
import org.piramalswasthya.cho.model.GenderMaster
import org.piramalswasthya.cho.model.GovIdEntityMaster
import org.piramalswasthya.cho.model.IncomeMaster
import org.piramalswasthya.cho.model.Language
import org.piramalswasthya.cho.model.LiteracyStatus
import org.piramalswasthya.cho.model.MaritalStatusMaster
import org.piramalswasthya.cho.model.OtherGovIdEntityMaster
import org.piramalswasthya.cho.model.QualificationMaster
import org.piramalswasthya.cho.model.RelationshipMaster
import org.piramalswasthya.cho.model.ReligionMaster
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.model.VisitCategory
import org.piramalswasthya.cho.model.VisitReason

object MasterDataListConverter {

    @TypeConverter
    fun toStatesMasterList(value: String?): List<StateMaster> {
        val listType = object : TypeToken<List<StateMaster?>?>() {}.type
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

}