package com.joshua.demo

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SectionIndexer
import com.joshua.demo.util.StringMatcher.match
import com.joshua.demo.widget.IndexAlphabeticalListView
import java.util.*

class CountriesListViewActivity : Activity() {
    private var list_countries: ArrayList<String>? = null
    private var listView: IndexAlphabeticalListView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        list_countries = ArrayList(Arrays.asList(*resources.getStringArray(R.array.countries_array)))
        list_countries!!.sort()
        val adapter = ContentAdapter(this,
                android.R.layout.simple_list_item_1, list_countries)
        listView = findViewById<View>(R.id.listview) as IndexAlphabeticalListView
        listView!!.adapter = adapter
        listView!!.isFastScrollEnabled = true
    }

    private inner class ContentAdapter(context: Context?, textViewResourceId: Int,
                                       objects: List<String>?) : ArrayAdapter<String?>(context!!, textViewResourceId, objects!!), SectionIndexer {
        private val sections = getString(R.string.sections_alphabetical)
        override fun getPositionForSection(section: Int): Int {
            for (i in section downTo 0) {
                for (j in 0 until count) {
                    if (i == 0) {
                        for (k in 0..9) {
                            if (match(getItem(j)!![0].toString(), k.toString())) return j
                        }
                    } else {
                        if (match(getItem(j)!![0].toString(), sections[i].toString())) return j
                    }
                }
            }
            return 0
        }

        override fun getSectionForPosition(position: Int): Int {
            return 0
        }

        override fun getSections(): Array<String?> {
            val sections = arrayOfNulls<String>(sections.length)
            for (i in this.sections.indices) sections[i] = this.sections[i].toString()
            return sections
        }
    }
}