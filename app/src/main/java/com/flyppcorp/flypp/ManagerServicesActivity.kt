package com.flyppcorp.flypp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.flyppcorp.Helper.MyViewPagerAdapter
import com.flyppcorp.tabsFragments.*
import kotlinx.android.synthetic.main.activity_manager_services.*

class ManagerServicesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_services)
        val adapter = MyViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(FragmentPendentes(), "Pendente")
        adapter.addFragment(FragmentAndamento(), "Andamento")
        adapter.addFragment(FragmentFinalizado(), "Finalizado")
        adapter.addFragment(FragmentTodos(), "Todos")
        viewPagerMs.adapter = adapter
        tabManagerServices.setupWithViewPager(viewPagerMs)

    }
}
