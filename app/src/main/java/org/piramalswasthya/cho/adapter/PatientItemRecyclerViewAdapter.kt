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

package org.piramalswasthya.cho.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.piramalswasthya.cho.databinding.PatientListItemViewBinding
import org.piramalswasthya.cho.ui.commons.personal_details.PatientItemViewHolder
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsViewModel

/** UI Controller helper class to monitor Patient viewmodel and display list of patients. */
class PatientItemRecyclerViewAdapter(
  private val onItemClicked: (PersonalDetailsViewModel.PatientItem) -> Unit
) :
  ListAdapter<PersonalDetailsViewModel.PatientItem, PatientItemViewHolder>(PatientItemDiffCallback()) {

  class PatientItemDiffCallback : DiffUtil.ItemCallback<PersonalDetailsViewModel.PatientItem>() {
    override fun areItemsTheSame(
      oldItem: PersonalDetailsViewModel.PatientItem,
      newItem: PersonalDetailsViewModel.PatientItem
    ): Boolean = oldItem.resourceId == newItem.resourceId

    override fun areContentsTheSame(
      oldItem: PersonalDetailsViewModel.PatientItem,
      newItem: PersonalDetailsViewModel.PatientItem
    ): Boolean = oldItem.id == newItem.id
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientItemViewHolder {
    return PatientItemViewHolder(
      PatientListItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
  }

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onBindViewHolder(holder: PatientItemViewHolder, position: Int) {
    val item = currentList[position]
    holder.bindTo(item, onItemClicked)
  }
}
