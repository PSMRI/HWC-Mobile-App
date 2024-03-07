package org.piramalswasthya.cho.ui

import android.R
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.databinding.BindingAdapter

import android.net.Uri
import android.os.Build
import android.text.Editable
import android.text.Html
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import android.widget.RadioGroup.LayoutParams
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.android.material.color.MaterialColors
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.helpers.Konstants
import org.piramalswasthya.cho.model.AncFormState
import org.piramalswasthya.cho.model.AncFormState.*
//import org.piramalswasthya.cho.model.BenBasicDomain
import org.piramalswasthya.cho.model.FormInputOld
import org.piramalswasthya.cho.model.Gender
import org.piramalswasthya.cho.model.VaccineState
import org.piramalswasthya.cho.model.VaccineState.*
import timber.log.Timber
import org.piramalswasthya.cho.R as Resource


@BindingAdapter("listItems")
fun AutoCompleteTextView.setSpinnerItems(list: Array<String?>) {
    list?.let {
        this.setAdapter(ArrayAdapter(context, R.layout.simple_spinner_dropdown_item, it))
    }
}

@BindingAdapter("vaccineState")
fun ImageView.setVaccineState(syncState: VaccineState?) {
    syncState?.let {
//        visibility = View.VISIBLE
        val drawable = when (it) {
            DONE -> Resource.drawable.ic_check_circle
            MISSED -> Resource.drawable.ic_close
            PENDING -> Resource.drawable.ic_add_circle
            OVERDUE -> Resource.drawable.ic_overdue
            UNAVAILABLE -> null
        }
        drawable?.let { it1 -> setImageResource(it1) }
    }
}

@BindingAdapter("vaccineState")
fun Button.setVaccineState(syncState: VaccineState?) {

    syncState?.let {
        visibility = View.VISIBLE
        when (it) {
            PENDING,
            OVERDUE -> {
                text = "FILL"
            }

            DONE -> {
                text = "VIEW"
            }

            MISSED,
            UNAVAILABLE -> {
                visibility = View.GONE
            }
        }
    }
}


@BindingAdapter("scope", "recordCount")
fun TextView.setRecordCount(scope: CoroutineScope, count: Flow<Int>?) {
    count?.let { flow ->
        scope.launch {
            try{
                flow.collect {
                    text = it.toString()
                }
            }catch (e : Exception){
                Timber.d("Exception at record count : $e collected")
            }

        }
    } ?: run {
        text = null
    }
}

@BindingAdapter("allowRedBorder", "scope", "recordCount")
fun CardView.setRedBorder(allowRedBorder: Boolean, scope: CoroutineScope, count: Flow<Int>?) {
    count?.let {
        scope.launch {
            it.collect {
                if (it > 0 && allowRedBorder) {
                    setBackgroundResource(Resource.drawable.red_border)
                }
                //                else{
//                    this@setRedBorder.setBackgroundResource(0)
//                }
            }
        }
    }
//        ?: run {
//        this@setRedBorder.setBackgroundResource(0)
//    }
}

@BindingAdapter("benIdText")
fun TextView.setBenIdText(benId: Long?) {
    benId?.let {
        if (benId < 0L) {
            text = "Pending Sync"
            setTextColor(resources.getColor(android.R.color.holo_orange_light))
        } else {
            text = benId.toString()
            setTextColor(
                MaterialColors.getColor(
                    this,
                    com.google.android.material.R.attr.colorOnPrimary
                )
            )

        }
    }

}


