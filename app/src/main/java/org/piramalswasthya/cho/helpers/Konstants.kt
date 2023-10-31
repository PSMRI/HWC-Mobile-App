package org.piramalswasthya.cho.helpers

import java.util.concurrent.TimeUnit

object Konstants {

    val minMillisBwtweenCbacFiling : Long = TimeUnit.DAYS.toMillis(365)
    const val  amritTokenTimeoutDuration: Int = 100

    //Dev
    const val devCode = 112


    const val tempBenImagePrefix: String = "tmp_image_file"
    const val tempBenImageSuffix: String = ".jpeg"
    const val editTextHintLimit: Byte = 50
    const val benIdCapacity: Int = 100
    const val benIdWorkerTriggerLimit = 90

    //Ben
    const val micClickIndex = -108

    //Note: min and max both are inclusive for age ranges
    const val minAgeForEligibleCouple: Int = 15
    const val maxAgeForEligibleCouple: Int = 49
    const val minAgeForNcd: Int = 30
    const val minAgeForReproductiveAge: Int = 15
    const val maxAgeForReproductiveAge: Int = 49
    const val maxAgeForInfant: Int = 1
    const val minAgeForChild: Int = 2
    const val maxAgeForChild: Int = 5
    const val minAgeForAdolescent: Int = 6
    const val maxAgeForAdolescent: Int = 14
    const val maxAgeForCdr: Int = 14
    const val minAgeForGenBen : Int = 15
    const val maxAgeForGenBen : Int = 99
    const val minAgeForMarriage: Int = 12

    //HBNC
    const val hbncCardDay = 0
    const val hbncPart1Day = -1
    const val hbncPart2Day = -2

    //PW-ANC
    const val minAnc1Week = 5
    const val maxAnc1Week = 12
    const val minAnc2Week = 14
    const val maxAnc2Week = 27
    const val minAnc3Week = 28
    const val maxAnc3Week = 35
    const val minAnc4Week = 36
    const val maxAnc4Week = 40

    const val minWeekToShowDelivered = 23


    const val babyLowWeight: Double = 2.5


        //PNC-EC cycle
    const val pncEcGap : Long = 45


    const val defaultTimeStamp = 1577817001000L
}