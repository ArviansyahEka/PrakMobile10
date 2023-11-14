package com.example.prakmobile10.UI

import android.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AdapterView.OnItemLongClickListener
import androidx.lifecycle.Observer
import com.example.prakmobile10.Database.Note
import com.example.prakmobile10.Database.NoteDao
import com.example.prakmobile10.Database.NoteRoomDatabase
import com.example.prakmobile10.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var mNotesDao: NoteDao
    private lateinit var executorService: ExecutorService
    private var updateId: Int = 0
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        executorService = Executors.newSingleThreadExecutor()
        val db = NoteRoomDatabase.getDatabase(this)
        mNotesDao = db!!.noteDao()!!

        with(binding) {
            btnAdd.setOnClickListener {
                insert(
                    Note(
                        title = txtTitle.text.toString(),
                        description = txtDesc.text.toString(),
                        date = txtDate.text.toString()
                    )
                )
                setEmptyField()
            }

            btnUpdate.setOnClickListener {
                update(
                    Note(
                        id = updateId,
                        title = txtTitle.text.toString(),
                        description = txtDesc.text.toString(),
                        date = txtDate.text.toString()
                    )
                )
                updateId = 0
                setEmptyField()
            }

            listView.setOnItemClickListener { _, _, i, _ ->
                val item = listView.adapter.getItem(i) as Note
                updateId = item.id
                txtTitle.setText(item.title)
                txtDesc.setText(item.description)
                txtDate.setText(item.date)
            }

            listView.onItemLongClickListener = OnItemLongClickListener { _, _, i, _ ->
                val item = listView.adapter.getItem(i) as Note
                delete(item)
                true
            }
            getAllNotes()
        }
    }

    private fun insert(note: Note) {
        executorService.execute { mNotesDao.insert(note) }
    }

    private fun delete(note: Note) {
        executorService.execute { mNotesDao.delete(note) }
    }

    private fun update(note: Note) {
        executorService.execute { mNotesDao.update(note) }
    }

    private fun getAllNotes() {
        mNotesDao.getAllNotes().observe(this, Observer { notes ->
            val adapter: ArrayAdapter<Note> = ArrayAdapter(
                this,
                R.layout.simple_list_item_1, notes
            )
            binding.listView.adapter = adapter
        })
    }

    override fun onResume() {
        super.onResume()
        getAllNotes()
    }

    private fun setEmptyField() {
        with(binding) {
            txtTitle.setText("")
            txtDesc.setText("")
            txtDate.setText("")
        }
    }
}
