/*
 * Copyright 2018 Bakumon. https://github.com/Bakumon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package me.bakumon.moneykeeper.ui.typerecords

import android.os.Bundle
import android.view.Gravity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.layout_list.*
import me.bakumon.moneykeeper.R
import me.bakumon.moneykeeper.base.ErrorResource
import me.bakumon.moneykeeper.base.SuccessResource
import me.bakumon.moneykeeper.database.entity.RecordForList
import me.bakumon.moneykeeper.ui.common.BaseFragment
import me.bakumon.moneykeeper.ui.common.Empty
import me.bakumon.moneykeeper.ui.common.EmptyViewBinder
import me.bakumon.moneykeeper.utill.ToastUtils
import me.bakumon.moneykeeper.widget.WidgetProvider
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import me.drakeet.multitype.register
import java.util.*

/**
 * 某一类型记账记录
 * 按时间排序
 *
 * @author Bakumon https://bakumon.me
 */
class TypeRecordsByMoneyFragment : BaseFragment() {

    private lateinit var mViewModel: TypeRecordsViewModel
    private lateinit var mAdapter: MultiTypeAdapter

    private var mRecordType: Int = 0
    private var mRecordTypeId: Int = 0

    private lateinit var dateFrom: Date
    private lateinit var dateTo: Date

    override val layoutId: Int
        get() = R.layout.layout_list

    override fun onInit(savedInstanceState: Bundle?) {
        val bundle = arguments
        if (bundle != null) {
            mRecordType = bundle.getInt(KEY_RECORD_TYPE)
            mRecordTypeId = bundle.getInt(KEY_RECORD_TYPE_ID)
            dateFrom = bundle.getSerializable(KEY_DATE_FROM) as Date
            dateTo = bundle.getSerializable(KEY_DATE_TO) as Date
        }

        mAdapter = MultiTypeAdapter()
        mAdapter.register(RecordForList::class, RecordsByMoneyViewBinder { deleteRecord(it) })
        mAdapter.register(Empty::class, EmptyViewBinder())
        recyclerView.adapter = mAdapter

        mViewModel = getViewModel()
        getData()
    }

    private fun deleteRecord(record: RecordForList) {
        mViewModel.deleteRecord(record).observe(this, Observer {
            when (it) {
                is SuccessResource<Boolean> -> {
                    // 更新 widget
                    if (context != null) {
                        WidgetProvider.updateWidget(context!!)
                    }
                }
                is ErrorResource<Boolean> -> ToastUtils.show(R.string.toast_record_delete_fail)
            }
        })
    }

    override fun lazyInitData() {

    }

    private fun getData() {
        mViewModel.getRecordForListWithTypes(1, mRecordType, mRecordTypeId, dateFrom, dateTo).observe(this, Observer {
            if (it != null) {
                setItems(it)
            }
        })
    }

    private fun setItems(records: List<RecordForList>) {
        val items = Items()
        if (records.isEmpty()) {
            items.add(Empty(getString(R.string.text_empty_tip), Gravity.CENTER))
        } else {
            items.addAll(records)
        }
        mAdapter.items = items
        mAdapter.notifyDataSetChanged()
    }

    companion object {
        private const val KEY_RECORD_TYPE = "KEY_RECORD_TYPE"
        private const val KEY_RECORD_TYPE_ID = "KEY_RECORD_TYPE_ID"
        private const val KEY_DATE_FROM = "KEY_DATE_FROM"
        private const val KEY_DATE_TO = "KEY_DATE_TO"

        fun newInstance(recordType: Int, recordTypeId: Int, dateFrom: Date, dateTo: Date): TypeRecordsByMoneyFragment {
            val fragment = TypeRecordsByMoneyFragment()
            val bundle = Bundle()
            bundle.putInt(KEY_RECORD_TYPE, recordType)
            bundle.putInt(KEY_RECORD_TYPE_ID, recordTypeId)
            bundle.putSerializable(KEY_DATE_FROM, dateFrom)
            bundle.putSerializable(KEY_DATE_TO, dateTo)
            fragment.arguments = bundle
            return fragment
        }
    }
}
