package com.easytec.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.easytec.R
import com.easytec.databinding.ActivityBookMarksBinding
import com.easytec.utils.Utils

class BookMarksActivity : AppCompatActivity() {
    lateinit var binding:ActivityBookMarksBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_book_marks)
        Utils.statusBarColor(this)
        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }
        Utils.searchViewTextClearSearchIconsColor(binding.searchView, this, R.color.white)
        Utils.setSearchViewHintColor(this, binding.searchView, R.color.hint_edt_color)
    }
}