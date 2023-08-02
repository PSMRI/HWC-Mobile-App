/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.piramalswasthya.cho.ui.commons.personal_details

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Build
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.time.LocalDate
import java.time.Period
import org.hl7.fhir.r4.model.codesystems.RiskProbability
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.PatientListItemViewBinding
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity

class PatientItemViewHolder(binding: PatientListItemViewBinding) :
  RecyclerView.ViewHolder(binding.root) {
  private val nameView: TextView = binding.name
  private val ageView: TextView = binding.age
  private val phoneView: TextView = binding.phoneNo
  private val gender: TextView = binding.gender
  private val abhaBtn : MaterialButton = binding.btnAbha

  @RequiresApi(Build.VERSION_CODES.O)
  fun bindTo(
    patientItem: PersonalDetailsViewModel.PatientItem,
    onItemClicked: (PersonalDetailsViewModel.PatientItem) -> Unit
  ) {
    this.nameView.text = patientItem.name
    this.ageView.text = getFormattedAge(patientItem, ageView.context.resources)
    this.phoneView.text = patientItem.phone
    this.gender.text = getGender(patientItem.gender)
    this.itemView.setOnClickListener { onItemClicked(patientItem) }

//    this.abhaBtn.setOnClickListener {
//      val intent = Intent(it.context, AbhaIdActivity::class.java)
//      //TODO: Replace hard coded values of benRegId and benId with actual values
//      intent.putExtra("benRegId", 32129L)
//      intent.putExtra("benId", 690994260411L)
//      it.context.startActivity(intent)
//    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun getFormattedAge(
    patientItem: PersonalDetailsViewModel.PatientItem,
    resources: Resources
  ): String {
    if (patientItem.dob == null) return ""
    return Period.between(patientItem.dob, LocalDate.now()).let {
      when {
        it.years > 0 -> resources.getQuantityString(R.plurals.ageYear, it.years, it.years)
        it.months > 0 -> resources.getQuantityString(R.plurals.ageMonth, it.months, it.months)
        else -> resources.getQuantityString(R.plurals.ageDay, it.days, it.days)
      }
    }
  }

  /** The new ui just shows shortened id with just last 3 characters. */
  private fun getTruncatedId(patientItem: PersonalDetailsViewModel.PatientItem): String {
    return patientItem.resourceId.takeLast(3)
  }
  private fun getGender(string: String): String{
     if(string.equals("male"))
       return "Male"
    else if(string.equals("female"))
      return "Female"
      else
        return "Transgender"
  }
}