@BindingAdapter("showBasedOnNumMembers")
fun TextView.showBasedOnNumMembers(numMembers: Int?) {
    numMembers?.let {
        visibility = if (it > 0) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("backgroundTintBasedOnNumMembers")
fun CardView.setBackgroundTintBasedOnNumMembers(numMembers: Int?) {
    numMembers?.let {
        val color = MaterialColors.getColor(
            this,
            if (it > 0) androidx.appcompat.R.attr.colorPrimary else android.R.attr.colorEdgeEffect
        )
        setCardBackgroundColor(color)
    }
}


@BindingAdapter("textBasedOnNumMembers")
fun TextView.textBasedOnNumMembers(numMembers: Int?) {
    numMembers?.let {
        text = if (it > 0) "Add Member" else "Add Head of Family"
    }
}


@BindingAdapter("allCaps")
fun TextInputEditText.setAllAlphabetCaps(allCaps: Boolean) {
    if (allCaps) {
        isAllCaps = true
        inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    }
}

@BindingAdapter("showLayout")
fun Button.setVisibilityOfLayout(show: Boolean?) {
    show?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("showLayout")
fun ImageView.setVisibilityOfLayout(show: Boolean?) {
    show?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("showLayout")
fun ViewGroup.setVisibilityOfLayout(show: Boolean?) {
    show?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("radioForm")
fun ConstraintLayout.setItems(form: FormInputOld?) {
}

@BindingAdapter("checkBoxesForm")
fun ConstraintLayout.setItemsCheckBox(form: FormInputOld?) {

    val ll = this.findViewById<LinearLayout>(Resource.id.ll_checks)
    ll.removeAllViews()
    ll.apply {
        form?.entries?.let { items ->
            orientation = form.orientation ?: LinearLayout.VERTICAL
            weightSum = items.size.toFloat()
            items.forEach {
                val cbx = CheckBox(this.context)
                cbx.layoutParams =
                    LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0F)
                cbx.id = View.generateViewId()
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) cbx.setTextAppearance(
                    context, android.R.style.TextAppearance_Material_Medium
                )
                else cbx.setTextAppearance(android.R.style.TextAppearance_Material_Subhead)
                cbx.text = it
                addView(cbx)
                if (form.value.value?.contains(it) == true) cbx.isChecked = true
                cbx.setOnCheckedChangeListener { _, b ->
                    if (b) {
                        if (form.value.value != null) form.value.value = form.value.value + it
                        else form.value.value = it
                    } else {
                        if (form.value.value?.contains(it) == true) {
                            form.value.value = form.value.value?.replace(it, "")
                        }
                    }
                    if (form.value.value.isNullOrBlank()) {
                        form.value.value = null
                    } else {
                        Timber.d("Called here!")
                        form.errorText = null
                        this@setItemsCheckBox.setBackgroundResource(0)
                    }
                    Timber.d("Checkbox values : ${form.value.value}")
                }
            }
        }
    }
}

@BindingAdapter("required")
fun TextView.setRequired(required: Boolean?) {
    required?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("imgRequired")
fun ImageView.setRequired(required: Boolean?) {
    required?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("required2")
fun TextView.setRequired2(required2: Boolean?) {
    required2?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("headingLine")
fun MaterialDivider.setHeadingLine(required: Boolean?) {
    required?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}


private val rotate = RotateAnimation(
    360F, 0F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
).apply {
    duration = 1000
    interpolator = LinearInterpolator()
    repeatCount = Animation.INFINITE
}


@BindingAdapter("syncState")
fun ImageView.setSyncState(syncState: SyncState?) {
    syncState?.let {
        visibility = View.VISIBLE
        val drawable = when (it) {
            SyncState.UNSYNCED -> Resource.drawable.ic_unsynced
            SyncState.SYNCING -> Resource.drawable.ic_syncing
            SyncState.SYNCED -> Resource.drawable.ic_synced
            else -> {
                Resource.drawable.ic_unsynced
            }
        }
        setImageResource(drawable)
        isClickable = it == SyncState.UNSYNCED
        if (it == SyncState.SYNCING) startAnimation(rotate)
    } ?: run {
        visibility = View.INVISIBLE
    }
}


@BindingAdapter("benImage")
fun ImageView.setBenImage(uriString: String?) {
    if (uriString == null) setImageResource(Resource.drawable.ic_person)
    else {
        Glide.with(this).load(Uri.parse(uriString)).placeholder(Resource.drawable.ic_person).circleCrop()
            .into(this)
    }
}

fun EditText.afterTextChanged(afterTextChanged: (String?) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            if(!editable.isNullOrBlank())
                afterTextChanged.invoke(editable.toString())
        }
    })
}


@BindingAdapter("list_avail")
fun Button.setCbacListAvail(list: List<Any>?) {
    list?.let {
        if (list.isEmpty())
            visibility = View.INVISIBLE
        else
            visibility = View.VISIBLE
    }
}

@BindingAdapter("anc_state_icon")
fun ImageView.setAncState(ancFormState: AncFormState?) {
    ancFormState?.let {
        setImageResource(
            when (it) {
                ALLOW_FILL -> {
                    Resource.drawable.ic_pending_actions
                }

                ALREADY_FILLED -> {
                    Resource.drawable.ic_check_circle
                }

                NO_FILL -> {
                    Resource.drawable.ic_close
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@BindingAdapter("cbac_name", "asteriskColor")
fun TextView.setAsteriskText(fieldName: String?, numAsterisk: Int?) {

    fieldName?.let {
        numAsterisk?.let {
            text = if (numAsterisk == 1) {
                Html.fromHtml(
                    resources.getString(Resource.string.radio_title_cbac, fieldName),
                    Html.FROM_HTML_MODE_LEGACY
                )
            } else if (numAsterisk == 2) {
                Html.fromHtml(
                    resources.getString(Resource.string.radio_title_cbac_ds, fieldName),
                    Html.FROM_HTML_MODE_LEGACY
                )
            } else {
                fieldName
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@BindingAdapter("asteriskRequired", "hintText")
fun TextInputLayout.setAsteriskFormText(required: Boolean?, title: String?) {

    required?.let {
        title?.let {
            hint = if (required) {
                Html.fromHtml(
                    resources.getString(Resource.string.radio_title_cbac, title),
                    Html.FROM_HTML_MODE_LEGACY
                )
            } else {
                title
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@BindingAdapter("asteriskRequired", "hintText")
fun TextView.setAsteriskTextView(required: Boolean?, title: String?) {

    required?.let {
        title?.let {
            text = if (required) {
                Html.fromHtml(
                    resources.getString(Resource.string.radio_title_cbac, title),
                    Html.FROM_HTML_MODE_LEGACY
                )
            } else {
                title
            }
        }
    }
}


