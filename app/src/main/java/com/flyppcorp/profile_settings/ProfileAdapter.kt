package com.flyppcorp.profile_settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.flyppcorp.flypp.R
import kotlinx.android.synthetic.main.profile_items.view.*

class ProfileAdapter(val profile: MutableList<Profile>): RecyclerView.Adapter<ProfileAdapter.ProfileBuilder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileBuilder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_items, parent, false)
        return ProfileBuilder(view)
    }

    override fun getItemCount(): Int = profile.size

    override fun onBindViewHolder(holder: ProfileBuilder, position: Int) {
        holder.bind(profile[position])
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(position)
        }
    }

    var onItemClick:((Int) -> Unit)? = null


    inner class ProfileBuilder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(profile: Profile) {
            with(profile){
                itemView.imgLogo.setImageResource(profile.logo)
                itemView.txtMyName.text = nomeOption
            }
        }

    }

}




