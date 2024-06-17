package org.piramalswasthya.cho.ui.outreach_activity.outreach_activity_form

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentOutreachActivityFormBinding
import org.piramalswasthya.cho.ui.outreach_activity.OutreachActivity
import org.piramalswasthya.cho.utils.DateTimeUtil
import org.piramalswasthya.cho.utils.ImgUtils
import org.piramalswasthya.cho.utils.nullIfEmpty
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class OutreachActivityFormFragment : Fragment() {

    companion object {
        fun newInstance() = OutreachActivityFormFragment()
    }

    private val binding by lazy {
        FragmentOutreachActivityFormBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: OutreachActivityFormViewModel

    private lateinit var activityAdapter: ArrayAdapter<String>

    private var dateTimeUtil: DateTimeUtil = DateTimeUtil()

    private var currentFileName: String? = null
    private var currentPhotoPath: String? = null
    private lateinit var photoURI: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(OutreachActivityFormViewModel::class.java)
        activityAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            viewModel.activityList
        )

        binding.activityDropdown.setAdapter(activityAdapter)

        binding.dateOfEvent.setOnClickListener {
            dateTimeUtil.showDatePickerDialog(
                requireContext(),
                viewModel.outreachActivityModel.dateOfActivity,
                null,
                null
            ).show()
        }

        binding.activityDropdown.setOnItemClickListener { _, _, position, _ ->
            viewModel.outreachActivityModel.activityName = viewModel.activityList[position];
            binding.activityDropdown.setText(viewModel.activityList[position], false)
        }

        binding.noOfParticipant.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called to notify you that characters within `s` are about to be replaced with new text.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called to notify you that somewhere within `s`, the text has been replaced with new text.
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.outreachActivityModel.noOfParticipant =
                    s.toString().nullIfEmpty()?.toInt()
            }
        })

        binding.eventDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called to notify you that characters within `s` are about to be replaced with new text.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called to notify you that somewhere within `s`, the text has been replaced with new text.
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.outreachActivityModel.eventDesc = s.toString().nullIfEmpty()
            }
        })

        dateTimeUtil.selectedDate.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.outreachActivityModel.dateOfActivity = it
                binding.dateOfEvent.setText(DateTimeUtil.formatDate(it))
                binding.dateOfEvent.error = ""
            }
        }

        binding.addPhotos.setOnClickListener {
            checkAndRequestCameraPermission()
        }

        binding.saveEvent.setOnClickListener {
            if (viewModel.outreachActivityModel.dateOfActivity == null) {
                binding.dateOfEvent.setError("Date of event cannot be empty")
                return@setOnClickListener;
            } else {
                val formatter = SimpleDateFormat("dd-MM-yyyy")
                val formattedDate = formatter.format(
                    viewModel.outreachActivityModel.dateOfActivity
                )

                val isDateExist = viewModel.activities.any {
                    val inputDateString = it.activityDate
                    val inputDateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm:ss a")
                    val outputDateFormat = SimpleDateFormat("dd-MM-yyyy")

                    val date = inputDateString?.let { it1 -> inputDateFormat.parse(it1) }
                    val inputDate = date?.let { it1 -> outputDateFormat.format(it1) }
                    inputDate == formattedDate
                }
                if (isDateExist) {
                    Toast.makeText(requireContext(),"Events for this Date already exist",Toast.LENGTH_SHORT ).show()
                    return@setOnClickListener;
                }

            }

            if (viewModel.outreachActivityModel.activityName == null) {
                binding.activityDropdown.setError("Activity name cannot be empty")
                return@setOnClickListener;
            }

            if (viewModel.outreachActivityModel.eventDesc == null) {
                binding.eventDescription.setError("Event description cannot be empty")
                return@setOnClickListener;
            }

            if (viewModel.outreachActivityModel.noOfParticipant == null) {
                binding.noOfParticipant.setError("Number of participants cannot be empty")
                return@setOnClickListener;
            }
            binding.rlSaving.visibility = View.VISIBLE

            viewModel.outreachActivityModel.img1 =
                ImgUtils.base64ConvertedString(viewModel.outreachActivityModel.img1)
            viewModel.outreachActivityModel.img2 =
                ImgUtils.base64ConvertedString(viewModel.outreachActivityModel.img2)
            viewModel.saveNewActivity(viewModel.outreachActivityModel)
            viewModel.isDataSaved.observe(viewLifecycleOwner) {
                when (it) {
                    true -> {
                        requireActivity().finish()
                        startActivity(Intent(requireContext(), OutreachActivity::class.java))
                        binding.rlSaving.visibility = View.GONE
                    }

                    false -> {
                        Toast.makeText(
                            requireContext(),
                            "Error While Saving",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {

                    }
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Camera permission is granted, proceed to take a picture
            takePicture()
        } else {
            // Camera permission is not granted, request it
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        val permission =
            arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(permission, 112)
    }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { result: Boolean ->
            if (result) {
                // Picture was taken successfully, update the ImageView with the captured image
                if (photoURI == null)
//                    binding.ivImgCapture.setImageResource(R.drawable.ic_person)
                else {
                    if (viewModel.outreachActivityModel.img1.isNullOrEmpty()) {
                        viewModel.outreachActivityModel.img1 = currentPhotoPath!!
                        binding.iv1.visibility = View.VISIBLE
                        Glide.with(this).load(photoURI)
                            .into(binding.iv1)
                    } else {
                        viewModel.outreachActivityModel.img2 = currentPhotoPath!!
                        binding.iv2.visibility = View.VISIBLE
                        binding.addPhotos.visibility = View.GONE
                        Glide.with(this).load(photoURI)
                            .into(binding.iv2)
                    }
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun takePicture(position: Int? = null) {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            null
        }

        photoFile?.also {
            photoURI = FileProvider.getUriForFile(
                requireContext(),
                "org.piramalswasthya.cho.provider",
                it
            )
            takePictureLauncher.launch(photoURI)

        }
    }


    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir("images")
        currentFileName = "JPEG_${timeStamp}_.jpeg"
        var file = File(storageDir, currentFileName)

        // Ensure the file doesn't already exist
        var counter = 1
        while (file.exists()) {
            currentFileName = "JPEG_${timeStamp}_$counter.jpeg"
            file = File(storageDir, currentFileName)
            counter++
        }

        return file.apply {
            // Save a file path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }

    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 112) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed to open the camera
                takePicture()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_to_access_the_camera_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}