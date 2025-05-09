package com.alldocumentviewerapp.ui.activities.mainactivity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.data.StorageUsageCallBack
import com.alldocumentviewerapp.databinding.ActivityMainBinding
import com.alldocumentviewerapp.models.TotalFilesModel
import com.alldocumentviewerapp.ui.activities.AddPdfPasswordActivity
import com.alldocumentviewerapp.ui.activities.AllFoldersActivity
import com.alldocumentviewerapp.ui.activities.CreateZipFileActivity
import com.alldocumentviewerapp.ui.activities.DocAndDocxViewActivity
import com.alldocumentviewerapp.ui.activities.GetImagesFolder
import com.alldocumentviewerapp.ui.activities.GetVideosActivity
import com.alldocumentviewerapp.ui.activities.ImageToPdfActivity
import com.alldocumentviewerapp.ui.activities.MergePdfActivity
import com.alldocumentviewerapp.ui.activities.PDFViewActivity
import com.alldocumentviewerapp.ui.activities.PdfToImageActivity
import com.alldocumentviewerapp.ui.activities.RARFilesViewActivity
import com.alldocumentviewerapp.ui.activities.rtffiles.RTFFileViewActivity
import com.alldocumentviewerapp.ui.activities.ReadOnlinePdfActivity
import com.alldocumentviewerapp.ui.activities.RecentFilesActivity
import com.alldocumentviewerapp.ui.activities.SearchDocumentsActivity
import com.alldocumentviewerapp.ui.activities.SheetsViewActivity
import com.alldocumentviewerapp.ui.activities.SlidesViewActivity
import com.alldocumentviewerapp.ui.activities.TextFileViewAcitivyt
import com.alldocumentviewerapp.ui.activities.TextToPdfActivity
import com.alldocumentviewerapp.ui.activities.WordToPdfActivity
import com.alldocumentviewerapp.ui.activities.ZipUnZipActivity
import com.alldocumentviewerapp.ui.activities.barcodescanner.BarCodeScanner
import com.alldocumentviewerapp.ui.activities.notes.NotesViewActivity
import com.alldocumentviewerapp.ui.viewmodels.ReadAllDocxViewModel
import com.alldocumentviewerapp.utils.PermissionHelper
import com.alldocumentviewerapp.utils.Utils
import com.alldocumentviewerapp.utils.Utils.calculateUsedStoragePercentage
import com.alldocumentviewerapp.utils.Utils.feedBackIntent
import com.alldocumentviewerapp.utils.Utils.getAvailableStorageSpaceInGB
import com.alldocumentviewerapp.utils.Utils.getTotalStorageSpaceInGB
import com.alldocumentviewerapp.utils.Utils.myToast
import com.alldocumentviewerapp.utils.Utils.rippleEffect
import com.alldocumentviewerapp.utils.Utils.shareAppLink
import com.alldocumentviewerapp.utils.Utils.statusBarColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var permissionHelper: PermissionHelper
    val readAllDocxViewModel: ReadAllDocxViewModel by viewModels()
    var docDocxList = ArrayList<TotalFilesModel>()
    var pdfList = ArrayList<TotalFilesModel>()
    var slideList = ArrayList<TotalFilesModel>()
    var sheetsList = ArrayList<TotalFilesModel>()
    var textList = ArrayList<TotalFilesModel>()
    var zipList = ArrayList<TotalFilesModel>()
    var rarList = ArrayList<TotalFilesModel>()
    var rtfFilesList= ArrayList<TotalFilesModel>()

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Environment.isExternalStorageManager()
                    } else {
                        Toast.makeText(this@MainActivity, "Feature not available on older devices", Toast.LENGTH_LONG).show()
                        true
                    }
                ) {}
            }
        }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarColor(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        permissionHelper = PermissionHelper(this, requestPermissionLauncher)
        permissionHelper.requestReadExternalStoragePermission(this)

        binding.menuImg.setOnClickListener {
            rippleEffect(this@MainActivity, it)
            val popupMenu = PopupMenu(this@MainActivity, it)
            popupMenu.menuInflater.inflate(R.menu.menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.itemId) {

                        R.id.shareApp -> {
                            shareAppLink(this@MainActivity)
                            return true
                        }

                        R.id.feedback -> {
                            feedBackIntent(this@MainActivity)
                            return true
                        }

                        else -> return false

                    }
                }
            })
            popupMenu.show()
        }


        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            CoroutineScope(Dispatchers.IO).launch {
                val rootDirectory: File = Environment.getExternalStorageDirectory()
                val documentFolders: Map<File, List<TotalFilesModel>> =
                    readAllDocxViewModel.getDocumentFoldersWithFileDetails(rootDirectory)
                withContext(Dispatchers.Main) {
                    binding.allFolderCount.text = "(${documentFolders.size})"
                }
            }
        }


        setSupportActionBar(binding.toolBAR)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val toggle = ActionBarDrawerToggle(this, binding.drawer, binding.toolBAR, R.string.open, R.string.close)
        toggle.drawerArrowDrawable.color = Color.WHITE
        binding.drawer.addDrawerListener(toggle)
        toggle.syncState()


        binding.navDrawer.setNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.share -> {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "All Document Viewer")
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.alldocumentviewerapp")
                    startActivity(Intent.createChooser(shareIntent, "Share link via"))
                }
                R.id.search->{
                    lifecycleScope.launch {
                        startActivity(Intent(this@MainActivity, SearchDocumentsActivity::class.java))
                    }
                }
            }
            true
        }


        binding.searchLinear.setOnClickListener {
            rippleEff(it)
            lifecycleScope.launch {
                delay(1000)
                startActivity(Intent(this@MainActivity, SearchDocumentsActivity::class.java))
            }
        }


        binding.allDocumentLinear.setOnClickListener {
            rippleEff(it)
            lifecycleScope.launch {
                delay(1000)
                startActivity(Intent(this@MainActivity, SearchDocumentsActivity::class.java))
            }
        }

        binding.wordLinear.setOnClickListener {
            rippleEff(it)
            startForDocumentActivity(DocAndDocxViewActivity(), "listOfDocx", docDocxList)
        }

        binding.pdfLinear.setOnClickListener {
            rippleEff(it)
            startForDocumentActivity(PDFViewActivity(), "pdfFilesList", pdfList)
        }

        binding.slideLinear.setOnClickListener {
            rippleEff(it)
            startForDocumentActivity(SlidesViewActivity(), "slideFilesList", slideList)
        }


        binding.sheetLinear.setOnClickListener {
            rippleEff(it)
            startForDocumentActivity(SheetsViewActivity(), "sheetsFilesList", sheetsList)
        }


        binding.textLinear.setOnClickListener {
            startForDocumentActivity(TextFileViewAcitivyt(), "textFileList", textList)
            rippleEff(it)
        }

        binding.zipLinear.setOnClickListener {
            startForDocumentActivity(ZipUnZipActivity(), "zipFileList", zipList)
            rippleEff(it)
        }

        binding.rtfLinear.setOnClickListener {
            startForDocumentActivity(RTFFileViewActivity(), "rtfFileList", rtfFilesList)
            rippleEff(it)
        }

        binding.folderFiles.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, AllFoldersActivity::class.java))
        }

        binding.rarLinear.setOnClickListener {
            rippleEff(it)
            startForDocumentActivity(RARFilesViewActivity(), "rarFiles", rarList)
        }


        binding.imageLinear.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, GetImagesFolder::class.java))
        }

        binding.videoLinear.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, GetVideosActivity::class.java))
        }

        binding.imageToPdfLinear.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, ImageToPdfActivity::class.java))
        }

        binding.textToPdfLinear.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, TextToPdfActivity::class.java))
        }

        binding.pdfToImage.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, PdfToImageActivity::class.java))
        }

        binding.recentFilesLinear.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, RecentFilesActivity::class.java))
        }

        binding.addPdfPassword.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, AddPdfPasswordActivity::class.java))
        }

        binding.readOnlinePdfLinear.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, ReadOnlinePdfActivity::class.java))
        }
        binding.mergePdfLinear.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, MergePdfActivity::class.java))
        }

        binding.wordToPdfLinear.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, WordToPdfActivity::class.java))
        }

        binding.createNotesLinear.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, NotesViewActivity::class.java))
        }

        binding.scanQrCodeLinear.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, BarCodeScanner::class.java))
        }

        binding.scanBarCodeLinear.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, BarCodeScanner::class.java))
        }

        binding.createZipLinear.setOnClickListener {
            rippleEff(it)
            startActivity(Intent(this@MainActivity, CreateZipFileActivity::class.java))
        }

        binding.settingLinear.setOnClickListener {
            rippleEff(it)
        }

        binding.slideToPdf.setOnClickListener {
            rippleEff(it)
        }


        binding.allowPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // For Android(Red Velvet Cake) 11 and above
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent().apply {
                        action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                        data = Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                }
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
               // For Android 10
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            else {
                // For Android versions below 11
                val legacyReadPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE
                val legacyWritePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                val legacyReadPermissionGranted = checkSelfPermission(legacyReadPermission) == PackageManager.PERMISSION_GRANTED
                val legacyWritePermissionGranted = checkSelfPermission(legacyWritePermission) == PackageManager.PERMISSION_GRANTED
                if (!legacyWritePermissionGranted || !legacyReadPermissionGranted) {
                    permissionHelper.showRationaleDialog(this)
                }
            }
        }
    }

    fun rippleEff(view: View) {
        rippleEffect(this@MainActivity, view)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()

        binding.refresh.setOnClickListener {
            binding.pd.visibility=View.VISIBLE
            rippleEff(it)
            lifecycleScope.launch(Dispatchers.Main) {
                val list = readAllDocxViewModel.getAllDocx(this@MainActivity)
                countFiles(list)
                withContext(Dispatchers.Main){
                    binding.pd.visibility=View.GONE
                    myToast(this@MainActivity,"Refreshed",Toast.LENGTH_SHORT)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            if (Environment.isExternalStorageManager()) {
                showViews()
                lifecycleScope.launch(Dispatchers.Main) {
                    var list = readAllDocxViewModel.getAllDocx(this@MainActivity)
                    Log.i("mobile", "onResume:mobil is 11 or above")
                    countFiles(list)
                }
            }
        } else {
            val legacyReadPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE
            val legacyReadPermissionGranted = checkSelfPermission(legacyReadPermission) == PackageManager.PERMISSION_GRANTED

            if (legacyReadPermissionGranted) {
                showViews()
                lifecycleScope.launch(Dispatchers.Main) {
                    var list = readAllDocxViewModel.getAllDocx(this@MainActivity)
                    countFiles(list)
                }
            } else {
                permissionHelper.checkPermissionIsGrantedOrNot(this)
            }
        }
    }


    @SuppressLint("SetTextI18n")
    fun showViews() {

        binding.refresh.visibility = View.VISIBLE
        binding.searchCard.visibility = View.VISIBLE
        binding.textView2.visibility = View.VISIBLE
        binding.scrollView.visibility = View.VISIBLE
        binding.allowPermission.visibility = View.GONE
        binding.textView13.visibility = View.GONE

        val totalSpace=getTotalStorageSpaceInGB()
        val availableSpace=getAvailableStorageSpaceInGB()

        val totalSpaceFormatter= String.format("%.2f",totalSpace)
        val availableSpaceFormatter= String.format("%.2f",availableSpace)

        binding.totalSpace.text="$totalSpaceFormatter GB"
        binding.availabeSpace.text="$availableSpaceFormatter GB"

        calculateUsedStoragePercentage(this@MainActivity,object:StorageUsageCallBack{
            override fun onStorageCalculated(usedStoragePercentage: Int) {
                binding.progressIndicator.apply {
                    progressTintList = ColorStateList.valueOf(Color.BLUE)
                    progress = usedStoragePercentage
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun renderDataOnViews(
        totalFiles: Int,
        wordFiles: Int,
        pdfCount: Int,
        slideCount: Int,
        textCount: Int,
        sheetXlsXlsxCount: Int,
        zipFilesCounter: Int,
        rarFilesCounter: Int
    ) {

        rtfFilesList = Utils.getRTFFiles()
        binding.countAllDocx.text = "($totalFiles)"
        binding.countWord.text = "($wordFiles)"
        binding.pdfCount.text = "($pdfCount)"
        binding.slideCount.text = "($slideCount)"
        binding.textCount.text = "($textCount)"
        binding.sheetCount.text = "($sheetXlsXlsxCount)"
        binding.zipCounter.text = "($zipFilesCounter)"
        binding.rarCount.text = "($rarFilesCounter)"
        binding.rtfCount.text = "(${rtfFilesList.size})"
        val count = cacheDir.listFiles()?.count { file ->
            file.isFile && file.extension == "json"
        } ?: 0
        binding.recentFilesCount.text = "($count)"

    }

    private fun countFiles(list: ArrayList<TotalFilesModel>) {

        docDocxList.clear()
        pdfList.clear()
        slideList.clear()
        sheetsList.clear()
        textList.clear()
        zipList.clear()
        rarList.clear()

        val totalFiles = list.size
        var docxFilesCount = 0
        var pdfFilesCount = 0
        var pptFilesCount = 0
        var textFilesCount = 0
        var sheetXlsXlxsFilesCount = 0
        var zipFilesCounter = 0
        var rarFilesCounter = 0

        for (fileModel in list) {
            val fileExtension = fileModel.fileName.substringAfterLast('.', "")
            when {
                fileExtension.equals("pdf", ignoreCase = true) -> {
                    pdfList.add(fileModel)
                    pdfFilesCount++
                }

                fileExtension.equals("doc", ignoreCase = true) -> {
                    docxFilesCount++
                    docDocxList.add(fileModel)
                }

                fileExtension.equals("docx", ignoreCase = true) -> {
                    docxFilesCount++
                    docDocxList.add(fileModel)
                }

                fileExtension.equals("ppt", ignoreCase = true) -> {
                    pptFilesCount++
                    slideList.add(fileModel)
                }

                fileExtension.equals("pptx", ignoreCase = true) -> {
                    pptFilesCount++
                    slideList.add(fileModel)
                }

                fileExtension.equals("txt", ignoreCase = true) -> {
                    textFilesCount++
                    textList.add(fileModel)
                }

                fileExtension.equals("xls", ignoreCase = true) -> {
                    sheetXlsXlxsFilesCount++
                    sheetsList.add(fileModel)
                }

                fileExtension.equals("xlsx", ignoreCase = true) -> {
                    sheetXlsXlxsFilesCount++
                    sheetsList.add(fileModel)
                }

                fileExtension.equals("zip", ignoreCase = true) -> {
                    zipFilesCounter++
                    zipList.add(fileModel)
                }

                fileExtension.equals("rar", ignoreCase = true) -> {
                    rarList.add(fileModel)
                    rarFilesCounter++
                }

            }
        }

        renderDataOnViews(totalFiles, docxFilesCount, pdfFilesCount, pptFilesCount, textFilesCount, sheetXlsXlxsFilesCount, zipFilesCounter, rarFilesCounter)
    }

    fun startForDocumentActivity(activity: Activity, keyOfData: String, list: ArrayList<TotalFilesModel>) {
        lifecycleScope.launch {
            delay(1000)
            var intent = Intent(this@MainActivity, activity::class.java)
            intent.putParcelableArrayListExtra(keyOfData, list)
            startActivity(intent)
        }
    }

}

