package com.easytec.ui.activities.notes

import android.annotation.SuppressLint
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.easytec.EasyTechDatabase
import com.easytec.R
import com.easytec.adapters.NoteAdapter
import com.easytec.models.Note
import com.easytec.ui.viewmodels.NotesViewModel
import com.easytec.utils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class NotesViewActivity : AppCompatActivity() {
    @Inject
    lateinit var easyTechDatabase: EasyTechDatabase

    lateinit var addBtn: FloatingActionButton
    lateinit var notesRecycler: RecyclerView
    lateinit var list: ArrayList<Note>
    lateinit var adapter: NoteAdapter
    lateinit var searchView: SearchView
    lateinit var temp: ArrayList<Note>
    lateinit var backArrowImg: ImageView
    val notesViewModel: NotesViewModel by viewModels()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes_view)
        Utils.statusBarColor(this@NotesViewActivity)
        list = ArrayList()
        temp = ArrayList()
        notesRecycler = findViewById(R.id.notesRecycler)
        backArrowImg = findViewById(R.id.backArrowImg)
        addBtn = findViewById(R.id.addBtn)

        backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, backArrowImg) {
                onBackPressed()
            }
        }

        addBtn.setOnClickListener {
            showDialog()
        }

        lifecycleScope.launch { // getting data from data base.
            notesViewModel.apply {
                getNotes()
                notes.observe(this@NotesViewActivity){notesList->
                    list.clear()
                    notesList.forEach { it ->
                        list.add(it)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }

        adapter = NoteAdapter(list, this,easyTechDatabase,notesViewModel)
        notesRecycler.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        notesRecycler.adapter = adapter
        adapter.notifyDataSetChanged()

    }

    @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
    private fun showDialog() {
        var builder = AlertDialog.Builder(this@NotesViewActivity).setView(R.layout.add_note_dialog).show()
        var titleEdt = builder.findViewById<EditText>(R.id.titleEdt)
        var desEdt = builder.findViewById<EditText>(R.id.desEdt)

        var cancelBtn = builder.findViewById<Button>(R.id.cancelBtn)
        var saveBtn = builder.findViewById<Button>(R.id.saveBtn)

        cancelBtn.setOnClickListener {
            builder.dismiss()
        }

        saveBtn.setOnClickListener {
            var title = titleEdt.text.toString()
            var description = desEdt.text.toString()
            if (title.isEmpty()) {
                titleEdt.error = "Enter title."
            } else if (description.isEmpty()) {
                desEdt.error = "Enter description."
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    var date = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(Date())
                    var data=Note(0, title, description, date)
                    notesViewModel.putNotes(data) // uploading Notes.
                    withContext(Dispatchers.Main) {
                        adapter.notifyDataSetChanged()
                    }
                }
                builder.dismiss()
            }
        }
    }
}